package com.lyeeedar.Player

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.MessageBox
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.set

/**
 * Created by Philip on 06-Aug-16.
 */

class PlayerData
{
	val unlockedSprites = com.badlogic.gdx.utils.Array<Sprite>()
	lateinit var chosenSprite: Sprite
	var maxHP = 10
	var maxPower = 10
	val abilities = Array<String?>(4){e -> null}
	var gold = 200
	val inventory = ObjectMap<String, Item>()

	init
	{
		unlockedSprites.add(AssetManager.loadSprite("Oryx/Custom/heroes/farmer_m", drawActualSize = true))
		unlockedSprites.add(AssetManager.loadSprite("Oryx/Custom/heroes/farmer_f", drawActualSize = true))

		abilities[0] = "Firebolt"

		chosenSprite = unlockedSprites[0]
	}

	val trees = ObjectMap<String, SkillTree>()

	fun getSkillTree(path: String): SkillTree
	{
		if (trees.containsKey(path)) return trees[path]
		else
		{
			val tree= SkillTree.load(path)
			trees[path] = tree
			return tree
		}
	}

	fun getAbility(name: String): Ability?
	{
		for (tree in trees)
		{
			for (skill in tree.value.boughtDescendants())
			{
				if (skill.key == name)
				{
					return skill
				}
			}
		}

		return null
	}

	fun mergePlayerDataBack(player: Player)
	{
		var message = "Quest rewards:\n\n"

		gold += player.gold
		message += "Gold: +${player.gold}\n"

		for (item in player.inventory)
		{
			val existing = inventory[item.key]

			if (existing != null)
			{
				existing.count += item.value.count
			}
			else
			{
				inventory[item.key] = item.value
			}

			message += "${item.value.name}: +${item.value.count}\n"
		}

		MessageBox("Quest Complete", message, Pair("Okay", {}))
	}
}