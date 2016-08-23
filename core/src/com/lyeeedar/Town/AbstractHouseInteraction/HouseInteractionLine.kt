package com.lyeeedar.Town.AbstractHouseInteraction

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Town.House
import com.lyeeedar.UI.FullscreenMessage

/**
 * Created by Philip on 11-Aug-16.
 */

class HouseInteractionLine : AbstractHouseInteraction()
{
	lateinit var line: String

	override fun apply(house: House, playerData: PlayerData)
	{
		val message = FullscreenMessage(line, "", {})
		message.onClosed += { house.advance(playerData) }
		message.show()
	}

	override fun parse(xml: XmlReader.Element)
	{
		line = xml.get("Text")
	}
}