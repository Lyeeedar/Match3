package com.lyeeedar.UI

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Player.Player
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Util.EnumBitflag
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 24-Jul-16.
 */

class DungeonMapWidget(val map: DungeonMap, val player: Player): Widget()
{
	init
	{
		instance = this
	}

	val renderer = SpriteRenderer()
	val bitflag = EnumBitflag<Direction>()

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		Global.tileSize = 64f

		super.draw(batch, parentAlpha)

		val offsetx = (x + width / 2) - map.playerPos.x * Global.tileSize
		val offsety = (y + height / 2) - map.playerPos.y * Global.tileSize

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
				renderer.queueSprite(sprite, entry.key.x.toFloat(), entry.key.y.toFloat(), offsetx, offsety, SpaceSlot.TILE, 0)
			}
		}

		renderer.flush(Gdx.app.graphics.deltaTime, batch as SpriteBatch)
	}

	companion object
	{
		lateinit var instance: DungeonMapWidget
	}
}