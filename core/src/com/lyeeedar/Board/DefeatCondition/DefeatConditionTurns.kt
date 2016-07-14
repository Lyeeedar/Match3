package com.lyeeedar.Board.DefeatCondition

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid

/**
 * Created by Philip on 13-Jul-16.
 */

class DefeatConditionTurns(): AbstractDefeatCondition()
{
	var turnCount: Int = 30

	lateinit var label: Label

	override fun createTable(skin: Skin): Table
	{
		label = Label("$turnCount", skin)

		val table = Table()
		table.add(label)

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

	override fun isDefeated(): Boolean = turnCount <= 0
}
