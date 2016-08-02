package com.lyeeedar.Map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point

class World
{
	lateinit var mapImage: Texture
	val dungeons: Array<WorldDungeon> = Array()

	init
	{
		val xml = XmlReader().parse(Gdx.files.internal("World/Dungeons.xml"))

		mapImage = AssetManager.loadTexture(xml.get("MapImage"))!!

		val dungeonsEl = xml.getChildByName("Dungeons")
		for (i in 0.. dungeonsEl.childCount-1)
		{
			val dungeonEl = dungeonsEl.getChild(i)
			val dungeon = WorldDungeon.load(dungeonEl)
			dungeons.add(dungeon)
		}
	}
}

class WorldDungeon
{
	lateinit var name: String
	lateinit var description: String
	lateinit var theme: String
	val location: Point = Point()
	var unlocked: Boolean = false

	companion object
	{
		fun load(xml: XmlReader.Element): WorldDungeon
		{
			val dungeon = WorldDungeon()

			dungeon.name = xml.get("Name")
			dungeon.description = xml.get("Description")
			dungeon.theme = xml.get("Theme")
			dungeon.location.set(xml.get("Location"))
			dungeon.unlocked = xml.getBoolean("UnlockedAtStart", false)

			return dungeon
		}
	}
}