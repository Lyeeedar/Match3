package com.lyeeedar.Player

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Player.Equipment.Armour
import com.lyeeedar.Player.Equipment.Charm
import com.lyeeedar.Player.Equipment.Equipment
import com.lyeeedar.Player.Equipment.Weapon
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

	private val defaultHitEffect = AssetManager.loadSprite("EffectSprites/Impact/Impact", updateTime = 0.1f)
	val specialHitEffect: Sprite
		get() = getEquipment<Weapon>()?.specialEffect ?: defaultHitEffect

	private var baseAbilityDam = 0
	val abilityDam: Int
		get() = baseAbilityDam + (getEquipment<Weapon>()?.abilityDam ?: 0)

	private var baseMatchDam = 0
	val matchDam: Int
		get() = baseMatchDam + (getEquipment<Weapon>()?.matchDam ?: 0)

	private var baseMaxHP = 10
	val maxHP: Int
		get() = baseMaxHP + (getEquipment<Armour>()?.maxHP ?: 0)

	private var baseRegen = 0
	val regen: Int
		get() = baseRegen + (getEquipment<Armour>()?.regen ?: 0)

	private var baseMaxPower = 10
	val maxPower: Int
		get() = baseMaxPower + (getEquipment<Charm>()?.maxPower ?: 0)

	private var baseStartPower = 0
	val startPower: Int
		get() = baseStartPower + (getEquipment<Charm>()?.startPower ?: 0)

	val equipment = Array<Equipment?>(4){e -> null}
	val abilities = Array<String?>(4){e -> null}
	var gold = 200
	val inventory = ObjectMap<String, Item>()
	val unlockedEquipment = com.badlogic.gdx.utils.Array<Equipment>()

	init
	{
		unlockedSprites.add(AssetManager.loadSprite("Oryx/Custom/heroes/farmer_m", drawActualSize = true))
		unlockedSprites.add(AssetManager.loadSprite("Oryx/Custom/heroes/farmer_f", drawActualSize = true))

		abilities[0] = "Firebolt"

		chosenSprite = unlockedSprites[0]

		val wep = Weapon()
		wep.name = "Lightning Rod"
		wep.description = "Crackling with lightning this rod provides a powerful channel for magics."
		wep.icon = AssetManager.loadSprite("Oryx/uf_split/uf_items/weapon_magic_staff_venom", drawActualSize = true)
		wep.specialEffect = AssetManager.loadSprite("EffectSprites/LightningBurst/LightningBurst", updateTime = 0.1f)
		wep.abilityDam = 5
		wep.matchDam = 1
		setEquipment(wep)
		unlockedEquipment.add(wep)
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

	fun setEquipment(equip: Equipment)
	{
		val index = when(equip)
		{
			is Weapon -> 0
			is Armour -> 1
			is Charm -> 2
			else -> 3
		}

		equipment[index] = equip
	}

	inline fun <reified T> clearEquipment()
	{
		val index = when(T::class)
		{
			Weapon::class -> 0
			Armour::class -> 1
			Charm::class -> 2
			else -> 3
		}

		equipment[index] = null
	}

	inline fun <reified T: Equipment> getEquipment(): T? =
			when(T::class)
			{
				Weapon::class -> equipment[0] as? T
				Armour::class -> equipment[1] as? T
				Charm::class -> equipment[2] as? T
				else -> equipment[3] as? T
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