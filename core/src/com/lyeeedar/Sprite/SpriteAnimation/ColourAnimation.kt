package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools

/**
 * Created by Philip on 22-Jul-16.
 */

class ColourAnimation
{
	val targetColour: Color = Color()
	val colour: Color = Color()

	var oneTime = true
	var duration: Float = 1f
	var time: Float = 0f

	fun update(delta: Float, baseColour: Color): Boolean
	{
		time += delta

		val alpha = MathUtils.clamp(Math.abs((time - duration / 2) / (duration / 2)), 0f, 1f)

		colour.set(targetColour).lerp(baseColour, alpha)

		if (time >= duration)
		{
			if (oneTime) return true
			else time -= duration
		}

		return false
	}

	fun set(colour: Color, duration: Float, oneTime: Boolean): ColourAnimation
	{
		this.targetColour.set(colour)
		this.duration = duration
		this.oneTime = oneTime

		return this
	}

	var obtained: Boolean = false
	companion object
	{
		private val pool: Pool<ColourAnimation> = Pools.get( ColourAnimation::class.java, Int.MAX_VALUE )

		@JvmStatic fun obtain(): ColourAnimation
		{
			val anim = ColourAnimation.pool.obtain()

			if (anim.obtained) throw RuntimeException()

			anim.obtained = true
			anim.time = 0f
			return anim
		}
	}
	fun free() { if (obtained) { ColourAnimation.pool.free(this); obtained = false } }
}