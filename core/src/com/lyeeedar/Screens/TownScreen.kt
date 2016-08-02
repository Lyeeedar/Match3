package com.lyeeedar.Screens

import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Player
import com.lyeeedar.Town.Town
import com.lyeeedar.UI.TownWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class TownScreen() : AbstractScreen()
{
	override fun create()
	{
		val player = Player()
		player.abilities[0] = Ability(icon = AssetManager.loadSprite("Icons/Action"), cost = 1, elite = false)
		player.portrait = AssetManager.loadSprite("Oryx/Custom/heroes/Merc", drawActualSize = true)

		val widget = TownWidget(Town(), player)

		mainTable.add(widget).expand().fill()
	}

	override fun doRender(delta: Float)
	{

	}
}
