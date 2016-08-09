package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 09-Aug-16.
 */

abstract class Equipment
{
	lateinit var icon: Sprite
	lateinit var name: String
	lateinit var description: String

	abstract fun parse(xml: XmlReader.Element)
	abstract fun createSimpleTable(): Table
	abstract fun createFullTable(): Table

	companion object
	{
		fun load(xml: XmlReader.Element): Equipment
		{
			val equip = when (xml.name.toUpperCase())
			{
				"WEAPON" -> Weapon()
				"ARMOUR" -> Armour()
				"CHARM" -> Charm()
				else -> throw Exception("Invalid equipment type ${xml.name}!")
			}

			equip.icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
			equip.name = xml.get("Name")
			equip.description = xml.get("Description")
			equip.parse(xml)

			return equip
		}
	}
}