package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid

/**
 * Created by Philip on 07-Aug-16.
 */

class CompletionConditionNone() : AbstractCompletionCondition()
{
	override fun attachHandlers(grid: Grid)
	{

	}

	override fun isCompleted(): Boolean = false

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun createTable(skin: Skin): Table = Table()

	override fun getTextDescription(): String = "Your hp reaches 0"
}