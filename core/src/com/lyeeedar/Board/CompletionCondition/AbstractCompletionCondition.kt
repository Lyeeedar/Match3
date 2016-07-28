package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Board.CompletionCondition.CompletionConditionDeath
import com.lyeeedar.Board.CompletionCondition.CompletionConditionTime
import com.lyeeedar.Board.CompletionCondition.CompletionConditionTurns
import com.lyeeedar.Board.Grid

abstract class AbstractCompletionCondition
{
	abstract fun attachHandlers(grid: Grid)
	abstract fun isCompleted(): Boolean
	abstract fun parse(xml: XmlReader.Element)
	abstract fun createTable(skin: Skin): Table
	abstract fun getTextDescription(): String

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractCompletionCondition
		{
			val obj = get(xml.name)
			obj.parse(xml)

			return obj
		}

		private fun get(name: String): AbstractCompletionCondition
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		private fun getClass(name: String): Class<out AbstractCompletionCondition>
		{
			val type = when(name) {
				// Defeat
				"TURN", "TURNS" -> CompletionConditionTurns::class.java
				"TIME" -> CompletionConditionTime::class.java
				"DEATH" -> CompletionConditionDeath::class.java

				// Victory
				"KILL" -> CompletionConditionKill::class.java
				"LOOT" -> CompletionConditionLoot::class.java
				"MATCH", "MATCHES" -> CompletionConditionMatches::class.java
				"SINK" -> CompletionConditionSink::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid completion condition type: $name")
			}

			return type
		}
	}
}