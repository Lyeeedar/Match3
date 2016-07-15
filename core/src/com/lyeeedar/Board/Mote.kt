package com.lyeeedar.Board

import com.badlogic.gdx.math.*
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteRenderer

/**
 * Created by Philip on 15-Jul-16.
 */

class Mote(val pos: Vector2, val dst: Vector2, val dir: Vector2, val sprite: Sprite, val grid: Grid, val function: () -> Unit)
{
	var time = 0f
	var duration = 1.5f

	var done = false

	lateinit var path: Path<Vector2>

	init
	{
		val p0 = pos
		val p1 = Vector2().set(dir).scl(50f + MathUtils.random(125).toFloat()).add(pos)
		val p2 = Vector2().set(pos).lerp(dst, 0.8f)
		val p3 = dst

		path = Bezier(p0, p1, p2, p3)
	}

	fun update(delta: Float)
	{
		if (done) return

		time += delta
		if (time >= duration)
		{
			done = true
			function()
		}

		val alpha = time / duration

		path.valueAt(pos, alpha * alpha * alpha)
	}
}