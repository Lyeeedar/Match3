package com.lyeeedar.Board.DefeatCondition

import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid

/**
 * Created by Philip on 13-Jul-16.
 */

class DefeatConditionTurns(): AbstractDefeatCondition()
{
	var turnCount: Int = 30

	override fun parse(xml: XmlReader.Element)
	{
		turnCount = xml.text.toInt()
	}

	override fun attachHandlers(grid: Grid)
	{
		grid.onTurn += { turnCount-- }
	}

	override fun isDefeated(): Boolean = turnCount <= 0
}
