package com.lyeeedar.Screens

import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.Map.Generators.HubGenerator
import com.lyeeedar.Player.Player
import com.lyeeedar.UI.DungeonMapWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 25-Jul-16.
 */

class MapScreen(): AbstractScreen()
{
	override fun create()
	{
		Global.stage = stage

		val generator = HubGenerator()
		val map = generator.generate()
		map.theme = LevelTheme.load("Dungeon")
		val player = Player()
		player.portrait = AssetManager.loadSprite("Oryx/Custom/heroes/Merc")

		val widget = DungeonMapWidget(map, player)

		mainTable.add(widget).expand().fill()
	}

	override fun doRender(delta: Float)
	{

	}
}