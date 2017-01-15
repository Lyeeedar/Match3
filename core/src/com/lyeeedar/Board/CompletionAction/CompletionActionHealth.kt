package com.lyeeedar.Board.CompletionAction

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Player.Player

/**
 * Created by Philip on 29-Jul-16.
 */

class CompletionActionHealth() : AbstractCompletionAction()
{
	var change: Int = 0

	override fun apply(player: Player)
	{
		player.hp += change
	}

	override fun parse(xml: XmlReader.Element)
	{
		change = xml.text.toInt()
	}
}