package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Board.Mote
import com.lyeeedar.Global
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 22-Jul-16.
 */

class CompletionConditionSink() : AbstractCompletionCondition()
{
	var count = 5

	lateinit var label: Label

	override fun attachHandlers(grid: Grid)
	{
		grid.onSunk += {

			val sprite = it.sprite.copy()
			val dst = label.localToStageCoordinates(Vector2())
			val src = GridWidget.instance.pointToScreenspace(it)

			Mote(src, dst, sprite, { if (count > 0) count--; label.setText("$count") })

			false
		}
	}

	override fun isCompleted(): Boolean = count == 0

	override fun parse(xml: XmlReader.Element)
	{
		count = xml.getInt("Count")
	}

	override fun createTable(skin: Skin, theme: LevelTheme): Table
	{
		val table = Table()

		val sprite = theme.coin.copy()
		label = Label("$count", Global.skin)

		table.add(SpriteWidget(sprite, 24f, 24f))
		table.add(label)

		return table
	}

	override fun getTextDescription(): String = "Move all the coins to the bottom"
}