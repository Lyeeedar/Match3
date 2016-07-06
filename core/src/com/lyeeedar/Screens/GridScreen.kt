package com.lyeeedar.Screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Board.Grid

import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.UI.GridWidget
import java.awt.event.MouseListener

/**
 * Created by Philip on 20-Mar-16.
 */

class GridScreen(): AbstractScreen()
{
	// ----------------------------------------------------------------------
	override fun create()
	{
		grid = Grid.load("Basic")

		val widget = GridWidget()
		widget.grid = grid

		val table = Table()
		table.isVisible = true
		table.setFillParent(true)
		stage.addActor(table)
		//table.debug()

		table.add(widget)
	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{
		grid.update()
	}

	lateinit var grid: Grid
}