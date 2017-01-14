package com.lyeeedar.Screens

import com.lyeeedar.Player.*
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Town.Town
import com.lyeeedar.UI.TownWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class TownScreen(val playerData: PlayerData, val town: Town) : AbstractScreen()
{
	init
	{
		instance = this
	}

	override fun create()
	{
		val widget = TownWidget(town, playerData)

		mainTable.add(widget).expand().fill()
	}

	override fun doRender(delta: Float)
	{

	}

	companion object
	{
		lateinit var instance: TownScreen
	}
}
