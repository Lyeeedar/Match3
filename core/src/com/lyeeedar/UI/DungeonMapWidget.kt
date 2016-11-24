package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.MainGame
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Player.Player
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Screens.GridScreen
import com.lyeeedar.Screens.TownScreen
import com.lyeeedar.Util.*
import sun.applet.Main

/**
 * Created by Philip on 24-Jul-16.
 */

class DungeonMapWidget(val map: DungeonMap, val player: Player): Widget()
{
	val tileSize = 64f

	var moveFrom: Point? = null
	var moveTo: Point? = null

	val upArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")
	val downArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")
	val leftArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")
	val rightArrow = AssetManager.loadSprite("Oryx/Custom/ui/arrow")

	val fog = TilingSprite("fog", "Masks/fog", "Masks/fog")

	val tempPoint = Point()

	lateinit var playerSprite: Sprite

	val ROOMBASE = 0
	val ROOMDETAIL = 1
	val ARROWS = 3
	val FOG = 2
	val PLAYER = 4

	val renderer = SortedRenderer(tileSize, map.width.toFloat(), map.height.toFloat(), 5)
	val bitflag = EnumBitflag<Direction>()
	var waitingOnTransition: Boolean = false
	var dungeonComplete = false

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

	fun completeDungeon()
	{
		Global.game.switchScreen(MainGame.ScreenEnum.TOWN)
		TownScreen.instance.playerData.mergePlayerDataBack(player)
	}

	override fun act(delta: Float)
	{
		super.act(delta)

		if (dungeonComplete)
		{
		}
		else if (player.hp <= 0)
		{
			dungeonComplete = true
			MessageBox("Out of life!", "You collapse from your wounds, falling unconscious. Some time later you wake up back in town, alive but with your pockets misteriously lighter.",
					Pair("Okay", {player.gold /= 2; completeDungeon()}))
		}
		else if (map.objective.isCompleted())
		{
			dungeonComplete = true
			MessageBox("Quest Complete", "You have completed your all your tasks in your quest. Return to town?",
					Pair("Yes", {completeDungeon()}),
					Pair("Continue Exploring", {val m = MessageBox("Continue Exploration", "You can tap the icon in the top left when you are ready to return to town.", Pair("Okay", {}))})
			)
		}

		playerSprite.update(delta)

		if (waitingOnTransition) return

		val playerRoom = map.get(map.playerPos)!!
		playerRoom.seen = true

		if (playerSprite.animation == null && playerRoom.isRoom && !playerRoom.isCompleted)
		{
			moveTo = null
			moveFrom = null

			waitingOnTransition = true
			Future.call(
					{
						FullscreenMessage(playerRoom.level!!.entryText, "",
								{
									playerRoom.level!!.create(map.theme, player)
									waitingOnTransition = false; Global.game.switchScreen(MainGame.ScreenEnum.GRID)
									GridScreen.instance.updateLevel(playerRoom.level!!, player)
								}).show()
					}, 0.5f)

		}
		else if (moveTo != null && playerSprite.animation == null)
		{
			map.playerPos.set(moveTo!!)

			playerSprite.animation = MoveAnimation.obtain().set(moveTo!!, moveFrom!!, 0.3f)

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
		super.draw(batch, parentAlpha)

		var offsetx = (x + width / 2) - map.playerPos.x * tileSize - tileSize*0.5f
		var offsety = (y + height / 2) - map.playerPos.y * tileSize - tileSize*0.5f

		if (playerSprite.animation != null)
		{
			val offset = playerSprite.animation!!.renderOffset()!!
			offsetx -= offset[0] * tileSize
			offsety -= offset[1] * tileSize
		}

		renderer.queueSprite(playerSprite, map.playerPos.x.toFloat(), map.playerPos.y.toFloat() + 0.2f, PLAYER, 0, update = false)

		for (entry in map.map.values())
		{
			var sprite: Sprite?

			bitflag.clear()
			fun setFlag(dir: Direction) { if (entry.connections.containsKey(dir)) bitflag.setBit(dir) }
			setFlag(Direction.NORTH)
			setFlag(Direction.SOUTH)
			setFlag(Direction.EAST)
			setFlag(Direction.WEST)

			if (bitflag.bitFlag == 0) bitflag.setBit(Direction.CENTRE)

			if (entry.isRoom)
			{
				sprite = map.theme.mapRoom.getSprite(bitflag)
			}
			else
			{
				sprite = map.theme.mapCorridor.getSprite(bitflag)
			}

			if (sprite != null)
			{
				renderer.queueSprite(sprite, entry.point.x.toFloat(), entry.point.y.toFloat(), ROOMBASE, 0)
			}

			if (entry.isRoom)
			{
				if (entry.isCompleted)
				{
					if (entry.completedSprite != null) renderer.queueSprite(entry.completedSprite!!, entry.point.x.toFloat(), entry.point.y.toFloat(), ROOMDETAIL, 0)
				}
				else
				{
					if (entry.uncompletedSprite != null) renderer.queueSprite(entry.uncompletedSprite!!, entry.point.x.toFloat(), entry.point.y.toFloat(), ROOMDETAIL, 0)
				}
			}

			if (!entry.seen)
			{
				buildTilingBitflag(bitflag, entry.point)
				val fsprite = fog.getSprite(bitflag)
				renderer.queueSprite(fsprite, entry.point.x.toFloat(), entry.point.y.toFloat(), FOG, 0, colour = Colour(Color.BLACK))
			}

			if (entry.point == map.playerPos && moveTo == null && playerSprite.animation == null && !waitingOnTransition)
			{
				// add arrows
				if (entry.connections.containsKey(Direction.NORTH))
				{
					renderer.queueSprite(upArrow, entry.point.x.toFloat() + Direction.NORTH.x, entry.point.y.toFloat() + Direction.NORTH.y, ARROWS, 0)
				}
				if (entry.connections.containsKey(Direction.SOUTH))
				{
					renderer.queueSprite(downArrow, entry.point.x.toFloat() + Direction.SOUTH.x, entry.point.y.toFloat() + Direction.SOUTH.y, ARROWS, 0)
				}
				if (entry.connections.containsKey(Direction.WEST))
				{
					renderer.queueSprite(leftArrow, entry.point.x.toFloat() + Direction.WEST.x, entry.point.y.toFloat() + Direction.WEST.y, ARROWS, 0)
				}
				if (entry.connections.containsKey(Direction.EAST))
				{
					renderer.queueSprite(rightArrow, entry.point.x.toFloat() + Direction.EAST.x, entry.point.y.toFloat() + Direction.EAST.y, ARROWS, 0)
				}
			}
		}

		renderer.flush(Gdx.app.graphics.deltaTime, offsetx, offsety, batch as SpriteBatch)
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

	// ----------------------------------------------------------------------
	fun getCenterInScreenspace(): Vector2 = localToStageCoordinates(Vector2(width/2, height/2))

	companion object
	{
		lateinit var instance: DungeonMapWidget
	}
}
