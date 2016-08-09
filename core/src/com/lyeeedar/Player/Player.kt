package com.lyeeedar.Player

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Grid
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Player.Equipment.Equipment
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 15-Jul-16.
 */

class Player(data: PlayerData)
{
	lateinit var portrait: Sprite

	lateinit var specialHitEffect: Sprite

	var hp: Int = 0
		set(value)
		{
			field = value
			if (field < 0) field = 0
			if (field > maxhp) field = maxhp
		}

	var physDam: Int = 0
	var spellDam: Int = 0

	var maxhp: Int = 0
	var regen: Int = 0

	var startpower: Int = 0
	var maxpower: Int = 0

	var gold: Int = 0
	val inventory = ObjectMap<String, Item>()

	// abilities and stuff
	val abilities: Array<Ability?> = Array(4){e -> null}
	val equipment = Array<Equipment?>(4){ e -> null}

	init
	{
		portrait = data.chosenSprite
		portrait.drawActualSize = false

		physDam = data.physDam
		spellDam = data.spellDam

		maxhp = data.maxHP
		hp = maxhp
		regen = data.regen

		maxpower = data.maxPower
		startpower = data.startPower

		specialHitEffect = data.specialHitEffect.copy()

		for (i in 0..3)
		{
			abilities[i] = if (data.abilities[i] != null) data.getAbility(data.abilities[i]!!) else null
			equipment[i] = data.equipment[i]
		}
	}
}