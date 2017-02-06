package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import ktx.collections.get
import ktx.collections.set

/**
 * Created by Philip on 01-Aug-16.
 */

class Faction
{
	val sizeMap = IntMap<Array<MonsterDesc>>()

	fun get(size: Int) : MonsterDesc
	{
		var s = size
		while (s > 0)
		{
			if (sizeMap.containsKey(s)) return sizeMap[s].random()
			s--
		}

		return sizeMap.values().first().random()
	}

	fun get(name: String): MonsterDesc?
	{
		for (size in sizeMap)
		{
			for (monster in size.value)
			{
				if (monster.name == name) return monster
			}
		}

		return null
	}

	companion object
	{
		fun load(path: String): Faction
		{
			val xml = XmlReader().parse(Gdx.files.internal("Factions/$path.xml"))

			val faction = Faction()

			val monsterEl = xml.getChildByName("Monsters")
			for (i in 0..monsterEl.childCount-1)
			{
				val el = monsterEl.getChild(i)
				val desc = MonsterDesc.load(el)

				if (!faction.sizeMap.containsKey(desc.size))
				{
					faction.sizeMap[desc.size] = Array()
				}

				faction.sizeMap[desc.size].add(desc)
			}

			return faction
		}
	}
}

class MonsterDesc
{
	lateinit var name: String
	lateinit var sprite: Sprite
	lateinit var death: Sprite
	var attackDelay: Int = 5
	var attackSpeed: Int = 6
	var size: Int = 1
	var hp: Int = 25
	val rewards = ObjectMap<String, Pair<Int, Int>>()
	val abilities = Array<MonsterAbility>()

	companion object
	{
		fun load(xml: XmlReader.Element): MonsterDesc
		{
			val desc = MonsterDesc()

			desc.name = xml.get("Name", "")

			desc.sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"))
			desc.death = AssetManager.loadSprite(xml.getChildByName("Death"))

			desc.attackDelay = xml.getInt("AttackDelay")
			desc.attackSpeed = xml.getInt("AttackSpeed")

			desc.size = xml.getInt("Size", 1)

			desc.hp = xml.getInt("HP", 10)

			val rewardsEl = xml.getChildByName("Rewards")
			if (rewardsEl != null)
			{
				for (i in 0..rewardsEl.childCount - 1)
				{
					val el = rewardsEl.getChild(i)
					val text = el.text
					val split = text.split(",")

					desc.rewards[split[0]] = Pair<Int, Int>(split[1].toInt(), split[2].toInt())
				}
			}

			val abilitiesEl = xml.getChildByName("Abilities")
			if (abilitiesEl != null)
			{
				for (i in 0..abilitiesEl.childCount-1)
				{
					val el = abilitiesEl.getChild(i)
					val ability = MonsterAbility.load(el)
					desc.abilities.add(ability)
				}
			}

			return desc
		}
	}
}