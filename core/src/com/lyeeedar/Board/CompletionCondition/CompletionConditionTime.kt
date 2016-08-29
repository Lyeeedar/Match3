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

class CompletionConditionTime(): AbstractCompletionCondition()
{
	var time: Float = 60f

	lateinit var label: Label

	override fun getTextDescription(): String = "Run out of time"

	override fun createTable(skin: Skin): Table
	{
		val t = time.toInt()
		label = Label("$t\nSeconds", skin)
		label.setAlignment(Align.center)

		val table = Table()
		table.defaults().pad(10f)
		table.add(label)

		return table
	}

	override fun parse(xml: XmlReader.Element)
	{
		time = xml.getInt("Time").toFloat()
	}

	override fun attachHandlers(grid: Grid)
	{
		grid.onTime +=
				{
					time -= it
					val t = time.toInt()
					label.setText("$t\nSeconds")
				}
	}

	override fun isCompleted(): Boolean = time <= 0
}
