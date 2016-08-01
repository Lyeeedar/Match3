package com.lyeeedar.Sprite

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.BinaryHeap
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Util.EnumBitflag
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

class SpriteRenderer
{
	var batchID: Int = 0

	val heap: BinaryHeap<RenderSprite> = BinaryHeap<RenderSprite>()

	var screenShakeRadius: Float = 0f
	var screenShakeAccumulator: Float = 0f
	var screenShakeSpeed: Float = 0f
	var screenShakeAngle: Float = 0f

	// ----------------------------------------------------------------------
	fun setScreenShake(amount: Float)
	{
		screenShakeRadius = amount
	}

	// ----------------------------------------------------------------------
	fun flush(deltaTime: Float, batch: SpriteBatch)
	{
		// do screen shake
		var offsetx = 0f
		var offsety = 0f

		if ( screenShakeRadius > 2 )
		{
			screenShakeAccumulator += deltaTime
			while ( screenShakeAccumulator >= screenShakeSpeed )
			{
				screenShakeAccumulator -= screenShakeSpeed
				screenShakeAngle += ( 150 + MathUtils.random() * 60 )
				screenShakeRadius *= 0.9f
			}

			offsetx += Math.sin( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius
			offsety += Math.cos( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius
		}

		while (heap.size > 0)
		{
			val rs = heap.pop()

			batch.color = rs.colour
			rs.sprite.render(batch, rs.x + offsetx, rs.y + offsety, Global.tileSize * rs.width, Global.tileSize * rs.height )

			rs.free()
		}

		batchID = random.nextInt()
	}

	// ----------------------------------------------------------------------
	fun queueSprite(sprite: Sprite, ix: Float, iy: Float, offsetx: Float, offsety: Float, slot: SpaceSlot, index: Int, colour: Color = Color.WHITE, update: Boolean = true, width: Float = 1f, height: Float = 1f)
	{
		if (update)
		{
			if (sprite.batchID != batchID) sprite.update(Gdx.app.graphics.deltaTime)
		}
		sprite.batchID = batchID

		var x = ix * Global.tileSize + offsetx
		var y = iy * Global.tileSize + offsety

		if ( sprite.spriteAnimation != null )
		{
			val offset = sprite.spriteAnimation?.renderOffset()

			if (offset != null)
			{
				x += offset[0]
				y += offset[1]
			}
		}

		val rs = RenderSprite.obtain().set( sprite, x, y, offsetx, offsety, slot, index, colour, width, height )

		heap.add( rs, rs.comparisonVal )
	}

	companion object
	{
		val random = Random()
	}
}

class RenderSprite : BinaryHeap.Node(0f)
{
	val colour: Color = Color(1f, 1f, 1f, 1f)
	lateinit var sprite: Sprite
	var x: Float = 0f
	var y: Float = 0f
	var width: Float = 1f
	var height: Float = 1f

	var comparisonVal: Float = 0f

	operator fun set(sprite: Sprite, x: Float, y: Float, offsetx: Float, offsety: Float, slot: SpaceSlot, index: Int, colour: Color, width: Float, height: Float): RenderSprite
	{
		this.colour.set(colour)
		this.sprite = sprite
		this.x = x
		this.y = y
		this.width = width
		this.height = height

		val bx = (x - offsetx).toFloat() / Global.tileSize
		val by = (y - offsety).toFloat() / Global.tileSize

		val sx = bx.toInt()
		val sy = by.toInt()

		comparisonVal = MAX_Y_BLOCK_SIZE - sy * Y_BLOCK_SIZE + (MAX_X_BLOCK_SIZE - sx * X_BLOCK_SIZE) + slot.ordinal * 3f + index

		return this
	}

	fun free() = RenderSprite.pool.free(this)

	companion object
	{
		val pool: Pool<RenderSprite> = Pools.get( RenderSprite::class.java, Int.MAX_VALUE )
		fun obtain() = RenderSprite.pool.obtain()

		val X_BLOCK_SIZE = SpaceSlot.Values.size * 3
		val Y_BLOCK_SIZE = X_BLOCK_SIZE * Global.tileSize
		val MAX_Y_BLOCK_SIZE = Y_BLOCK_SIZE * Global.tileSize
		val MAX_X_BLOCK_SIZE = X_BLOCK_SIZE * Global.tileSize
	}
}