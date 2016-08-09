package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 09-Aug-16.
 */

class Weapon : Equipment()
{
	var specialEffect: Sprite? = null
	var abilityDam: Int = 0
	var matchDam: Int = 0

	override fun parse(xml: XmlReader.Element)
	{
		val effectEl = xml.getChildByName("Sprite")
		specialEffect = if (effectEl != null) AssetManager.loadSprite(effectEl) else null
		matchDam = xml.getInt("MatchDam")
		abilityDam = xml.getInt("AbilityDam")
	}

	override fun stats(): String = "Match Damage: $matchDam\nAbility Damage: $abilityDam"
}