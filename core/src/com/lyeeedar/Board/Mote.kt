package com.lyeeedar.Board

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.*
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteRenderer

/**
 * Created by Philip on 15-Jul-16.
 */

class Mote(val pos: Vector2, dst: Vector2, val sprite: Sprite, val function: () -> Unit): Actor()
{
	var time = 0f
	var duration = 1.5f

	val dir = Vector2().setToRandomDirection()

	var done = false

	lateinit var path: Path<Vector2>

	init
	{
		val p0 = pos
		val p1 = Vector2().set(dir).scl(50f + MathUtils.random(125).toFloat()).add(pos)
		val p2 = Vector2().set(pos).lerp(dst, 0.8f)
		val p3 = dst

		path = Bezier(p0, p1, p2, p3)

		Global.stage.addActor(this)
	}

	override fun act(delta: Float)
	{
		if (done) return

		super.act(delta)

		time += delta
		if (time >= duration)
		{
			done = true
			function()
			remove()
		}

		val alpha = time / duration

		path.valueAt(pos, alpha * alpha * alpha)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		super.draw(batch, parentAlpha)

		sprite.render(batch as SpriteBatch, pos.x, pos.y, 32f, 32f)
	}
}