package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Map.DungeonMapEntry
import com.lyeeedar.Sprite.DirectedSprite
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.set

/**
 * Created by Philip on 13-Jul-16.
 */

class LevelTheme
{
	lateinit var name: String

	lateinit var floor: SpriteWrapper
	lateinit var wall: SpriteWrapper
	lateinit var pit: SpriteWrapper

	lateinit var mapRoom: DirectedSprite
	lateinit var mapCorridor: DirectedSprite

	val allowedFactions = Array<String>()
	val roomWeights = FastEnumMap<DungeonMapEntry.Type, ObjectMap<String, Int>>(DungeonMapEntry.Type::class.java)

	companion object
	{
		fun load(path: String): LevelTheme
		{
			val xml = XmlReader().parse(Gdx.files.internal("Themes/$path.xml"))

			val theme = LevelTheme()
			theme.name = path

			theme.floor = SpriteWrapper.load(xml.getChildByName("Floor"))
			theme.wall = SpriteWrapper.load(xml.getChildByName("Wall"))
			theme.pit = SpriteWrapper.load(xml.getChildByName("Pit"))

			val mapEl = xml.getChildByName("Map")
			theme.mapRoom = DirectedSprite.load(mapEl.getChildByName("Room").getChild(0))
			theme.mapCorridor = DirectedSprite.load(mapEl.getChildByName("Corridor").getChild(0))

			val factionsEl = xml.getChildByName("AllowedFactions")
			for (i in 0..factionsEl.childCount-1)
			{
				val el = factionsEl.getChild(i)
				theme.allowedFactions.add(el.name)
			}

			val weightsEl = xml.getChildByName("RoomWeights")
			for (i in 0..weightsEl.childCount-1)
			{
				val typeEl = weightsEl.getChild(i)
				val type = DungeonMapEntry.Type.valueOf(typeEl.name.toUpperCase())

				theme.roomWeights[type] = ObjectMap()
				val map = theme.roomWeights[type]

				for (ii in 0..typeEl.childCount-1)
				{
					val el = typeEl.getChild(ii)
					map[el.name] = el.text.toInt()
				}
			}

			return theme
		}
	}
}