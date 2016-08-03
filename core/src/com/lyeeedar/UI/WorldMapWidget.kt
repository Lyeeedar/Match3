package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Map.World
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class WorldMapWidget(val world: World) : Widget()
{
	val white = AssetManager.loadTextureRegion("Sprites/white.png")
	val notSelected = AssetManager.loadTextureRegion("Sprites/Oryx/uf_split/uf_map/uf_map_263.png")!!

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
					if (dungeon.location.dist(x.toInt(), y.toInt()) < 25)
					{
						// select this dungeon
						DungeonDescriptionWidget(dungeon, this@WorldMapWidget)
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
			batch.draw(notSelected, x+dungeon.location.x-notSelected.regionWidth/2, y+dungeon.location.y-notSelected.regionHeight/2)
		}
	}
}

