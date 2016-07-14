package com.lyeeedar.Board.VictoryCondition

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.set
import com.lyeeedar.Util.get

/**
 * Created by Philip on 13-Jul-16.
 */

class VictoryConditionMatches(): AbstractVictoryCondition()
{
	val toBeMatched = IntIntMap()
	val sprites = IntMap<Sprite>()
	val table = Table()

	override fun createTable(skin: Skin): Table
	{
		rebuildWidget()

		return table
	}

	fun rebuildWidget()
	{
		table.clear()

		for (entry in toBeMatched.entries())
		{
			val sprite = sprites[entry.key]
			val count = entry.value

			table.add(SpriteWidget(sprite, 24, 24))
			table.add(Label("$count", Global.skin))
		}
	}

	override fun attachHandlers(grid: Grid)
	{
		grid.onPop += {
			if (toBeMatched.containsKey(it.key))
			{
				var count = toBeMatched[it.key]
				count--
				toBeMatched[it.key] = count

				rebuildWidget()
			}
		}

		for (entry in toBeMatched.entries())
		{
			sprites.put(entry.key, grid.validOrbs.filter{ it.key == entry.key }.first().sprite)
		}
	}

	override fun isVictory(): Boolean
	{
		var done = true
		for (entry in toBeMatched.entries())
		{
			if (entry.value > 0)
			{
				done = false
				break
			}
		}

		return done
	}

	override fun parse(xml: XmlReader.Element)
	{
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)
			val name = el.name
			val key = name.hashCode()
			val count = el.text.toInt()

			toBeMatched[key] = count
		}
	}
}