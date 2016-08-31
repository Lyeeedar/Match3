package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Util.*
import com.sun.org.apache.xpath.internal.operations.Bool

/**
 * Created by Philip on 14-Aug-16.
 */

class Particle
{
	enum class BlendMode
	{
		ADDITIVE,
		MULTIPLICATIVE
	}

	enum class CollisionAction
	{
		NONE,
		SLIDE,
		BOUNCE,
		DIE
	}

	private val moveVec = Vector2()
	private val oldPos = Vector2()
	private val normal = Vector2()
	private val reflection = Vector2()
	private val temp = Vector2()
	private val collisionList = Array<Direction>(false, 16)

	val particles = Array<ParticleData>(false, 16)

	lateinit var lifetime: Range
	lateinit var blend: BlendMode
	var drag = 0f
	var velocityAligned = false
	lateinit var collision: CollisionAction
	val texture = StepTimeline<TextureRegion>()
	val colour = ColourTimeline()
	val alpha = LerpTimeline()
	val rotationSpeed = RangeLerpTimeline()
	val size = RangeLerpTimeline()

	fun particleCount() = particles.size
	fun complete() = particles.size == 0

	fun simulate(delta: Float, collisionGrid: Array2D<Boolean>?)
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

				particle.speed -= drag * particle.speed * delta
				if (particle.speed < 0f) particle.speed = 0f

				if (particle.velocity.isZero)
				{
					particle.velocity.setToRandomDirection()
				}

				moveVec.set(particle.velocity)
				moveVec.scl(particle.speed * delta)

				oldPos.set(particle.position)

				particle.position.add(moveVec)

				if (collisionGrid != null && collision != CollisionAction.NONE)
				{
					val aabb = getBoundingBox(particle)

					if (checkColliding(aabb, collisionGrid))
					{
						if (collision == CollisionAction.DIE)
						{
							itr.remove()
							particle.free()
						}
						else if (collision == CollisionAction.BOUNCE || collision == CollisionAction.SLIDE)
						{
							// calculate average collision normal
							normal.x = collisionList.sumBy { it.x }.toFloat()
							normal.y = collisionList.sumBy { it.y }.toFloat()
							normal.nor()

							// reflect vector around normal
							val reflected = reflection.set(moveVec).sub(temp.set(normal).scl(2 * moveVec.dot(normal)))

							// handle based on collision action
							if (collision == CollisionAction.BOUNCE)
							{
								particle.speed *= 0.75f

								particle.position.set(oldPos)
								particle.velocity.set(reflected)
								particle.velocity.nor()
							}
							else
							{
								val yaabb = getBoundingBox(particle, temp.set(particle.position.x, oldPos.y))
								val xaabb = getBoundingBox(particle, temp.set(oldPos.x, particle.position.y))

								// negate y
								if (!checkColliding(yaabb, collisionGrid))
								{
									particle.position.y = oldPos.y
								}
								// negate x
								else if (!checkColliding(xaabb, collisionGrid))
								{
									particle.position.x = oldPos.x
								}
								// negate both
								else
								{
									particle.position.set(oldPos)
								}
							}
						}
						else
						{
							throw NotImplementedError("Forgot to add code to deal with collision action")
						}
					}

					Pools.free(aabb)
				}
			}
		}
	}

	fun checkColliding(aabb: Rectangle, collisionGrid: Array2D<Boolean>): Boolean
	{
		collisionList.clear()

		for (x in aabb.x.toInt()..(aabb.x+aabb.width).toInt())
		{
			for (y in aabb.y.toInt()..(aabb.y+aabb.height).toInt())
			{
				if (collisionGrid.tryGet(x, y, false))
				{
					// calculate collision normal

					val wy = (aabb.width + 1f) * ((aabb.y+aabb.height*0.5f) - (y+0.5f))
					val hx = (aabb.height + 1f) * ((aabb.x+aabb.width*0.5f) - (x+0.5f))

					var dir: Direction

					if (wy > hx)
					{
						if (wy > -hx)
						{
							/* top */
							dir = Direction.SOUTH
						}
						else
						{
							/* left */
							dir = Direction.WEST
						}
					}
					else
					{
						if (wy > -hx)
						{
							/* right */
							dir = Direction.EAST
						}
						else
						{
							/* bottom */
							dir = Direction.NORTH
						}
					}

					collisionList.add(dir)
				}
			}
		}

		return collisionList.size > 0
	}

	fun getBoundingBox(particle: ParticleData, overridePos: Vector2? = null): Rectangle
	{
		val size = size.valAt(particle.sizeStream, particle.life).lerp(particle.ranVal)
		val s2 = size * 0.5f

		val x = if (overridePos == null) particle.position.x else overridePos.x
		val y = if (overridePos == null) particle.position.y else overridePos.y

		return Pools.obtain(Rectangle::class.java).set(x-s2, y-s2,size, size)
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
			particle.collision = CollisionAction.valueOf(xml.get("Collision", "None").toUpperCase())
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

data class ParticleData(val position: Vector2, val velocity: Vector2,
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