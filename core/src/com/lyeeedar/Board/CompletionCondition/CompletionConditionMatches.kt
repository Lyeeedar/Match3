package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.set
import com.lyeeedar.Util.get

/**
 * Created by Philip on 13-Jul-16.
 */

class CompletionConditionMatches(): AbstractCompletionCondition()
{
	val toBeMatched = IntIntMap()
	val sprites = IntMap<Sprite>()
	val table = Table()

	override fun getTextDescription(): String = "Match the orbs"

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
		toBeMatched[grid.validOrbs[0].key] = 30

		grid.onPop += {
			if (toBeMatched.containsKey(it.key))
			{
				var count = toBeMatched[it.key]
				if (count > 0) count--
				toBeMatched[it.key] = count

				rebuildWidget()
			}
		}

		for (entry in toBeMatched.entries())
		{
			sprites.put(entry.key, grid.validOrbs.filter{ it.key == entry.key }.first().sprite)
		}
	}

	override fun isCompleted(): Boolean
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
//		for (i in 0..xml.childCount-1)
//		{
//			val el = xml.getChild(i)
//			val name = el.name
//			val key = name.hashCode()
//			val count = el.text.toInt()
//
//			toBeMatched[key] = count
//		}
	}
}