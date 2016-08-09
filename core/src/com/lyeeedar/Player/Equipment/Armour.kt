package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 09-Aug-16.
 */

class Armour : Equipment()
{
	var maxHP: Int = 0
	var regen: Int = 0

	override fun parse(xml: XmlReader.Element)
	{
		maxHP = xml.getInt("MaxHP")
		regen = xml.getInt("Regen")
	}

	override fun stats(): String = "Max HP: $maxHP\nRegen: $regen"
}