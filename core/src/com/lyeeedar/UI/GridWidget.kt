package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Board.Grid
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 05-Jul-16.
 */

class GridWidget() : Widget()
{
	init
	{
		touchable = Touchable.enabled

		addListener(object : ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				val sx = (x / Global.tileSize).toInt()
				val sy = (height-1) - (y / Global.tileSize).toInt()

				grid?.select(Point(sx, sy))
			}
		})
	}

	val frame: Sprite = AssetManager.loadSprite("GUI/frame")
	val renderer: SpriteRenderer = SpriteRenderer()
	val bitflag: EnumBitflag<Direction> = EnumBitflag()

	var grid: Grid? = null
		set(value)
		{
			field = value
			invalidate()
		}

	val width: Int
		get() = grid?.width ?: 0

	val height: Int
		get() = grid?.height ?: 0

	override fun getPrefHeight(): Float
	{
		return height.toFloat() * Global.tileSize
	}

	override fun getPrefWidth(): Float
	{
		return width.toFloat() * Global.tileSize
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		if (grid == null) return

		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val tile = grid!!.grid[x, y]
				val orb = tile.orb
				val block = tile.block

				val tileHeight = if (y == 0) SpaceSlot.OVERHANG else SpaceSlot.TILE

				if (tile.sprite.sprite != null)
				{
					renderer.queueSprite(tile.sprite.sprite!!, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), tileHeight, 0)
				}
				if (tile.sprite.tilingSprite != null)
				{
					val tiling = tile.sprite.tilingSprite!!
					grid!!.buildTilingBitflag(bitflag, x, y, tiling.checkID)
					val sprite = tiling.getSprite( bitflag )
					renderer.queueSprite(sprite, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), tileHeight, 0)

					if (tiling.overhang != null && bitflag.contains(Direction.NORTH))
					{
						renderer.queueSprite(tiling.overhang!!, x.toFloat(), (height) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.OVERHANG, 0)
					}
				}

				for (sprite in tile.effects)
				{
					if (sprite.completed)
					{
						tile.effects.removeValue(sprite, true)
					}
					else
					{
						renderer.queueSprite(sprite, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 5)
					}
				}

				if (orb != null)
				{
					renderer.queueSprite(orb.sprite, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 1)

					if (orb.explosion != null)
					{
						renderer.queueSprite(orb.explosion!!.icon, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 2)
					}

					if (orb.sealed)
					{
						renderer.queueSprite(orb.sealSprite, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 4)
					}
				}

				if (block != null)
				{
					renderer.queueSprite(block.sprite, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 1)
				}

				if (tile.isSelected)
				{
					renderer.queueSprite(frame, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 0)
				}

				if (grid!!.noMatchTimer > 5f && grid!!.matchHint != null)
				{
					if (tile == grid!!.matchHint!!.first || tile == grid!!.matchHint!!.second)
					{
						renderer.queueSprite(frame, x.toFloat(), (height-1) - y.toFloat(), this.x.toFloat(), this.y.toFloat(), SpaceSlot.ORB, 0)
					}
				}
			}
		}

		renderer.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
	}
}