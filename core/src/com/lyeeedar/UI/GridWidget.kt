package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Mote
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Targetter
import com.lyeeedar.Player.Player
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 05-Jul-16.
 */

class GridWidget(val grid: Grid) : Widget()
{
	init
	{
		touchable = Touchable.enabled

		addListener(object : InputListener()
		{
			override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean
			{
				val xp = x + ((grid.width * Global.tileSize) / 2f) - (width.toFloat() / 2f)

				val sx = (xp / Global.tileSize).toInt()
				val sy = (grid.height-1) - (y / Global.tileSize).toInt()

				grid.select(Point(sx, sy))

				return true
			}

			override fun touchDragged (event: InputEvent?, x: Float, y: Float, pointer: Int)
			{
				if (grid.selected != Point.MINUS_ONE)
				{
					val xp = x + ((grid.width * Global.tileSize) / 2f) - (width.toFloat() / 2f)

					val sx = (xp / Global.tileSize).toInt()
					val sy = (grid.height - 1) - (y / Global.tileSize).toInt()

					val point = Point(sx, sy)

					if (point != grid.selected)
					{
						grid.select(point)
					}
				}
			}
		})

		grid.onPop += {
			val xp = x.toFloat() + (width.toFloat() / 2f) - ((grid.width * Global.tileSize) / 2f)

			val actualx = it.x * Global.tileSize + xp
			val actualy = ((grid.height-1) - it.y) * Global.tileSize + y

			val pos = Vector2(actualx, actualy)
			val dst = PowerBar.instance.getOrbDest()
			val dir = Vector2().setToRandomDirection()
			val sprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/crystal_cloud")
			sprite.colour = Color.CYAN

			if (dst != null)
			{
				val mote = Mote(pos, dst, dir, sprite, grid, { PowerBar.instance.power++ })
				grid.motes.add(mote)
			}
		}
	}

	val glow: Sprite = AssetManager.loadSprite("glow")
	val frame: Sprite = AssetManager.loadSprite("GUI/frame", colour = Color(0.6f, 0.7f, 0.9f, 0.6f))
	val border: Sprite = AssetManager.loadSprite("GUI/border", colour = Color(0.6f, 0.9f, 0.6f, 0.6f))

	val background: SpriteRenderer = SpriteRenderer()
	val foreground: SpriteRenderer = SpriteRenderer()
	val floating: SpriteRenderer = SpriteRenderer()

	val bitflag: EnumBitflag<Direction> = EnumBitflag()

//	override fun getPrefHeight(): Float
//	{
//		return grid.height.toFloat() * Global.tileSize
//	}
//
//	override fun getPrefWidth(): Float
//	{
//		return grid.width.toFloat() * Global.tileSize
//	}

	override fun invalidate()
	{
		super.invalidate()

		val w = width.toFloat() / grid.width.toFloat()
		val h = (height.toFloat() - 16f) / grid.height.toFloat()

		Global.tileSize = Math.min(w, h)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		val xp = this.x.toFloat() + (this.width.toFloat() / 2f) - ((grid.width * Global.tileSize) / 2f)
		val yp = this.y.toFloat()

		if (grid.activeAbility == null)
		{
			batch!!.color = Color.WHITE
		}

		for (x in 0..grid.width-1)
		{
			for (y in 0..grid.height-1)
			{
				val tile = grid.grid[x, y]
				val orb = tile.orb
				val block = tile.block

				var tileColour = Color.WHITE
				var orbColour = Color.WHITE
				var blockColour = Color.WHITE

				if (grid.activeAbility != null)
				{
					if (grid.activeAbility!!.targetter.isValid(tile))
					{
						if (grid.activeAbility!!.targetter.type == Targetter.Type.ORB)
						{
							tileColour = Color.DARK_GRAY
							orbColour = Color.WHITE
							blockColour = Color.DARK_GRAY
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.BLOCK)
						{
							tileColour = Color.DARK_GRAY
							orbColour = Color.DARK_GRAY
							blockColour = Color.WHITE
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.EMPTY)
						{
							tileColour = Color.WHITE
							orbColour = Color.DARK_GRAY
							blockColour = Color.DARK_GRAY
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.TILE)
						{
							val col = if (tile.canHaveOrb) Color.WHITE else Color.DARK_GRAY

							tileColour = col
							orbColour = col
							blockColour = col
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.SEALED)
						{
							tileColour = Color.DARK_GRAY
							orbColour = if (orb != null && orb.sealed) Color.WHITE else Color.DARK_GRAY
							blockColour = Color.DARK_GRAY
						}
					}
					else
					{
						tileColour = Color.DARK_GRAY
						orbColour = Color.DARK_GRAY
						blockColour = Color.DARK_GRAY
					}
				}

				val xi = x.toFloat()
				val yi = (grid.height-1) - y.toFloat()

				if (tile.sprite.sprite != null)
				{
					background.queueSprite(tile.sprite.sprite!!, xi, yi, xp, yp, SpaceSlot.TILE, 0, tileColour)
				}
				if (tile.sprite.tilingSprite != null)
				{
					val tiling = tile.sprite.tilingSprite!!
					grid.buildTilingBitflag(bitflag, x, y, tiling.checkID)
					val sprite = tiling.getSprite( bitflag )
					background.queueSprite(sprite, xi, yi, xp, yp, SpaceSlot.TILE, 0, tileColour)

					if (tiling.overhang != null && bitflag.contains(Direction.NORTH))
					{
						foreground.queueSprite(tiling.overhang!!, xi, (grid.height) - y.toFloat(), xp, yp, SpaceSlot.OVERHANG, 0, tileColour)
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
						foreground.queueSprite(sprite, xi, yi, xp, yp, SpaceSlot.EFFECT, 0)
					}
				}

				if (orb != null)
				{
					foreground.queueSprite(orb.sprite, xi, yi, xp, yp, SpaceSlot.ORB, 1, orbColour)

					if (orb.sealed)
					{
						foreground.queueSprite(orb.sealSprite, xi, yi, xp, yp, SpaceSlot.ORB, 2, orbColour)
					}

					if (orb.armed)
					{
						foreground.queueSprite(glow, xi, yi, xp, yp, SpaceSlot.ORB, 0)
					}
				}

				if (block != null)
				{
					foreground.queueSprite(block.sprite, xi, yi, xp, yp, SpaceSlot.ORB, 1, blockColour)
				}

				if (tile.isSelected)
				{
					foreground.queueSprite(frame, xi, yi, xp, yp, SpaceSlot.ORB, 0)
				}

				if (grid.noMatchTimer > 5f && grid.matchHint != null)
				{
					if (tile == grid.matchHint!!.first || tile == grid.matchHint!!.second)
					{
						foreground.queueSprite(border, xi, yi, xp, yp, SpaceSlot.ORB, 0)
					}
				}
			}
		}

		for (mote in grid.motes)
		{
			if (!mote.done)
			{
				floating.queueSprite(mote.sprite, mote.pos.x / Global.tileSize, mote.pos.y / Global.tileSize, 0f, 0f, SpaceSlot.MOTE, 0)
			}
		}

		background.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
		foreground.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
		floating.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
	}
}