package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.set

/**
 * Created by Philip on 01-Aug-16.
 */

class Faction
{
	val size1 = Array<MonsterDesc>()
	var size2 = Array<MonsterDesc>()

	var bosses = Array<MonsterDesc>()

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

				if (desc.size == 1)
				{
					faction.size1.add(desc)
				}
				else
				{
					faction.size2.add(desc)
				}
			}

			return faction
		}
	}
}

class MonsterDesc
{
	lateinit var sprite: Sprite
	lateinit var death: Sprite
	var attackDelay: Float = 5f
	var attackSpeed: Int = 6
	var size: Int = 1
	var hp: Int = 25
	val rewards = ObjectMap<String, Int>()

	companion object
	{
		fun load(xml: XmlReader.Element): MonsterDesc
		{
			val desc = MonsterDesc()

			desc.sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"))
			desc.death = AssetManager.loadSprite(xml.getChildByName("Death"))

			desc.attackDelay = xml.getFloat("AttackDelay")
			desc.attackSpeed = xml.getInt("AttackSpeed")

			desc.size = xml.getInt("Size")

			desc.hp = xml.getInt("HP")

			val rewardsEl = xml.getChildByName("Reward")
			for (i in 0..rewardsEl.childCount-1)
			{
				val el = rewardsEl.getChild(i)
				desc.rewards[el.name] = el.text.toInt()
			}

			return desc
		}
	}
}