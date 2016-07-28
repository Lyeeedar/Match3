package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Player.Player
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Sprite.TilingSprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.UnsmoothedPath

/**
 * Created by Philip on 24-Jul-16.
 */

class DungeonMapWidget(val map: DungeonMap, val player: Player): Widget()
{
	var moveFrom: Point? = null
	var moveTo: Point? = null

	val upArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")
	val downArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")
	val leftArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")
	val rightArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")

	val fog = TilingSprite("fog", "Masks/fog", "Masks/fog")

	val tempPoint = Point()

	lateinit var playerSprite: Sprite

	init
	{
		upArrow.rotation = Direction.NORTH.angle
		upArrow.baseScale = floatArrayOf(0.5f, 0.5f)

		downArrow.rotation = Direction.SOUTH.angle
		downArrow.baseScale = floatArrayOf(0.5f, 0.5f)

		leftArrow.rotation = Direction.WEST.angle
		leftArrow.baseScale = floatArrayOf(0.5f, 0.5f)

		rightArrow.rotation = Direction.EAST.angle
		rightArrow.baseScale = floatArrayOf(0.5f, 0.5f)

		playerSprite = player.portrait.copy()
		playerSprite.baseScale = floatArrayOf(0.75f, 0.75f)

		instance = this

		addListener( object : ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				if (moveTo != null) return

				val cx = width / 2f
				val cy = height / 2f

				val dx = x - cx
				val dy = y - cy

				val dir: Direction?

				if (dx == 0f && dy == 0f)
				{
					// do nothin
					dir = null
				}
				else if (Math.abs( dx ) > Math.abs( dy ))
				{
					// moving on the x
					if (dx < 0)
					{
						// go left
						dir = Direction.WEST
					}
					else
					{
						// go right
						dir = Direction.EAST
					}
				}
				else
				{
					// moving on the y
					if (dy < 0)
					{
						// go down
						dir = Direction.SOUTH
					}
					else
					{
						// go up
						dir = Direction.NORTH
					}
				}

				val room = map.get(map.playerPos)!!

				// check if dst is a valid direction
				if (dir != null && room.connections.containsKey(dir))
				{
					moveFrom = map.playerPos.copy()
					moveTo = map.playerPos + dir
				}
			}
		} )
	}

	val baseRenderer = SpriteRenderer()
	val detailRenderer = SpriteRenderer()
	val bitflag = EnumBitflag<Direction>()

	override fun act(delta: Float)
	{
		super.act(delta)

		playerSprite.update(delta)

		map.get(map.playerPos)!!.seen = true

		if (moveTo != null && playerSprite.spriteAnimation == null)
		{
			map.playerPos.set(moveTo!!)

			playerSprite.spriteAnimation = MoveAnimation.obtain().set(moveTo!!, moveFrom!!, 0.3f)

			val room = map.get(moveTo!!) ?: throw RuntimeException("Tried to move to invalid location")

			if (room.connections.size == 2 && !room.isRoom)
			{
				val lastRoom = map.get(moveFrom!!)!!

				// move onwards
				for (dir in Direction.CardinalValues)
				{
					val connection = room.connections[dir] ?: continue

					if (connection != lastRoom)
					{
						moveFrom = moveTo
						moveTo = moveTo!! + dir

						break
					}
				}
			}
			else
			{
				// else stay put
				moveTo = null
				moveFrom = null
			}
		}
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		Global.tileSize = 64f

		super.draw(batch, parentAlpha)

		var offsetx = (x + width / 2) - map.playerPos.x * Global.tileSize - Global.tileSize*0.5f
		var offsety = (y + height / 2) - map.playerPos.y * Global.tileSize - Global.tileSize*0.5f

		if (playerSprite.spriteAnimation != null)
		{
			val offset = playerSprite.spriteAnimation!!.renderOffset()!!
			offsetx -= offset[0]
			offsety -= offset[1]
		}

		detailRenderer.queueSprite(playerSprite, map.playerPos.x.toFloat(), map.playerPos.y.toFloat() + 0.2f, offsetx, offsety, SpaceSlot.TILE, 1, update = false)

		for (entry in map.map)
		{
			var sprite: Sprite?

			bitflag.clear()
			fun setFlag(dir: Direction) { if (entry.value.connections.containsKey(dir)) bitflag.setBit(dir) }
			setFlag(Direction.NORTH)
			setFlag(Direction.SOUTH)
			setFlag(Direction.EAST)
			setFlag(Direction.WEST)

			if (bitflag.bitFlag == 0) bitflag.setBit(Direction.CENTRE)

			if (entry.value.isRoom)
			{
				sprite = map.theme.mapRoom.getSprite(bitflag)
			}
			else
			{
				sprite = map.theme.mapCorridor.getSprite(bitflag)
			}

			if (sprite != null)
			{
				baseRenderer.queueSprite(sprite, entry.key.x.toFloat(), entry.key.y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 0)
			}

			if (entry.value.isRoom)
			{
				if (entry.value.isCompleted)
				{
					if (entry.value.completedSprite != null) baseRenderer.queueSprite(entry.value.completedSprite!!, entry.key.x.toFloat(), entry.key.y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 1)
				}
				else
				{
					if (entry.value.uncompletedSprite != null) baseRenderer.queueSprite(entry.value.uncompletedSprite!!, entry.key.x.toFloat(), entry.key.y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 1)
				}
			}

			if (!entry.value.seen)
			{
				buildTilingBitflag(bitflag, entry.key)
				val fsprite = fog.getSprite(bitflag)
				baseRenderer.queueSprite(fsprite, entry.key.x.toFloat(), entry.key.y.toFloat(), offsetx, offsety, SpaceSlot.EFFECT, 0, colour = Color.BLACK)
			}

			if (entry.key == map.playerPos && moveTo == null && playerSprite.spriteAnimation == null)
			{
				// add arrows
				if (entry.value.connections.containsKey(Direction.NORTH))
				{
					baseRenderer.queueSprite(upArrow, entry.key.x.toFloat() + Direction.NORTH.x, entry.key.y.toFloat() + Direction.NORTH.y, offsetx, offsety, SpaceSlot.EFFECT, 1)
				}
				if (entry.value.connections.containsKey(Direction.SOUTH))
				{
					baseRenderer.queueSprite(downArrow, entry.key.x.toFloat() + Direction.SOUTH.x, entry.key.y.toFloat() + Direction.SOUTH.y, offsetx, offsety, SpaceSlot.EFFECT, 1)
				}
				if (entry.value.connections.containsKey(Direction.WEST))
				{
					baseRenderer.queueSprite(leftArrow, entry.key.x.toFloat() + Direction.WEST.x, entry.key.y.toFloat() + Direction.WEST.y, offsetx, offsety, SpaceSlot.EFFECT, 1)
				}
				if (entry.value.connections.containsKey(Direction.EAST))
				{
					baseRenderer.queueSprite(rightArrow, entry.key.x.toFloat() + Direction.EAST.x, entry.key.y.toFloat() + Direction.EAST.y, offsetx, offsety, SpaceSlot.EFFECT, 1)
				}
			}
		}

		baseRenderer.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
		detailRenderer.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
	}

	// ----------------------------------------------------------------------
	fun buildTilingBitflag(bitflag: EnumBitflag<Direction>, p: Point)
	{
		// Build bitflag of surrounding tiles
		bitflag.clear()
		for (dir in Direction.Values)
		{
			val point = tempPoint.set(p.x + dir.x, p.y + dir.y)
			val room = map.get(point)

			if (room != null && room.seen)
			{
				bitflag.setBit(dir)
			}
		}
	}

	companion object
	{
		lateinit var instance: DungeonMapWidget
	}
}