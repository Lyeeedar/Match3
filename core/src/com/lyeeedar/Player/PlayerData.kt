package com.lyeeedar.Player

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Equipment.Equipment
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.MessageBox
import com.lyeeedar.UI.UnlockTree
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.set

/**
 * Created by Philip on 06-Aug-16.
 */

class PlayerData
{
	val unlockedSprites = com.badlogic.gdx.utils.Array<Sprite>()
	lateinit var chosenSprite: Sprite

	private val defaultHitEffect = AssetManager.loadSprite("EffectSprites/Impact/Impact", updateTime = 0.1f)
	val specialHitEffect: Sprite
		get() = getEquipment(Equipment.EquipmentSlot.WEAPON)?.specialEffect ?: defaultHitEffect

	private var baseAbilityDam = 0
	val abilityDam: Int
		get() = baseAbilityDam + (getEquipment(Equipment.EquipmentSlot.WEAPON)?.get("AbilityDam") ?: 0)

	private var baseMatchDam = 0
	val matchDam: Int
		get() = baseMatchDam + (getEquipment(Equipment.EquipmentSlot.WEAPON)?.get("MatchDam") ?: 0)

	private var baseMaxHP = 10
	val maxHP: Int
		get() = baseMaxHP + (getEquipment(Equipment.EquipmentSlot.ARMOUR)?.get("MaxHP") ?: 0)

	private var baseRegen = 0
	val regen: Int
		get() = baseRegen + (getEquipment(Equipment.EquipmentSlot.ARMOUR)?.get("Regen") ?: 0)

	private var baseMaxPower = 10
	val maxPower: Int
		get() = baseMaxPower + (getEquipment(Equipment.EquipmentSlot.CHARM)?.get("MaxPower") ?: 0)

	private var baseStartPower = 0
	val startPower: Int
		get() = baseStartPower + (getEquipment(Equipment.EquipmentSlot.CHARM)?.get("StartPower") ?: 0)

	val equipment = Array<String?>(Equipment.EquipmentSlot.values().size){ e -> null}
	val abilities = Array<String?>(4){e -> null}
	var gold = 200
	val inventory = ObjectMap<String, Item>()

	val equipTree = UnlockTree.load("UnlockTrees/Equipment", {Equipment()})
	val skillTrees = ObjectMap<String, UnlockTree<Ability>>()

	init
	{
		unlockedSprites.add(AssetManager.loadSprite("Oryx/Custom/heroes/farmer_m", drawActualSize = true))
		unlockedSprites.add(AssetManager.loadSprite("Oryx/Custom/heroes/farmer_f", drawActualSize = true))

		chosenSprite = unlockedSprites[0]
	}

	fun getSkillTree(path: String): UnlockTree<Ability>
	{
		if (skillTrees.containsKey(path)) return skillTrees[path]
		else
		{
			val tree = UnlockTree.load(path, {Ability()})
			skillTrees[path] = tree
			return tree
		}
	}

	fun getEquipment(slot: Equipment.EquipmentSlot) = if(equipment[slot.ordinal] != null) getEquipment(equipment[slot.ordinal]!!) else null
	fun getEquipment(name: String): Equipment?
	{
		for (equip in equipTree.boughtDescendants())
		{
			if (equip.key == name)
			{
				return equip
			}
		}

		return null
	}

	fun setEquipment(key: String?, slot: Equipment.EquipmentSlot) { equipment[slot.ordinal] = key }

	fun getAbility(name: String): Ability?
	{
		for (tree in skillTrees)
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