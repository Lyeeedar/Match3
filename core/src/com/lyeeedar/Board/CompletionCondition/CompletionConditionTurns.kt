package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
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
		label = Label("$turnCount", skin)

		val table = Table()
		table.defaults().pad(10f)
		table.add(label)

		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("GUI/TilePanel"), 6, 6, 6, 6))

		return table
	}

	override fun parse(xml: XmlReader.Element)
	{
		turnCount = xml.text.toInt()
	}

	override fun attachHandlers(grid: Grid)
	{
		grid.onTurn +=
				{
					turnCount--
					label.setText("$turnCount")
				}
	}

	override fun isCompleted(): Boolean = turnCount <= 0
}
