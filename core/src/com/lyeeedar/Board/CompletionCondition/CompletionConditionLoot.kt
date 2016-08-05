package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Mote
import com.lyeeedar.Global
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 22-Jul-16.
 */

class CompletionConditionLoot(): AbstractCompletionCondition()
{
	val tick = AssetManager.loadSprite("Oryx/uf_split/uf_interface/uf_interface_680", colour = Color.FOREST)

	var count = 0

	lateinit var table: Table
	lateinit var grid: Grid

	override fun attachHandlers(grid: Grid)
	{
		this.grid = grid

		grid.onSunk += {

			val sprite = it.sprite.copy()
			val dst = table.localToStageCoordinates(Vector2())
			val src = GridWidget.instance.pointToScreenspace(it)

			Mote(src, dst, sprite, { count++; updateTable() })
		}
	}

	override fun isCompleted(): Boolean = if (grid.level.defeat.isCompleted()) true else false

	override fun parse(xml: XmlReader.Element)
	{

	}

	fun updateTable()
	{
		table.clear()

		val sprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_gold")

		table.add(SpriteWidget(sprite, 24, 24))

		if (count == 0)
		{
			table.add(SpriteWidget(tick, 24, 24))
		}
		else
		{
			table.add(Label("$count", Global.skin))
		}
	}

	override fun createTable(skin: Skin): Table
	{
		table = Table()

		table.defaults().pad(10f)

		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("GUI/TilePanel"), 6, 6, 6, 6))

		updateTable()

		return table
	}

	override fun getTextDescription(): String = "Loot as many coins as you can before time runs out"
}