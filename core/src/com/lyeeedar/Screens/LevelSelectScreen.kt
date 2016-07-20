package com.lyeeedar.Screens

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.MainGame
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Player
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 18-Jul-16.
 */

class LevelSelectScreen(): AbstractScreen()
{
	// ----------------------------------------------------------------------
	init
	{
		instance = this
	}

	// ----------------------------------------------------------------------
	override fun create()
	{
		val player = Player()
		player.portrait = AssetManager.loadSprite("Oryx/Custom/heroes/Merc")
		player.abilities[0] = Ability(AssetManager.loadSprite("Icons/Aim"), 2, false)
		player.abilities[1] = Ability(AssetManager.loadSprite("Icons/Bash"), 6, false)

		val trapButton = TextButton("Create Trap Level", Global.skin)
		trapButton.addListener(object : ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				val theme = LevelTheme.load("Dungeon")
				val level = Level.load("Trap/Diamond", theme, Level.LevelType.TRAP)
				level.create()

				GridScreen.instance.updateLevel(level, player)
				Global.game.switchScreen(MainGame.ScreenEnum.GRID)
			}
		})

		val treasureButton = TextButton("Create Treasure Level", Global.skin)

		mainTable.add(trapButton)
		mainTable.row()
		mainTable.add(treasureButton)
	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{

	}

	// ----------------------------------------------------------------------
	companion object
	{
		lateinit var instance: LevelSelectScreen
	}
}
