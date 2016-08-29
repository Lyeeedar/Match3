package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getPool
import com.lyeeedar.Util.vectorToAngle

/**
 * Created by Philip on 14-Aug-16.
 */

internal class Particle
{
	enum class BlendMode
	{
		ADDITIVE,
		MULTIPLICATIVE
	}

	private val temp = Vector2()

	private val particles = Array<ParticleData>(false, 16)

	private lateinit var lifetime: Range
	private lateinit var blend: BlendMode
	private var drag = 0f
	private var velocityAligned = false
	private val texture = StepTimeline<TextureRegion>()
	private val colour = ColourTimeline()
	private val alpha = LerpTimeline()
	private val rotationSpeed = RangeLerpTimeline()
	private val size = RangeLerpTimeline()

	fun particleCount() = particles.size
	fun complete() = particles.size == 0

	fun simulate(delta: Float)
	{
		val itr = particles.iterator()
		while (itr.hasNext())
		{
			val particle = itr.next()
			particle.life += delta
			if (particle.life > lifetime.v2)
			{
				itr.remove()
				particle.free()
			}
			else
			{
				if (velocityAligned)
				{
					particle.rotation = vectorToAngle(particle.velocity.x, particle.velocity.y)
				}
				else
				{
					val rotation = rotationSpeed.valAt(particle.rotStream, particle.life).lerp(particle.ranVal)
					particle.rotation += rotation * delta
				}

				particle.speed -= drag * delta
				if (particle.speed < 0f) particle.speed = 0f

				temp.set(particle.velocity)
				temp.scl(particle.speed)

				particle.position.add(temp)
			}
		}
	}

	fun render(batch: SpriteBatch, offsetx: Float, offsety: Float, tileSize: Float, modifierColour: Color)
	{
		if (blend == BlendMode.ADDITIVE)
		{
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE)
		}
		else if (blend == BlendMode.MULTIPLICATIVE)
		{
			batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
		}

		for (particle in particles)
		{
			val tex = texture.valAt(particle.texStream, particle.life)
			val col = colour.valAt(particle.colStream, particle.life)
			col.a = alpha.valAt(particle.alphaStream, particle.life)
			val size = size.valAt(particle.sizeStream, particle.life).lerp(particle.ranVal) * tileSize

			col.mul(modifierColour)

			val drawx = particle.position.x * tileSize + offsetx
			val drawy = particle.position.y * tileSize + offsety

			batch.color = col
			batch.draw(tex, drawx, drawy, 0.5f, 0.5f, 1f, 1f, size, size, particle.rotation)
		}
	}

	fun spawn(position: Vector2, velocity: Vector2, speed: Float, rotation: Float)
	{
		val particle = ParticleData.obtain().set(
				position, velocity,
				speed, rotation, lifetime.v1 * MathUtils.random(),
				MathUtils.random(texture.streams.size-1),
				MathUtils.random(colour.streams.size-1),
				MathUtils.random(alpha.streams.size-1),
				MathUtils.random(rotationSpeed.streams.size-1),
				MathUtils.random(size.streams.size-1),
				MathUtils.random())

		particles.add(particle)
	}

	companion object
	{
		fun load(xml: XmlReader.Element): Particle
		{
			val particle = Particle()

			particle.lifetime = Range(xml.get("Lifetime"))
			particle.blend = BlendMode.valueOf(xml.get("BlendMode", "Additive").toUpperCase())
			particle.drag = xml.getFloat("Drag", 0f)
			particle.velocityAligned = xml.getBoolean("VelocityAligned", false)

			val textureEls = xml.getChildByName("TextureKeyframes")
			if (textureEls != null)
			{
				particle.texture.parse(textureEls, { AssetManager.loadTextureRegion(it) ?: throw RuntimeException("Failed to find texture $it!") }, particle.lifetime.v2)
			}
			else
			{
				particle.texture[0, 0f] = AssetManager.loadTextureRegion("white")!!
			}

			val colourEls = xml.getChildByName("ColourKeyframes")
			if (colourEls != null)
			{
				particle.colour.parse(colourEls, { AssetManager.loadColour(it) }, particle.lifetime.v2)
			}
			else
			{
				particle.colour[0, 0f] = Color(1f, 1f, 1f, 1f)
			}

			val alphaEls = xml.getChildByName("AlphaKeyframes")
			if (alphaEls != null)
			{
				particle.alpha.parse(alphaEls, { it.toFloat() }, particle.lifetime.v2)
			}
			else
			{
				particle.alpha[0, 0f] = 1f
			}

			val rotationSpeedEls = xml.getChildByName("RotationSpeedKeyframes")
			if (rotationSpeedEls != null)
			{
				particle.rotationSpeed.parse(rotationSpeedEls, { Range(it) }, particle.lifetime.v2)
			}
			else
			{
				particle.rotationSpeed[0, 0f] = Range(0f, 0f)
			}

			val sizeEls = xml.getChildByName("SizeKeyframes")
			if (sizeEls != null)
			{
				particle.size.parse(sizeEls, { Range(it) }, particle.lifetime.v2)
			}
			else
			{
				particle.size[0, 0f] = Range(1f, 1f)
			}

			return particle
		}
	}
}

internal data class ParticleData(val position: Vector2, val velocity: Vector2,
								 var speed: Float, var rotation: Float, var life: Float,
								 var texStream: Int, var colStream: Int, var alphaStream: Int, var rotStream: Int, var sizeStream: Int,
								 var ranVal: Float)
{
	constructor(): this(Vector2(), Vector2(0f, 1f), 0f, 0f, 0f, 0, 0, 0, 0, 0, 0f)

	fun set(position: Vector2, velocity: Vector2, speed: Float, rotation: Float, life: Float, texStream: Int, colStream: Int, alphaStream: Int, rotStream: Int, sizeStream: Int, ranVal: Float): ParticleData
	{
		this.position.set(position)
		this.velocity.set(velocity)
		this.speed = speed
		this.life = life
		this.rotation = rotation
		this.texStream = texStream
		this.colStream = colStream
		this.alphaStream = alphaStream
		this.rotStream = rotStream
		this.sizeStream = sizeStream
		this.ranVal = ranVal
		return this
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ParticleData> = getPool()

		@JvmStatic fun obtain(): ParticleData
		{
			val particle = pool.obtain()

			if (particle.obtained) throw RuntimeException()

			particle.obtained = true
			particle.life = 0f
			return particle
		}
	}
	fun free() { if (obtained) { pool.free(this); obtained = false } }
}