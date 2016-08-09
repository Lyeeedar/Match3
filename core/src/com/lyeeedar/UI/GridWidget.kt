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
import com.lyeeedar.Board.Orb
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Targetter
import com.lyeeedar.Player.Player
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Future
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 05-Jul-16.
 */

class GridWidget(val grid: Grid) : Widget()
{
	val glow: Sprite = AssetManager.loadSprite("glow")
	val frame: Sprite = AssetManager.loadSprite("GUI/frame", colour = Color(0.6f, 0.7f, 0.9f, 0.6f))
	val border: Sprite = AssetManager.loadSprite("GUI/border", colour = Color(0.6f, 0.9f, 0.6f, 0.6f))
	val hp_full: Sprite = AssetManager.loadSprite("GUI/health_full")
	val hp_empty: Sprite = AssetManager.loadSprite("GUI/health_empty")
	val atk_full: Sprite = AssetManager.loadSprite("GUI/attack_full")
	val atk_empty: Sprite = AssetManager.loadSprite("GUI/attack_empty")

	val background: SpriteRenderer = SpriteRenderer()
	val foreground: SpriteRenderer = SpriteRenderer()
	val floating: SpriteRenderer = SpriteRenderer()

	val bitflag: EnumBitflag<Direction> = EnumBitflag()

	val tempCol = Color()

	init
	{
		instance = this

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

		atk_empty.baseScale = floatArrayOf(0.14f, 0.14f)
		atk_full.baseScale = floatArrayOf(0.14f, 0.14f)
	}

	fun pointToScreenspace(point: Point): Vector2
	{
		val xp = x.toFloat() + (width.toFloat() / 2f) - ((grid.width * Global.tileSize) / 2f)

		val actualx = point.x * Global.tileSize + xp
		val actualy = ((grid.height-1) - point.y) * Global.tileSize + y

		return Vector2(actualx, actualy)
	}

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
				val chest = tile.chest
				val monster = tile.monster

				var tileColour = Color.WHITE
				var orbColour = Color.WHITE
				var blockColour = Color.WHITE
				var monsterColour = Color.WHITE

				if (grid.activeAbility != null)
				{
					if (grid.activeAbility!!.targetter.isValid(tile, grid.activeAbility!!.data))
					{
						if (grid.activeAbility!!.targetter.type == Targetter.Type.ORB)
						{
							tileColour = Color.DARK_GRAY
							orbColour = Color.WHITE
							blockColour = Color.DARK_GRAY
							monsterColour = Color.DARK_GRAY
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.BLOCK)
						{
							tileColour = Color.DARK_GRAY
							orbColour = Color.DARK_GRAY
							blockColour = Color.WHITE
							monsterColour = Color.DARK_GRAY
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.EMPTY)
						{
							tileColour = Color.WHITE
							orbColour = Color.DARK_GRAY
							blockColour = Color.DARK_GRAY
							monsterColour = Color.DARK_GRAY
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.MONSTER)
						{
							tileColour = Color.DARK_GRAY
							orbColour = Color.DARK_GRAY
							blockColour = Color.DARK_GRAY
							monsterColour = Color.WHITE
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.ATTACK)
						{
							tileColour = Color.DARK_GRAY
							orbColour = Color.WHITE
							blockColour = Color.DARK_GRAY
							monsterColour = Color.DARK_GRAY
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.TILE)
						{
							val col = if (tile.canHaveOrb) Color.WHITE else Color.DARK_GRAY

							tileColour = col
							orbColour = col
							blockColour = col
							monsterColour = col
						}
						else if (grid.activeAbility!!.targetter.type == Targetter.Type.SEALED)
						{
							tileColour = Color.DARK_GRAY
							orbColour = if (orb != null && orb.sealed) Color.WHITE else Color.DARK_GRAY
							blockColour = Color.DARK_GRAY
							monsterColour = Color.DARK_GRAY
						}
					}
					else
					{
						tileColour = Color.DARK_GRAY
						orbColour = Color.DARK_GRAY
						blockColour = Color.DARK_GRAY
						monsterColour = Color.DARK_GRAY
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

				if (chest != null)
				{
					background.queueSprite(chest.sprite, xi, yi, xp, yp, SpaceSlot.TILE, 1, tileColour)
				}

				for (sprite in tile.effects)
				{
					if (sprite.completed)
					{
						tile.effects.removeValue(sprite, true)
					}
					else
					{
						floating.queueSprite(sprite, xi, yi, xp, yp, SpaceSlot.EFFECT, 0)
					}
				}

				if (orb != null)
				{
					foreground.queueSprite(orb.sprite, xi, yi, xp, yp, SpaceSlot.ORB, 1, orbColour)

					if (orb.sealed)
					{
						foreground.queueSprite(orb.sealSprite, xi, yi, xp, yp, SpaceSlot.ORB, 2, orbColour)
					}

					if (orb.armed != null)
					{
						foreground.queueSprite(glow, xi, yi, xp, yp, SpaceSlot.ORB, 0)
					}

					if (orb.hasAttack)
					{
						val cx = xi + (orb.sprite.spriteAnimation?.renderOffset()?.get(0) ?: 0f) / Global.tileSize
						val cy = yi + 0.15f + (orb.sprite.spriteAnimation?.renderOffset()?.get(1) ?: 0f) / Global.tileSize

						val currentPoint = Vector2(0f, 0.4f)

						val maxdots = 10
						val degreesStep = 360f / maxdots
						for (i in 0..maxdots-1)
						{
							val sprite = if(i < orb.attackTimer) atk_full else atk_empty

							floating.queueSprite(sprite, cx + currentPoint.x, cy + currentPoint.y, xp, yp, SpaceSlot.ORB, 2, orbColour)

							currentPoint.rotate(degreesStep)
						}

						//foreground.queueSprite(orb.attackIcon, xi, yi, xp, yp, SpaceSlot.ORB, 2, orbColour)
					}
				}

				if (monster != null && tile == monster.tiles[0, monster.size-1])
				{
					monster.sprite.size[0] = monster.size
					monster.sprite.size[1] = monster.size
					foreground.queueSprite(monster.sprite, xi, yi, xp, yp, SpaceSlot.ORB, 1, monsterColour)

					// do hp bar
					val solidSpaceRatio = 0.12f // 20% free space
					val space = monster.size.toFloat()
					val spacePerPip = space / monster.maxhp.toFloat()
					val spacing = spacePerPip * solidSpaceRatio
					val solid = spacePerPip - spacing

					for (i in 0..monster.maxhp-1)
					{
						val sprite = if(i < monster.hp) hp_full else hp_empty
						floating.queueSprite(sprite, xi+i*spacePerPip, yi+0.1f, xp, yp, SpaceSlot.ORB, 2, width = solid, height = 0.15f)
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

				if (grid.noMatchTimer > 10f && grid.matchHint != null)
				{
					if (tile == grid.matchHint!!.first || tile == grid.matchHint!!.second)
					{
						foreground.queueSprite(border, xi, yi, xp, yp, SpaceSlot.ORB, 0)
					}
				}
			}
		}

		background.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
		foreground.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
		floating.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
	}

	companion object
	{
		lateinit var instance: GridWidget
	}
}