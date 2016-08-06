package com.lyeeedar.Screens

import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Player
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Town.Town
import com.lyeeedar.UI.TownWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class TownScreen() : AbstractScreen()
{
	init
	{
		instance = this
	}

	val playerData = PlayerData()

	override fun create()
	{
		val widget = TownWidget(Town(), playerData)

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
