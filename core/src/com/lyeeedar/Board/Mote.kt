package com.lyeeedar.Board

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.*
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteEffectActor

class Mote(val src: Vector2, dst: Vector2, val sprite: Sprite, val completionFunc: (() -> Unit)? = null) : SpriteEffectActor(sprite, 32f, 32f, Vector2(), { completionFunc?.invoke(); moteCount-- })
{
	init
	{
		val dir = Vector2().setToRandomDirection()
		val p0 = pos
		val p1 = Vector2().set(dir).scl(50f + MathUtils.random(125).toFloat()).add(pos)
		val p2 = Vector2().set(pos).lerp(dst, 0.8f)
		val p3 = dst

		val path = Bezier(p0, p1, p2, p3)
		sprite.spriteAnimation = MoveAnimation.obtain().set(1.5f, path, Interpolation.exp6)
		
		moteCount++
	}

	
	companion object
	{
		var moteCount = 0
	}
}
