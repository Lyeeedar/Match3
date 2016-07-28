package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Monster
import com.lyeeedar.Global
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager


class CompletionConditionKill() : AbstractCompletionCondition()
{
	var monsters = Array<Monster>()

	val table = Table()

	override fun attachHandlers(grid: Grid)
	{
		for (tile in grid.grid)
		{
			if (tile.monster != null)
			{
				val monster = tile.monster!!
				if (!monsters.contains(monster, true))
				{
					monsters.add(monster)
				}
			}
		}

		grid.onDamaged += {
			rebuildWidget()
		}
	}

	override fun isCompleted(): Boolean = monsters.filter { it.hp > 0 }.count() == 0

	override fun parse(xml: XmlReader.Element)
	{
	}

	override fun createTable(skin: Skin): Table
	{
		rebuildWidget()

		return table
	}

	fun rebuildWidget()
	{
		table.clear()

		table.defaults().pad(10f)

		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("GUI/TilePanel"), 6, 6, 6, 6))

		for (monster in monsters)
		{
			val sprite = monster.sprite.copy()
			val hp = monster.hp
			val max = monster.maxhp

			table.add(SpriteWidget(sprite, 24, 24))
			table.add(Label("$hp/$max", Global.skin))
		}
	}

	override fun getTextDescription(): String = "Kill all the monsters"
}