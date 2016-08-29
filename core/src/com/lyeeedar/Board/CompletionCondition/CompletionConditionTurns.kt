package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 13-Jul-16.
 */

class CompletionConditionTurns(): AbstractCompletionCondition()
{
	var turnCount: Int = 30

	lateinit var label: Label

	override fun getTextDescription(): String = "Run out of turns"

	override fun createTable(skin: Skin): Table
	{
		label = Label("$turnCount\nTurns", skin)
		label.setAlignment(Align.center)

		val table = Table()
		table.defaults().pad(10f)
		table.add(label)

		return table
	}

	override fun parse(xml: XmlReader.Element)
	{
		turnCount = xml.getInt("Turns")
	}

	override fun attachHandlers(grid: Grid)
	{
		grid.onTurn +=
				{
					turnCount--
					label.setText("$turnCount\nTurns")
				}
	}

	override fun isCompleted(): Boolean = turnCount <= 0
}
