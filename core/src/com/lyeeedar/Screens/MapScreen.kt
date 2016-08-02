package com.lyeeedar.Screens

import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.Generators.HubGenerator
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Player
import com.lyeeedar.UI.DungeonMapWidget
import com.lyeeedar.UI.PlayerWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 25-Jul-16.
 */

class MapScreen(): AbstractScreen()
{
	init
	{
		instance = this
	}

	override fun create()
	{

	}

	fun setMap(map: DungeonMap, player: Player)
	{
		if (!created){
			baseCreate()
			created = true
		}

		mainTable.clear()

		val dungeonWidget = DungeonMapWidget(map, player)
		val playerWidget = PlayerWidget(player)

		val stack = Stack()
		stack.add(dungeonWidget)

		val overlay = Table()
		overlay.defaults().pad(10f)
		overlay.add(map.objective.createTable(Global.skin)).expand().top().left()
		overlay.add(playerWidget).expand().bottom().left()
		stack.add(overlay)

		mainTable.add(stack).expand().fill()
	}

	override fun doRender(delta: Float)
	{

	}

	companion object
	{
		lateinit var instance: MapScreen
	}
}