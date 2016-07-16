package com.lyeeedar.Screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.Player.Player

import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.UI.FullscreenMessage
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.Util.AssetManager
import java.awt.event.MouseListener

/**
 * Created by Philip on 20-Mar-16.
 */

class GridScreen(): AbstractScreen()
{
	// ----------------------------------------------------------------------
	override fun create()
	{
		Global.stage = stage

		val player = Player()
		player.sprite = AssetManager.loadSprite("Oryx/Custom/heroes/Merc")
		val playerWidget = player.createTable(Global.skin)

		val theme = LevelTheme.load("Dungeon")
		val level = Level.load("Basic", theme)
		level.create()
		grid = level.grid

		player.attachHandlers(grid)

		val widget = GridWidget(grid, player)

		val defeatWidget = level.defeat.createTable(Global.skin)
		val victoryWidget = level.victory.createTable(Global.skin)

		val table = Table()
		table.isVisible = true
		table.setFillParent(true)
		stage.addActor(table)
		//table.debug()

		val background = TextureRegionDrawable(theme.floor.sprite!!.currentTexture)
		table.background = TiledDrawable(background).tint(Color.DARK_GRAY)

		table.add(defeatWidget).left()
		table.row()
		table.add(playerWidget)
		table.row()
		table.add(widget).expand()
		table.row()
		table.add(victoryWidget).left()

		var message = level.entryMessage
		message += "\n\nVictory Condition: " + level.victory.getTextDescription()
		message += "\n\nDefeat Condition: " + level.defeat.getTextDescription()

		val fswidget = FullscreenMessage(message, "", { val i = 9 })
		fswidget.setFillParent(true)

		Global.stage.addActor(fswidget)
	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{
		grid.update(delta)

		FullscreenMessage.instance?.update(delta)
	}

	lateinit var grid: Grid
}