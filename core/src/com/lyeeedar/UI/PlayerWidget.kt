package com.lyeeedar.UI

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.lyeeedar.Global
import com.lyeeedar.Player.Player

/**
 * Created by Philip on 29-Jul-16.
 */

class PlayerWidget(val player: Player): Table()
{
	lateinit var hpLabel: Label
	lateinit var moneyLabel: Label

	init
	{
		instance = this

		val sprite = SpriteWidget(player.portrait.copy(), 64, 64)
		hpLabel = Label("${player.hp}/${player.maxhp}", Global.skin)
		moneyLabel = Label("${player.gold} coins", Global.skin)

		val abilityBar = Table()
		for (ability in player.abilities)
		{
			if (ability == null) continue

			val s = SpriteWidget(ability.icon.copy(), 24, 24)
			abilityBar.add(s)
		}

		val rightTable = Table()

		this.add(sprite)
		this.add(rightTable).expand().fill()

		rightTable.add(abilityBar)
		rightTable.row()
		rightTable.add(hpLabel)
		rightTable.row()
		rightTable.add(moneyLabel)
	}

	override fun act(delta: Float)
	{
		super.act(delta)

		hpLabel.setText("${player.hp}/${player.maxhp}")
		moneyLabel.setText("${player.gold} coins")
	}

	companion object
	{
		lateinit var instance: PlayerWidget
	}
}