package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Global
import com.lyeeedar.Map.World
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Player.Player
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.SortedRenderer
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Town.House
import com.lyeeedar.Town.Town
import com.lyeeedar.Util.*

/**
 * Created by Philip on 02-Aug-16.
 */

class TownWidget(val town: Town, val player: PlayerData) : Widget()
{
	val tileSize = 48f

	var gate = AssetManager.loadSprite("Oryx/Custom/townmap/gate")
	var grass = AssetManager.loadSprite("Oryx/uf_split/uf_terrain/ground_grass_1")
	var path = TilingSprite("path", "Oryx/uf_split/uf_terrain/floor_extra_5", "Masks/path")
	var emptyHouse = AssetManager.loadSprite("Oryx/Custom/townmap/housebase")

	val tilesWidth = 4 + 8 + 2

	var tilesHeight: Int = 1

	val playerPos: Point
		get() = town.playerPos

	var playerSprite: Sprite = player.chosenSprite.copy()

	lateinit var renderer: SortedRenderer

	init
	{
		playerSprite.drawActualSize = true

		val rows = 1 + Math.ceil(town.houses.size / 2.0).toInt()
		tilesHeight = rows * 4 + rows * 2

		if (playerPos.x == -1 && playerPos.y == -1)
		{
			playerPos.x = tilesWidth / 2
			playerPos.y = tilesHeight / 2
		}

		addListener(object: ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				super.clicked(event, x, y)

				if (playerSprite.animation != null) return

				val offsetx = (width / 2) - playerPos.x * tileSize - tileSize*0.5f
				val offsety = (height / 2) - playerPos.y * tileSize - tileSize*0.5f

				val lx = x - offsetx
				val ly = y - offsety

				val ix = (lx / tileSize).toInt()
				val iy = (ly / tileSize).toInt()

				val house = getHouse(ix, iy)
				if (playerPos.dist(ix, iy) == 0)
				{
					PlayerDataWidget(player)
				}
				else if (house != null)
				{
					val index = town.houses.indexOf(house)

					val hx = 1 + 8 * (index % 2)
					val hy = tilesHeight - (9 + 6 * (index / 2)) - 1

					moveTo(hx+1, hy-1, {
						town.save()
						if (house.unlocked) house.advance(player)
					})
				}
				else if (ix >= 5 && ix < 9 && iy >= tilesHeight-4)
				{
					// world map
					moveTo(tilesWidth/2, tilesHeight - 5, {
						town.save()

						val widget = Table()
						val closeButton = Button(Global.skin, "close")
						closeButton.setSize(24f, 24f)

						val map = WorldMapWidget(town.world, player, widget, closeButton)
						val scroll = ScrollPane(map)
						scroll.setFlingTime(0f)
						scroll.setOverscroll(false, false)
						widget.add(scroll).expand().fill()

						widget.setFillParent(true)
						Global.stage.addActor(widget)

						scroll.layout()
						scroll.scrollTo(map.prefWidth/3, 0f, 1f, 1f, true, true)
						scroll.act(1f)

						closeButton.addClickListener({ widget.remove(); closeButton.remove() })
						Global.stage.addActor(closeButton)
						closeButton.setPosition(Global.stage.width - 50, Global.stage.height - 50)
					})
				}
				else if (isOnPath(ix, iy))
				{
					moveTo(ix, iy, { town.save() })
				}
			}
		})

		renderer = SortedRenderer(tileSize, tilesWidth.toFloat(), tilesHeight.toFloat(), 2)
	}

	fun moveTo(x: Int, y: Int, arrivalFun: (() -> Unit)? = null)
	{
		val oldPos = Point.obtain().set(playerPos)
		playerPos.set(x, y)

		val cx = tilesWidth / 2

		if ((oldPos.x == cx || oldPos.x == cx-1) && (x == cx || x == cx-1))
		{
			val path = arrayOf(Vector2((oldPos.x - x).toFloat(), (oldPos.y - y).toFloat()), Vector2())

			var dst = 0f
			path.forEachIndexed { i, p -> if (i > 0) dst += path[i-1].dst(p) }

			playerSprite.animation = MoveAnimation.obtain().set(dst * 0.2f, UnsmoothedPath(path))

			if (arrivalFun != null) Future.call(arrivalFun, dst * 0.2f + 0.3f, this)
		}
		else if (oldPos.y != y)
		{
			// path to center, then on the y, then to the x
			val sx = if (oldPos.x < cx) cx-1 else cx
			val ex = if (playerPos.x < cx) cx-1 else cx

			val path = arrayOf(oldPos, Point(sx, oldPos.y), Point(ex, playerPos.y), playerPos.copy())

			var dst = 0f
			path.forEachIndexed { i, p -> if (i > 0) dst += path[i-1].euclideanDist(p) }

			playerSprite.animation = MoveAnimation.obtain().set(dst * 0.2f, path)

			if (arrivalFun != null) Future.call(arrivalFun, dst * 0.2f + 0.3f, this)
		}
		else
		{
			val path = arrayOf(Vector2((oldPos.x - x).toFloat(), 0f), Vector2())

			var dst = 0f
			path.forEachIndexed { i, p -> if (i > 0) dst += path[i-1].dst(p) }

			playerSprite.animation = MoveAnimation.obtain().set(dst * 0.2f, UnsmoothedPath(path))

			if (arrivalFun != null) Future.call(arrivalFun, dst * 0.2f + 0.3f, this)
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

	override fun act(delta: Float)
	{
		super.act(delta)

		if (playerSprite.fileName != player.chosenSprite.fileName)
		{
			playerSprite = player.chosenSprite.copy()
		}

		playerSprite.update(delta)
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		super.draw(batch, parentAlpha)

		var offsetx = (x + width / 2) - playerPos.x * tileSize - tileSize*0.5f
		var offsety = (y + height / 2) - playerPos.y * tileSize - tileSize*0.5f

		if (playerSprite.animation != null)
		{
			val offset = playerSprite.animation!!.renderOffset()!!
			offsetx -= offset[0] * tileSize
			offsety -= offset[1] * tileSize
		}

		renderer.begin(Gdx.app.graphics.deltaTime, offsetx, offsety)

		// draw grass

		for (x in -tilesWidth..tilesWidth*2)
		{
			for (y in -tilesHeight..tilesHeight*2)
			{
				renderer.queueSprite(grass, x.toFloat(), y.toFloat(), 0, 0)
			}
		}

		// draw path
		for (y in 1..tilesHeight*2)
		{
			renderer.queueSprite(path, 6f, y.toFloat(), 0, 1)
			renderer.queueSprite(path, 7f, y.toFloat(), 0, 1)
		}

		gate.size[0] = 4
		gate.size[1] = 4
		renderer.queueSprite(gate, 5f, tilesHeight - 5f, 1, 0)

		for (i in 0..town.houses.size-1)
		{
			val house = town.houses[i]
			val x = 1f + 8f * (i % 2)
			val y = tilesHeight - (9f + 6f * (i / 2)) - 1

			val sprite = if (house.unlocked) house.sprite else emptyHouse

			sprite.size[0] = 4
			sprite.size[1] = 4

			renderer.queueSprite(sprite, x, y, 1, 0)

			val left = (i % 2) == 0

			if (left)
			{
				for (px in x.toInt()..(tilesWidth/2))
				{
					renderer.queueSprite(path, px.toFloat(), y - 1f, 0, 1)
				}

				renderer.queueSprite(path, 2f, y, 0, 1)
				renderer.queueSprite(path, 3f, y, 0, 1)
			}
			else
			{
				for (px in (tilesWidth/2)..tilesWidth-3)
				{
					renderer.queueSprite(path, px.toFloat(), y - 1f, 0, 1)
				}

				renderer.queueSprite(path, tilesWidth - 4f, y, 0, 1)
				renderer.queueSprite(path, tilesWidth - 3f, y, 0, 1)
			}
		}

		renderer.flush(batch as SpriteBatch)
		playerSprite.render(batch, x + width / 2 - tileSize * 0.5f, y + height / 2 - tileSize * 0.2f, tileSize, tileSize)
	}
}