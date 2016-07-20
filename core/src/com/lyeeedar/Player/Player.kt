package com.lyeeedar.Player

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.lyeeedar.Board.Grid
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 15-Jul-16.
 */

class Player
{
	lateinit var portrait: Sprite

	var hp: Int = 25
	var maxhp: Int = 25

	var startpower: Int = 0
	var maxpower: Int = 50

	// abilities and stuff
	val abilities: Array<Ability?> = Array(4){e -> null}
}