package com.lyeeedar.Player

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.lyeeedar.Board.Grid
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 15-Jul-16.
 */

class Player
{
	lateinit var sprite: Sprite

	var hp: Int = 25
		set(value)
		{
			field = value

			if (field > maxhp)
			{
				field = maxhp
			}
			if (field < 0)
			{
				field = 0
			}

			hpBar.value = field.toFloat()
		}

	var maxhp: Int = 25

	var power: Int = 0
		set(value)
		{
			field = value
			if (field > maxpower)
			{
				field = maxpower
			}
			if (field < 0)
			{
				field = 0
			}

			powerBar.value = field.toFloat()
		}

	var maxpower: Int = 50

	// abilities and stuff

	lateinit var portrait: SpriteWidget
	lateinit var hpBar: ProgressBar
	lateinit var powerBar: ProgressBar

	fun attachHandlers(grid: Grid)
	{

	}

	fun createTable(skin: Skin) : Table
	{
		val table = Table()

		hpBar = ProgressBar(0f, maxhp.toFloat(), 1f, false, skin)
		hpBar.value = hp.toFloat()
		hpBar.color = Color.FOREST
		powerBar = ProgressBar(0f, maxpower.toFloat(), 1f, false, skin)
		powerBar.value = power.toFloat()
		powerBar.color = Color.CYAN

		portrait = SpriteWidget(sprite, 32, 32)

		val barTable = Table()
		barTable.add(hpBar)
		barTable.row()
		barTable.add(powerBar)

		table.add(portrait)
		table.add(barTable).expand()

		return table
	}
}