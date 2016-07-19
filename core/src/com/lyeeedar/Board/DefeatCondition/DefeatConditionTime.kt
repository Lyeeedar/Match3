package com.lyeeedar.Board.DefeatCondition

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

class DefeatConditionTime(): AbstractDefeatCondition()
{
	var time: Float = 60f

	lateinit var label: Label

	override fun getTextDescription(): String = "Run out of time"

	override fun createTable(skin: Skin): Table
	{
		val t = time.toInt()
		label = Label("$t", skin)

		val table = Table()
		table.defaults().pad(10f)
		table.add(label)

		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("GUI/TilePanel"), 6, 6, 6, 6))

		return table
	}

	override fun parse(xml: XmlReader.Element)
	{
		//time = xml.text.toFloat()
	}

	override fun attachHandlers(grid: Grid)
	{
		grid.onTime +=
				{
					time -= it
					val t = time.toInt()
					label.setText("$t")
				}
	}

	override fun isDefeated(): Boolean = time <= 0
}
