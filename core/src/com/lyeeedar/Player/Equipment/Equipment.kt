package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.UI.Unlockable
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.set

/**
 * Created by Philip on 09-Aug-16.
 */

class Equipment : Unlockable()
{
	enum class EquipmentSlot
	{
		WEAPON,
		ARMOUR,
		CHARM
	}

	private val StatNames = arrayOf( "MatchDam", "AbilityDam", "MaxHP", "Regen", "MaxPower", "PowerBonus" )

	lateinit var slot: EquipmentSlot
	val stats = ObjectMap<String, Int>()
	var specialEffect: Sprite? = null

	fun get(key: String) : Int? = stats[key]

	override fun stats(): String?
	{
		var s = ""
		for (stat in stats) s += "${stat.key}: ${stat.value}\n"
		return s
	}

	override fun parse(xml: XmlReader.Element, resources: ObjectMap<String, XmlReader.Element>)
	{
		val dataEl = xml.getChildByName("UnlockableData")

		slot = EquipmentSlot.valueOf(dataEl.get("Slot", "Weapon").toUpperCase())
		for (stat in StatNames)
		{
			val el = dataEl.getChildByName(stat)
			if (el != null) stats[stat] = el.text.toInt()
			else stats[stat] = 0
		}

		val specialEl = dataEl.getChildByName("Sprite")
		if (specialEl != null)
		{
			specialEffect = AssetManager.tryLoadSpriteWithResources(specialEl, resources)
		}
	}

}