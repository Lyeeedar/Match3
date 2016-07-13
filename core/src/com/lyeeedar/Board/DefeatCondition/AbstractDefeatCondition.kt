package com.lyeeedar.Board.DefeatCondition

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Board.Grid

/**
 * Created by Philip on 13-Jul-16.
 */

abstract class AbstractDefeatCondition
{
	abstract fun attachHandlers(grid: Grid)
	abstract fun isDefeated(): Boolean
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractDefeatCondition
		{
			val obj = get(xml.name)
			obj.parse(xml)

			return obj
		}

		private fun get(name: String): AbstractDefeatCondition
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		private fun getClass(name: String): Class<out AbstractDefeatCondition>
		{
			val type = when(name) {
				"TURN", "TURNS" -> DefeatConditionTurns::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid defeat type: $name")
			}

			return type
		}
	}
}