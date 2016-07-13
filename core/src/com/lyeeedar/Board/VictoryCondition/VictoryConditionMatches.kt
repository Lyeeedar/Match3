package com.lyeeedar.Board.VictoryCondition

import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Util.set
import com.lyeeedar.Util.get

/**
 * Created by Philip on 13-Jul-16.
 */

class VictoryConditionMatches(): AbstractVictoryCondition()
{
	val toBeMatched = IntIntMap()

	override fun attachHandlers(grid: Grid)
	{
		grid.onPop += {
			if (toBeMatched.containsKey(it.key))
			{
				var count = toBeMatched[it.key]
				count--
				toBeMatched[it.key] = count
			}
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