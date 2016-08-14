package com.lyeeedar.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getPool

/**
 * Created by Philip on 14-Aug-16.
 */

internal class Particle
{
	private val temp = Vector2()

	private val particles = Array<ParticleData>(false, 16)

	private var lifetime = 0f
	private var lifetimeOffset = 0f
	private var drag = 0f
	private val texture = StepTimeline<TextureRegion>()
	private val colour = ColourTimeline()
	private val alpha = LerpTimeline()
	private val rotationSpeed = LerpTimeline()
	private val size = LerpTimeline()

	fun particleCount() = particles.size
	fun complete() = particles.size == 0

	fun simulate(delta: Float)
	{
		val itr = particles.iterator()
		while (itr.hasNext())
		{
			val particle = itr.next()
			particle.life += delta
			if (particle.life > lifetime)
			{
				itr.remove()
				particle.free()
			}
			else
			{
				val rotation = rotationSpeed.valAt(particle.life)

				particle.rotation += rotation * delta
				particle.speed -= drag * delta
				if (particle.speed < 0f) particle.speed = 0f

				temp.set(0f, 1f)
				temp.rotate(particle.rotation)
				temp.scl(particle.speed)

				particle.position.add(temp)
			}
		}
	}

	fun render(batch: SpriteBatch, offsetx: Float, offsety: Float, tileSize: Float)
	{
		for ((position, speed, rotation, life) in particles)
		{
			val tex = texture.valAt(life)
			val col = colour.valAt(life)
			col.a = alpha.valAt(life)
			val size = size.valAt(life) * tileSize

			val drawx = position.x * tileSize + offsetx
			val drawy = position.y * tileSize + offsety

			batch.color = col
			batch.draw(tex, drawx, drawy, 0.5f, 0.5f, 1f, 1f, size, size, rotation)
		}
	}

	fun spawn(position: Vector2, speed: Float, rotation: Float)
	{
		val particle = ParticleData.obtain().set(position, speed, rotation)
		particle.life = lifetimeOffset * MathUtils.random()
		particles.add(particle)
	}

	companion object
	{
		fun load(xml: XmlReader.Element): Particle
		{
			val particle = Particle()

			particle.lifetime = xml.getFloat("Lifetime")
			particle.lifetimeOffset = xml.getFloat("LifetimeOffset", 0f)
			particle.drag = xml.getFloat("Drag", 0f)

			val textureEls = xml.getChildByName("TextureKeyframes")
			if (textureEls != null)
			{
				particle.texture.parse(textureEls, { AssetManager.loadTextureRegion(it) ?: throw RuntimeException("Failed to find texture $it!") }, particle.lifetime)
			}
			else
			{
				particle.texture[0f] = AssetManager.loadTextureRegion("white")!!
			}

			val colourEls = xml.getChildByName("ColourKeyframes")
			if (colourEls != null)
			{
				for (i in 0..colourEls.childCount-1)
				{
					val el = colourEls.getChild(i)
					val time = el.getFloat("Time")
					val colEl = el.getChildByName("Value")
					val col = AssetManager.loadColour(colEl)

					particle.colour[time * particle.lifetime] = col
				}
			}
			else
			{
				particle.colour[0f] = Color(1f, 1f, 1f, 1f)
			}

			val alphaEls = xml.getChildByName("AlphaKeyframes")
			if (alphaEls != null)
			{
				particle.alpha.parse(alphaEls, { it.toFloat() }, particle.lifetime)
			}
			else
			{
				particle.alpha[0f] = 1f
			}

			val rotationSpeedEls = xml.getChildByName("RotationSpeedKeyframes")
			if (rotationSpeedEls != null)
			{
				particle.rotationSpeed.parse(rotationSpeedEls, { it.toFloat() }, particle.lifetime)
			}
			else
			{
				particle.rotationSpeed[0f] = 0f
			}

			val sizeEls = xml.getChildByName("SizeKeyframes")
			if (sizeEls != null)
			{
				particle.size.parse(sizeEls, { it.toFloat() }, particle.lifetime)
			}
			else
			{
				particle.size[0f] = 1f
			}

			return particle
		}
	}
}

internal data class ParticleData(val position: Vector2, var speed: Float, var rotation: Float, var life: Float)
{
	constructor(): this(Vector2(), 0f, 0f, 0f)

	fun set(position: Vector2, speed: Float, rotation: Float): ParticleData
	{
		this.position.set(position)
		this.speed = speed
		this.rotation = rotation
		return this
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ParticleData> = getPool()

		@JvmStatic fun obtain(): ParticleData
		{
			val particle = ParticleData.pool.obtain()

			if (particle.obtained) throw RuntimeException()

			particle.obtained = true
			particle.life = 0f
			return particle
		}
	}
	fun free() { if (obtained) { ParticleData.pool.free(this); obtained = false } }
}