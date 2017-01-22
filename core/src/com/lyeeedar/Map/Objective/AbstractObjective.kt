package com.lyeeedar.Map.Objective

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.DungeonMapEntry
import com.lyeeedar.Board.Level

/**
 * Created by Philip on 29-Jul-16.
 */

abstract class AbstractObjective()
{
	lateinit var srcData: XmlReader.Element

	abstract fun getDescription(): String
	abstract fun getRequiredLevels(): Array<Level>
	abstract fun update(map: DungeonMap)
	abstract fun isCompleted(): Boolean
	abstract fun createDynamicTable(skin: Skin): Table
	abstract fun createStaticTable(skin: Skin): Table

	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractObjective
		{
			val obj = get(xml.name)
			obj.srcData = xml
			obj.parse(xml)

			return obj
		}

		private fun get(name: String): AbstractObjective
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		private fun getClass(name: String): Class<out AbstractObjective>
		{
			val type = when(name) {
				"EXPLORE" -> ObjectiveExplore::class.java
				"ENCOUNTER" -> ObjectiveEncounter::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid objective type: $name")
			}

			return type
		}
	}
}