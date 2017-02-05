package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Global
import com.lyeeedar.Map.World
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class WorldMapWidget(val world: World, val playerData: PlayerData, val parent: Actor, val closeButton: Actor) : Widget()
{
	val white = AssetManager.loadTextureRegion("Sprites/white.png")
	val uncompleted = AssetManager.loadTextureRegion("Sprites/Oryx/uf_split/uf_map/uf_map_263.png")!!
	val completed = AssetManager.loadTextureRegion("Sprites/Oryx/uf_split/uf_map/uf_map_287.png")!!
	val path = AssetManager.loadTextureRegion("Sprites/Oryx/uf_split/uf_map/uf_map_289.png")!!
	val pathCol = Color(1f, 1f, 1f, 0.6f)

	val backCol = Color(56f / 255f, 91f / 255f, 129f / 255f, 1f)

	override fun getPrefWidth(): Float = world.mapImage.width.toFloat()
	override fun getPrefHeight(): Float = world.mapImage.height.toFloat()

	init
	{
		touchable = Touchable.enabled

		addListener(object : ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				super.clicked(event, x, y)

				for (dungeon in world.dungeons)
				{
					if ((dungeon.isUnlocked(world) || !Global.release) && dungeon.location.dist(x.toInt(), y.toInt()) < 25)
					{
						// select this dungeon
						DungeonDescriptionWidget(dungeon, playerData, parent, closeButton)
					}
				}
			}
		})
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		super.draw(batch!!, parentAlpha)

		batch.color = backCol
		batch.draw(white, 0f, 0f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
		batch.color = Color.WHITE

		batch.draw(world.mapImage, x, y)

		for (dungeon in world.dungeons)
		{
			if (dungeon.isUnlocked(world) || !Global.release)
			{
				val sprite = if (dungeon.isCompleted(world)) completed else uncompleted
				batch.draw(sprite, x + dungeon.location.x - sprite.regionWidth / 2f, y + dungeon.location.y - sprite.regionHeight / 2f)

				if (dungeon.unlockedBy != null)
				{
					val prev = dungeon.unlockedBy!!
					val dst = dungeon.location.euclideanDist(prev.location).toInt()
					val hsize = sprite.regionWidth / 2

					batch.color = pathCol

					for (i in hsize+5..dst-hsize step 15)
					{
						val pos = prev.location.lerp(dungeon.location, i.toFloat() / dst.toFloat())
						batch.draw(path, x + pos.x - path.regionWidth / 4f, y + pos.y - path.regionHeight / 4f, path.regionWidth / 2f, path.regionHeight / 2f)
					}

					batch.color = Color.WHITE
				}
			}
		}
	}
}

