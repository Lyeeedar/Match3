package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.DefeatCondition.AbstractDefeatCondition
import com.lyeeedar.Board.VictoryCondition.AbstractVictoryCondition
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.MainGame
import com.lyeeedar.Player.Player
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.UI.FullscreenMessage
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.ranChild

/**
 * Created by Philip on 13-Jul-16.
 */

class Level
{
	enum class LevelType
	{
		NONE,
		TRAP,
		TREASURE,
		ENCOUNTER
	}

	lateinit var grid: Grid
	lateinit var defeat: AbstractDefeatCondition
	lateinit var victory: AbstractVictoryCondition
	lateinit var theme: LevelTheme
	lateinit var charGrid: Array2D<Char>
	lateinit var type: LevelType
	lateinit var player: Player

	var completed = false

	fun create()
	{
		grid = Grid(charGrid.xSize, charGrid.ySize, this)

		var hasMonster = false

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
				else if (char == '=')
				{
					tile.canHaveOrb = true
					tile.sprite = theme.floor.copy()
					tile.block = Block()
				}
				else if (char == '$')
				{
					tile.canHaveOrb = false
					tile.sprite = theme.floor.copy()
					tile.chest = Chest()
					tile.chest!!.attachHandlers(grid)
				}
				else if (char == '£')
				{
					tile.canHaveOrb = false
					tile.sprite = theme.floor.copy()
					tile.chest = Chest(false)
					tile.chest!!.attachHandlers(grid)
				}
				else if (char == '!')
				{
					tile.canHaveOrb = true
					tile.sprite = theme.floor.copy()

					hasMonster = true
				}
				else
				{
					tile.sprite = theme.floor.copy()
				}
			}
		}

		if (hasMonster)
		{
			// iterate through and find groups
			val blocks = Array<Array<Tile>>()

			for (x in 0..charGrid.xSize-1)
			{
				for (y in 0..charGrid.ySize - 1)
				{
					if (charGrid[x, y] == '!')
					{
						val tile = grid.grid[x, y]

						var found = false
						for (block in blocks)
						{
							for (testtile in block)
							{
								if (testtile.dist(tile) == 1)
								{
									block.add(tile)
									found = true
									break
								}
							}
						}

						if (!found)
						{
							val newArray = Array<Tile>()
							newArray.add(tile)
							blocks.add(newArray)
						}
					}
				}
			}

			// convet groups into x by x arrays
			for (block in blocks)
			{
				var minx = block[0].x
				var miny = block[0].y
				var maxx = block[0].x
				var maxy = block[0].y

				for (tile in block)
				{
					if (tile.x < minx) minx = tile.x
					if (tile.y < miny) miny = tile.y
					if (tile.x > maxx) maxx = tile.x
					if (tile.y > maxy) maxy = tile.y
				}

				val w = (maxx - minx) + 1
				val h = (maxy - miny) + 1

				if (w != h) throw Exception("Non-square monster!")

				val size = w
				val monster = Monster() // this should be a lookup thing, but ignore that for now
				monster.size = size

				for (x in 0..size-1)
				{
					for (y in 0..size-1)
					{
						val gx = minx + x
						val gy = miny + y

						val tile = grid.grid[gx, gy]

						tile.monster = monster
						monster.tiles[x, y] = tile
					}
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

	fun update(delta: Float)
	{
		val done = grid.update(delta)

		if (!completed && done)
		{
			if (victory.isVictory())
			{
				val message: String = when(type)
				{
					LevelType.TRAP -> "You narrowly escaped the deadly trap"
					LevelType.TREASURE -> "You managed to acquire the treasure"
					LevelType.ENCOUNTER -> "You defeated the monster"
					else -> ""
				}

				FullscreenMessage(message, "", { Global.game.switchScreen(MainGame.ScreenEnum.LEVELSELECT) }).show()
				completed = true
			}
			else if (defeat.isDefeated())
			{
				val message: String = when(type)
				{
					LevelType.TRAP -> "You failed to escape the trap"
					LevelType.TREASURE -> "The treasure slipped from your fingers"
					LevelType.ENCOUNTER -> "You died"
					else -> ""
				}

				FullscreenMessage(message, "", { Global.game.switchScreen(MainGame.ScreenEnum.LEVELSELECT) }).show()
				completed = true
			}
		}
	}

	companion object
	{
		fun load(path: String, theme: LevelTheme, type: LevelType, player: Player): Level
		{
			val xml = XmlReader().parse(Gdx.files.internal("Levels/$path.xml"))

			val rows = xml.getChildByName("Rows")
			val width = rows.getChild(0).text.length
			val height = rows.childCount

			val level = Level()

			level.charGrid = Array2D<Char>(width, height) { x, y -> rows.getChild(y).text[x] }
			level.defeat = AbstractDefeatCondition.load(xml.getChildByName("AllowedDefeats").ranChild())
			level.victory = AbstractVictoryCondition.load(xml.getChildByName("AllowedVictories").ranChild())
			level.theme = theme
			level.type = type
			level.player = player

			return level
		}
	}
}