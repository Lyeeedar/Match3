package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.DefeatCondition.AbstractDefeatCondition
import com.lyeeedar.Board.VictoryCondition.AbstractVictoryCondition
import com.lyeeedar.Direction
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.Util.Array2D

/**
 * Created by Philip on 13-Jul-16.
 */

class Level
{
	lateinit var grid: Grid
	lateinit var defeat: AbstractDefeatCondition
	lateinit var victory: AbstractVictoryCondition
	lateinit var theme: LevelTheme
	lateinit var charGrid: Array2D<Char>

	fun create()
	{
		grid = Grid(charGrid.xSize, charGrid.ySize, this)

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

		for (x in 0..charGrid.xSize-1)
		{
			for (y in 0..charGrid.ySize-1)
			{
				val tile = grid.grid[x, y]
				val orb = tile.orb
				val char = charGrid[x, y]

				if (char == '|')
				{
					orb!!.explosion = grid.specialOrbs.filter { it.dir == Direction.NORTH && it.count == 4 }.first()
				}
				else if (char == '-')
				{
					orb!!.explosion = grid.specialOrbs.filter { it.dir == Direction.EAST && it.count == 4 }.first()
				}
				else if (char == '@')
				{
					orb!!.sealed = true
				}
			}
		}

		defeat.attachHandlers(grid)
		victory.attachHandlers(grid)
	}

	companion object
	{
		fun load(path: String, theme: LevelTheme): Level
		{
			val xml = XmlReader().parse(Gdx.files.internal("Levels/$path.xml"))

			val rows = xml.getChildByName("Rows")
			val width = rows.getChild(0).text.length
			val height = rows.childCount

			val level = Level()

			level.charGrid = Array2D<Char>(width, height) { x, y -> rows.getChild(y).text[x] }
			level.defeat = AbstractDefeatCondition.load(xml.getChildByName("Defeat").getChild(0))
			level.victory = AbstractVictoryCondition.load(xml.getChildByName("Victory").getChild(0))
			level.theme = theme

			return level
		}
	}
}