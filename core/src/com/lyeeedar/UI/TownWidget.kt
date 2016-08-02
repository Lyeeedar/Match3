package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.Map.World
import com.lyeeedar.Player.Player
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Sprite.TilingSprite
import com.lyeeedar.Town.House
import com.lyeeedar.Town.Town
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.UnsmoothedPath

/**
 * Created by Philip on 02-Aug-16.
 */

class TownWidget(val town: Town, val player: Player) : Widget()
{
	var airship = AssetManager.loadSprite("Oryx/Custom/townmap/airship")
	var grass = AssetManager.loadSprite("Oryx/uf_split/uf_terrain/ground_grass_1")
	var path = TilingSprite("path", "Oryx/uf_split/uf_terrain/floor_extra_5", "Masks/path")

	val tilesWidth = 4 + 8 + 2

	var tilesHeight: Int = 1

	val playerPos: Point = Point()
	val playerSprite: Sprite = player.portrait.copy()

	init
	{
		val rows = 1 + Math.ceil(town.houses.size / 2.0).toInt()
		tilesHeight = rows * 4 + rows * 2

		playerPos.x = tilesWidth / 2
		playerPos.y = tilesHeight / 2

		addListener(object: ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				super.clicked(event, x, y)

				if (playerSprite.spriteAnimation != null) return

				val offsetx = (width / 2) - playerPos.x * Global.tileSize - Global.tileSize*0.5f
				val offsety = (height / 2) - playerPos.y * Global.tileSize - Global.tileSize*0.5f

				val lx = x - offsetx
				val ly = y - offsety

				val ix = (lx / Global.tileSize).toInt()
				val iy = (ly / Global.tileSize).toInt()

				val house = getHouse(ix, iy)
				if (house != null)
				{
					val index = town.houses.indexOf(house)

					val hx = 1 + 8 * (index % 2)
					val hy = tilesHeight - (9 + 6 * (index / 2)) - 1

					moveTo(hx+1, hy-1)
				}
				else if (ix >= 5 && ix < 9 && iy >= tilesHeight-6)
				{
					// airship
					moveTo(tilesWidth/2, tilesHeight - 6)

					val widget = Table()
					val map = WorldMapWidget(World())
					val scroll = ScrollPane(map)
					scroll.setFlingTime(0f)
					scroll.setOverscroll(false, false)
					widget.add(scroll).expand().fill()

					widget.setFillParent(true)
					Global.stage.addActor(widget)
				}
				else if (isOnPath(ix, iy))
				{
					moveTo(ix, iy)
				}
			}
		})
	}

	fun moveTo(x: Int, y: Int)
	{
		val oldPos = Point.obtain().set(playerPos)
		playerPos.set(x, y)

		val cx = tilesWidth / 2

		if (oldPos.x == cx && x == cx)
		{
			val dst = oldPos.dist(playerPos)
			val path = arrayOf(Vector2(0f, (oldPos.y - y) * Global.tileSize), Vector2())
			playerSprite.spriteAnimation = MoveAnimation.obtain().set(dst * 0.2f, UnsmoothedPath(path))
		}
		else if (oldPos.y != y)
		{
			// path to center, then on the y, then to the x
			val path = arrayOf(oldPos, Point(cx, oldPos.y), Point(cx, playerPos.y), playerPos.copy())

			val dst = path.sumBy(fun (p: Point): Int
			{
				val index = path.indexOf(p)
				if (index > 0) return path[index-1].dist(p)
				else return 0
			})

			playerSprite.spriteAnimation = MoveAnimation.obtain().set(dst * 0.2f, path)
		}
		else
		{
			val dst = oldPos.dist(playerPos)
			val path = arrayOf(Vector2((oldPos.x - x) * Global.tileSize, 0f), Vector2())
			playerSprite.spriteAnimation = MoveAnimation.obtain().set(dst * 0.2f, UnsmoothedPath(path))
		}

		oldPos.free()
	}

	fun getHouse(x: Int, y: Int): House?
	{
		for (i in 0..town.houses.size-1)
		{
			val house = town.houses[i]
			val hx = 1f + 8f * (i % 2)
			val hy = tilesHeight - (9f + 6f * (i / 2)) - 1

			if (x >= hx && x < hx + 4 && y >= hy && y < hy + 4) return house
		}

		return null
	}

	fun isOnPath(x: Int, y: Int): Boolean
	{
		for (py in 1..tilesHeight-5)
		{
			if (6 == x && py == y) return true
			if (7 == x && py == y) return true

			// do side paths
			if (py < tilesHeight-10 && (py-1)% 6 == 0)
			{
				for (px in 2..tilesWidth-3)
				{
					if (px == x && py == y) return true
				}
			}
		}

		return false
	}

	val renderer = SpriteRenderer()

	override fun act(delta: Float)
	{
		super.act(delta)

		playerSprite.update(delta)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		Global.tileSize = 48f//width.toFloat() / tilesWidth.toFloat()

		super.draw(batch, parentAlpha)

		var offsetx = (x + width / 2) - playerPos.x * Global.tileSize - Global.tileSize*0.5f
		var offsety = (y + height / 2) - playerPos.y * Global.tileSize - Global.tileSize*0.5f

		if (playerSprite.spriteAnimation != null)
		{
			val offset = playerSprite.spriteAnimation!!.renderOffset()!!
			offsetx -= offset[0]
			offsety -= offset[1]
		}

		// draw grass

		for (x in 0..tilesWidth-1)
		{
			for (y in 0..tilesHeight-1)
			{
				renderer.queueSprite(grass, x.toFloat(), y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 0)
			}
		}

		// draw path
		for (y in 1..tilesHeight-5)
		{
			renderer.queueSprite(path, 6f, y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 1)
			renderer.queueSprite(path, 7f, y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 1)
		}

		airship.size[0] = 4
		airship.size[1] = 4
		renderer.queueSprite(airship, 5f, tilesHeight - 5f, offsetx, offsety, SpaceSlot.ORB, 0)

		for (i in 0..town.houses.size-1)
		{
			val house = town.houses[i]
			val x = 1f + 8f * (i % 2)
			val y = tilesHeight - (9f + 6f * (i / 2)) - 1

			house.sprite.size[0] = 4
			house.sprite.size[1] = 4

			renderer.queueSprite(house.sprite, x, y, offsetx, offsety, SpaceSlot.ORB, 0)

			val left = (i % 2) == 0

			if (left)
			{
				for (px in x.toInt()..(tilesWidth/2))
				{
					renderer.queueSprite(path, px.toFloat(), y.toFloat() - 1f, offsetx, offsety, SpaceSlot.TILE, 1)
				}

				renderer.queueSprite(path, 2f, (y.toFloat()), offsetx, offsety, SpaceSlot.TILE, 1)
				renderer.queueSprite(path, 3f, (y.toFloat()), offsetx, offsety, SpaceSlot.TILE, 1)
			}
			else
			{
				for (px in (tilesWidth/2)..tilesWidth-3)
				{
					renderer.queueSprite(path, px.toFloat(), y.toFloat() - 1f, offsetx, offsety, SpaceSlot.TILE, 1)
				}

				renderer.queueSprite(path, tilesWidth - 4f, (y.toFloat()), offsetx, offsety, SpaceSlot.TILE, 1)
				renderer.queueSprite(path, tilesWidth - 3f, (y.toFloat()), offsetx, offsety, SpaceSlot.TILE, 1)
			}
		}

		renderer.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
		playerSprite.render(batch, x + width / 2 - Global.tileSize * 0.5f, y + height / 2 - Global.tileSize * 0.2f, Global.tileSize, Global.tileSize)
	}
}