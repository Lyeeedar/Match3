package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.CompletionAction.AbstractCompletionAction
import com.lyeeedar.Board.CompletionCondition.AbstractCompletionCondition
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.MainGame
import com.lyeeedar.Map.DungeonMapEntry
import com.lyeeedar.Player.Player
import com.lyeeedar.Rarity
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.UI.DungeonMapWidget
import com.lyeeedar.UI.FullscreenMessage
import com.lyeeedar.Util.*

/**
 * Created by Philip on 13-Jul-16.
 */

class Level(val loadPath: String)
{
	// Load Data
	lateinit var charGrid: Array2D<Char>

	lateinit var defeat: AbstractCompletionCondition
	lateinit var victory: AbstractCompletionCondition

	lateinit var defeatText: String
	lateinit var victoryText: String
	lateinit var entryText: String

	val defeatActions: Array<AbstractCompletionAction> = Array()
	val victoryActions: Array<AbstractCompletionAction> = Array()

	lateinit var rarity: Rarity
	lateinit var type: DungeonMapEntry.Type
	var maxCountPerMap: Int = Int.MAX_VALUE
	var minDepth: Int = 0
	var maxDepth: Int = Int.MAX_VALUE

	var uncompletedMapSprite: Sprite? = null
	var completedMapSprite: Sprite? = null

	// Active state data
	lateinit var theme: LevelTheme
	lateinit var grid: Grid
	lateinit var player: Player
	var completed = false
	var completeFun: () -> Unit = {}

	fun create(theme: LevelTheme, player: Player)
	{
		this.theme = theme
		this.player = player

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
			if (victory.isCompleted())
			{
				completeFun = {FullscreenMessage(victoryText, "", { Global.game.switchScreen(MainGame.ScreenEnum.MAP); victoryActions.forEach { it.apply() } }).show()}
				completed = true
			}
			else if (defeat.isCompleted())
			{
				completeFun = {FullscreenMessage(defeatText, "", { Global.game.switchScreen(MainGame.ScreenEnum.MAP); defeatActions.forEach { it.apply() } }).show()}
				completed = true
			}

			if (completed)
			{
				Future.call(completeFun, 0.5f, this)
			}
		}

		if (completed && !done)
		{
			Future.call(completeFun, 0.5f, this)
		}
	}

	fun copy() = Level.load(loadPath)

	companion object
	{
		fun load(path: String): Level
		{
			val xml = XmlReader().parse(Gdx.files.internal("Levels/$path.xml"))

			val level = Level(path)

			var rows = xml.getChildByName("Rows")

			if (rows == null)
			{
				// We are importing from another file
				val gridPath = xml.getChildByName("Grid").text
				val gridxml = XmlReader().parse(Gdx.files.internal(gridPath+".xml"))
				rows = gridxml.getChildByName("Rows")
			}

			val width = rows.getChild(0).text.length
			val height = rows.childCount
			level.charGrid = Array2D<Char>(width, height) { x, y -> rows.getChild(y).text[x] }

			level.defeat = AbstractCompletionCondition.load(xml.getChildByName("AllowedDefeats").ranChild())
			level.victory = AbstractCompletionCondition.load(xml.getChildByName("AllowedVictories").ranChild())

			level.defeatText = xml.get("DefeatText", "You defeated")
			level.victoryText = xml.get("VictoryText", "You win!")
			level.entryText = xml.get("EntryText", "Oh noes! You've encountered a thing!")

			val victoryActionsEl = xml.getChildByName("VictoryActions")
			if (victoryActionsEl != null)
			{
				for (i in 0..victoryActionsEl.childCount-1)
				{
					val el = victoryActionsEl.getChild(i)
					val action = AbstractCompletionAction.load(el)
					level.victoryActions.add(action)
				}
			}

			val defeatActionsEl = xml.getChildByName("DefeatActions")
			if (defeatActionsEl != null)
			{
				for (i in 0..defeatActionsEl.childCount-1)
				{
					val el = defeatActionsEl.getChild(i)
					val action = AbstractCompletionAction.load(el)
					level.defeatActions.add(action)
				}
			}

			level.rarity = Rarity.valueOf(xml.get("Rarity", "COMMON").toUpperCase())
			level.type = DungeonMapEntry.Type.valueOf(xml.get("Type").toUpperCase())
			level.maxCountPerMap = xml.getInt("MaxCountPerMap", level.maxCountPerMap)
			level.minDepth = xml.getInt("MinDepth", level.minDepth)
			level.maxDepth = xml.getInt("MaxDepth", level.maxDepth)

			level.uncompletedMapSprite = AssetManager.tryLoadSprite(xml.getChildByName("UncompletedMapSprite"))
			level.completedMapSprite = AssetManager.tryLoadSprite(xml.getChildByName("CompletedMapSprite"))

			return level
		}

		fun loadAll(): FastEnumMap<DungeonMapEntry.Type, Array<Level>>
		{
			val levels = FastEnumMap<DungeonMapEntry.Type, Array<Level>>(DungeonMapEntry.Type::class.java)
			for (type in DungeonMapEntry.Type.values()) levels[type] = Array()

			val xml = XmlReader().parse(Gdx.files.internal("Levels/LevelList.xml"))
			for (i in 0..xml.childCount-1)
			{
				val el = xml.getChild(i)
				val path = el.text
				val level = load(path)

				levels[level.type].add(level)
			}

			return levels
		}
	}
}