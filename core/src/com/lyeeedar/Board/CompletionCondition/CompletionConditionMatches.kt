package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Orb
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
	val tick = AssetManager.loadSprite("Oryx/uf_split/uf_interface/uf_interface_680", colour = Color.FOREST)

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

			if (count == 0)
			{
				table.add(SpriteWidget(tick, 24, 24))
			}
			else
			{
				table.add(Label("$count", Global.skin))
			}
		}
	}

	override fun attachHandlers(grid: Grid)
	{
		val entries = toBeMatched.toList()
		toBeMatched.clear()
		for (entry in entries)
		{
			val valid = Array<Int>()
			for (orb in grid.validOrbs) if (!toBeMatched.containsKey(orb.key)) valid.add(orb.key)

			if (valid.size > 0)
			{
				toBeMatched[valid.random()] = entry.value
			}
		}

		grid.onPop += fun (orb: Orb, delay: Float ) {
			if (toBeMatched.containsKey(orb.key))
			{
				var count = toBeMatched[orb.key]
				if (count > 0) count--
				toBeMatched[orb.key] = count

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
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)
			val count = el.text.toInt()

			toBeMatched[i] = count
		}
	}
}