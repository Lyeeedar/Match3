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
import com.lyeeedar.UI.PowerBar
import com.lyeeedar.Util.AssetManager
import java.awt.event.MouseListener

/**
 * Created by Philip on 20-Mar-16.
 */

class GridScreen(): AbstractScreen()
{
	// ----------------------------------------------------------------------
	init
	{
		instance = this
	}

	// ----------------------------------------------------------------------
	override fun create()
	{
		Global.stage = stage
	}

	// ----------------------------------------------------------------------
	fun updateLevel(level: Level, player: Player)
	{
		if (!created)
		{
			baseCreate()
			created = true
		}

		player.attachHandlers(level.grid)

		this.level = level

		val widget = GridWidget(level.grid)
		val playerWidget = player.createTable(Global.skin)

		val powerBar = PowerBar()

		val defeatWidget = level.defeat.createTable(Global.skin)
		val victoryWidget = level.victory.createTable(Global.skin)

		mainTable.clear()
		val table = mainTable

		table.defaults().pad(10f)

		val background = TextureRegionDrawable(level.theme.floor.sprite!!.currentTexture)
		table.background = TiledDrawable(background).tint(Color.DARK_GRAY)

		table.add(defeatWidget).left()
		table.row()
		table.add(powerBar).expandX().height(25f).fill()
		table.row()
		table.add(widget).expand().fill()
		table.row()
		table.add(victoryWidget).left()

		var message = ""
		message += "\n\nVictory Condition: " + level.victory.getTextDescription()
		message += "\n\nDefeat Condition: " + level.defeat.getTextDescription()

		val fswidget = FullscreenMessage(message, "", { val i = 9 })
		fswidget.setFillParent(true)

		Global.stage.addActor(fswidget)
	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{
		level.update(delta)

		FullscreenMessage.instance?.update(delta)
	}

	lateinit var level: Level

	// ----------------------------------------------------------------------
	companion object
	{
		lateinit var instance: GridScreen
	}
}