package com.lyeeedar.Board.CompletionAction

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.exp4j.Helpers.EquationHelper
import com.lyeeedar.Board.Mote
import com.lyeeedar.Player.Item
import com.lyeeedar.Player.Player
import com.lyeeedar.Screens.MapScreen
import com.lyeeedar.UI.DungeonMapWidget
import com.lyeeedar.UI.PlayerWidget
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.ciel
import com.lyeeedar.Util.round

/**
 * Created by Philip on 29-Jul-16.
 */

data class Drop(val item: Item, val change: Int, val weight: Int)

class CompletionActionItem() : AbstractCompletionAction()
{
	val drops = Array<Drop>()
	lateinit var dropCount: String

	override fun apply(player: Player)
	{
		if (dropCount == "All")
		{
			for (drop in drops)
			{
				if (drop.change > 0)
				{
					for (i in 1..drop.change)
					{
						val sprite = drop.item.icon.copy()
						val dst = MapScreen.instance.getPortraitPos()
						val src = DungeonMapWidget.instance.getCenterInScreenspace()

						Mote(src, dst, sprite, {  })
						player.addItem(drop.item.copy())
					}
				}
				else
				{
					player.removeItem(drop.item)
				}
			}
		}
		else
		{
			val count = EquationHelper.evaluate(dropCount).round()
			val weightedList = Array<Drop>()
			for (drop in drops)
			{
				for (i in 1..drop.weight)
				{
					weightedList.add(drop)
				}
			}

			for (c in 1..count)
			{
				val drop = weightedList.random()

				if (drop.change > 0)
				{
					for (i in 1..drop.change)
					{
						val sprite = drop.item.icon.copy()
						val dst = MapScreen.instance.getPortraitPos()
						val src = DungeonMapWidget.instance.getCenterInScreenspace()

						Mote(src, dst, sprite, {  })
						player.addItem(drop.item.copy())
					}
				}
				else
				{
					player.removeItem(drop.item)
				}
			}
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		dropCount = xml.getAttribute("NumToDrop", "All")

		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)
			val split = el.text.split(",")

			val item = Item.load(split[0])
			val change = split[1].toInt()
			val weight = split[2].toInt()

			drops.add(Drop(item, change, weight))
		}
	}
}