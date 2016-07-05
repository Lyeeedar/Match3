package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.badlogic.gdx.utils.XmlReader.Element
import com.lyeeedar.Direction

class BumpAnimation : AbstractSpriteAnimation
{
	override fun duration(): Float = duration

	override fun time(): Float = time

	override fun renderOffset(): FloatArray? = offset

	override fun renderScale(): FloatArray? = null

	private var direction: Direction? = null

	private val offset = floatArrayOf(0f, 0f)
	private var duration: Float = 0f
	private var time: Float = 0f

	constructor()
	{

	}

	fun set(duration: Float, direction: Direction): BumpAnimation
	{
		this.duration = duration

		time = 0f

		this.direction = direction
		return this
	}

	override fun update(delta: Float): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(Math.abs((time - duration / 2) / (duration / 2)), 0f, 1f)

		offset[0] = (32 / 3 * alpha * direction!!.x.toFloat()).toInt().toFloat()
		offset[1] = (32 / 3 * alpha * direction!!.y.toFloat()).toInt().toFloat()

		return time > duration
	}

	override fun set(duration: Float, diff: FloatArray)
	{
		this.duration = duration
		this.direction = Direction.getDirection(diff)
		this.time = 0f
	}

	override fun parse(xml: Element)
	{
	}

	override fun copy(): AbstractSpriteAnimation
	{
		val anim = BumpAnimation()
		anim.direction = direction
		anim.duration = duration

		return anim
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<BumpAnimation> = Pools.get( BumpAnimation::class.java, Int.MAX_VALUE )

		@JvmStatic fun obtain(): BumpAnimation
		{
			val anim = BumpAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	override fun free() { if (obtained) { BumpAnimation.pool.free(this); obtained = false } }
}
