package com.lyeeedar.Board.CompletionAction

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Board.Grid
import com.lyeeedar.Player.Player

/**
 * Created by Philip on 28-Jul-16.
 */

abstract class AbstractCompletionAction
{
	abstract fun apply(player: Player)
	abstract fun parse(xml: XmlReader.Element)

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractCompletionAction
		{
			val obj = get(xml.name)
			obj.parse(xml)

			return obj
		}

		private fun get(name: String): AbstractCompletionAction
		{
			val uname = name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.newInstance(c)

			return instance
		}

		private fun getClass(name: String): Class<out AbstractCompletionAction>
		{
			val type = when(name) {
				"HEALTH", "HEAL", "HARM", "HURT" -> CompletionActionHealth::class.java
				"MONEY" -> CompletionActionMoney::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid completion action type: $name")
			}

			return type
		}
	}
}
