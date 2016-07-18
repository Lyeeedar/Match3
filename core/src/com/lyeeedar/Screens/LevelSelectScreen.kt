package com.lyeeedar.Screens

import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
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
		player.sprite = AssetManager.loadSprite("Oryx/Custom/heroes/Merc")

		val trapButton = TextButton("Create Trap Level", Global.skin)
		trapButton.addListener(object : ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				val theme = LevelTheme.load("Dungeon")
				val level = Level.load("Basic", theme)
				level.create()

				GridScreen.instance.updateLevel(level, player)
				GridScreen.instance.swapTo()
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
