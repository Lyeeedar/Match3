package com.lyeeedar.Player

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Grid
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 15-Jul-16.
 */

class Player(data: PlayerData)
{
	lateinit var portrait: Sprite

	var hp: Int = 25
	var maxhp: Int = 25

	var startpower: Int = 0
	var maxpower: Int = 50

	var gold: Int = 0
	val inventory = ObjectMap<String, Item>()

	// abilities and stuff
	val abilities: Array<Ability?> = Array(4){e -> null}

	init
	{
		portrait = data.chosenSprite
		portrait.drawActualSize = false

		maxhp = data.maxHP
		hp = maxhp

		maxpower = data.maxPower
		startpower = 0

		for (i in 0..3)
		{
			abilities[i] = if (data.abilities[i] != null) data.getAbility(data.abilities[i]!!) else null
		}
	}
}