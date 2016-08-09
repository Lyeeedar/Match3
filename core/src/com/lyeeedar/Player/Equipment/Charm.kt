package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 09-Aug-16.
 */

class Charm : Equipment()
{
	var maxPower: Int = 0
	var startPower: Int = 0

	override fun parse(xml: XmlReader.Element)
	{
		maxPower = xml.getInt("MaxPower")
		startPower = xml.getInt("StartPower")
	}

	override fun stats(): String = "Max Power: $maxPower\nStart Power: $startPower"
}