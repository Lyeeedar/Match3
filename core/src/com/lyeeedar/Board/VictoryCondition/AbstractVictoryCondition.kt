package com.lyeeedar.Board.VictoryCondition

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Board.Grid

/**
 * Created by Philip on 13-Jul-16.
 */

abstract class AbstractVictoryCondition
{
	abstract fun attachHandlers(grid: Grid)
	abstract fun isVictory(): Boolean
	abstract fun parse(xml: XmlReader.Element)
	abstract fun createTable(skin: Skin): Table
	abstract fun getTextDescription(): String

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractVictoryCondition
		{
			val obj = get(xml.name)
			obj.parse(xml)

			return obj
		}

		private fun get(name: String): AbstractVictoryCondition
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		private fun getClass(name: String): Class<out AbstractVictoryCondition>
		{
			val type = when(name) {
				"MATCH", "MATCHES" -> VictoryConditionMatches::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid victory type: $name")
			}

			return type
		}
	}
}