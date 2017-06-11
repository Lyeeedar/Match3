package com.lyeeedar.Map

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Map.Objective.AbstractObjective
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import ktx.collections.toGdxArray

class World
{
	var mapImage: Texture
	val dungeons: Array<WorldDungeon> = Array()

	operator fun get(name: String): WorldDungeon = dungeons.first { it.name == name }

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

		for (dungeon in dungeons.toGdxArray())
		{
			if (dungeon.unlockedByName != "")
			{
				dungeon.unlockedBy = this[dungeon.unlockedByName]
			}
		}
	}
}

class WorldDungeon
{
	lateinit var name: String
	lateinit var description: String
	lateinit var theme: String
	val location: Point = Point()
	lateinit var unlockedByName: String
	var unlockedBy: WorldDungeon? = null
	val progressionQuests = Array<XmlReader.Element>()
	val loopQuests = Array<XmlReader.Element>()
	var progression: Int = 0

	fun getObjective(): AbstractObjective
	{
		if (progression >= progressionQuests.size) return AbstractObjective.load(loopQuests.random())
		else return AbstractObjective.load(progressionQuests[progression])
	}

	fun isCompleted(world: World) = isUnlocked(world) && progression >= progressionQuests.size

	fun isUnlocked(world: World): Boolean
	{
		if (unlockedBy == null) return true
		return unlockedBy!!.isCompleted(world)
	}

	companion object
	{
		fun load(xml: XmlReader.Element): WorldDungeon
		{
			val dungeon = WorldDungeon()

			dungeon.name = xml.get("Name")
			dungeon.description = xml.get("Description")
			dungeon.theme = xml.get("Theme")
			dungeon.location.set(xml.get("Location"))
			dungeon.unlockedByName = xml.get("UnlockedBy", "")

			val questsEl = xml.getChildByName("QuestOrder")
			for (i in 0..questsEl.childCount-1)
			{
				dungeon.progressionQuests.add(questsEl.getChild(i))
			}

			val questsCompEl = xml.getChildByName("LoopQuests")
			for (i in 0..questsCompEl.childCount-1)
			{
				dungeon.loopQuests.add(questsCompEl.getChild(i))
			}

			return dungeon
		}
	}
}