package com.lyeeedar.Board

import com.lyeeedar.Board.DefeatCondition.AbstractDefeatCondition
import com.lyeeedar.Board.VictoryCondition.AbstractVictoryCondition
import com.lyeeedar.Util.Array2D

/**
 * Created by Philip on 13-Jul-16.
 */

class Level
{
	lateinit var grid: Grid
	var difficulty: Int = 0
	lateinit var defeat: AbstractDefeatCondition
	lateinit var victory: AbstractVictoryCondition
	lateinit var theme: LevelTheme
	lateinit var charGrid: Array2D<Char>

	fun create()
	{
		grid = Grid(charGrid.xSize, charGrid.ySize)

		for (x in 0..charGrid.xSize-1)
		{
			for (y in 0..charGrid.ySize-1)
			{
				val tile = grid.grid[x, y]
				val char = charGrid[x, y]

				if (char == '#')
				{
					tile.canHaveOrb = false
					tile.sprite = theme.wall.copy()
				}
				else if (char == 's')
				{
					tile.canSpawn = true
					tile.sprite = theme.floor.copy()
				}
				else if (char == 'v')
				{
					tile.canSink = true
					tile.sprite = theme.floor.copy()
				}
				else if (char == '=')
				{
					tile.canHaveOrb = true
					tile.sprite = theme.floor.copy()
					tile.block = Block()
				}
				else
				{
					tile.sprite = theme.floor.copy()
				}
			}
		}

		grid.loadSpecials()
		grid.fill()
	}
}