package com.lyeeedar.Town.AbstractHouseInteraction

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Town.House

/**
 * Created by Philip on 11-Aug-16.
 */

abstract class AbstractHouseInteraction
{
	abstract fun apply(house: House, playerData: PlayerData)
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractHouseInteraction
		{
			val obj = get(xml.getAttribute("meta:RefKey"))
			obj.parse(xml)

			return obj
		}

		private fun get(name: String): AbstractHouseInteraction
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		private fun getClass(name: String): Class<out AbstractHouseInteraction>
		{
			val type = when(name) {
				"DIALOGUELINETEXT" -> HouseInteractionLine::class.java
				"DIALOGUELINETREE" -> HouseInteractionTree::class.java
				"DIALOGUELINEUPGRADE" -> HouseInteractionUpgrade::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid house action type: $name")
			}

			return type
		}
	}
}