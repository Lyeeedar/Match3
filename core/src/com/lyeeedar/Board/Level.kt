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
import com.lyeeedar.Renderables.Sprite.Sprite
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
	lateinit var category: DungeonMapEntry.Type
	lateinit var type: String
	var minDepth: Int = 0
	var maxDepth: Int = Int.MAX_VALUE

	var sealStrength = 1
	var blockStrength = 1

	var uncompletedMapSprite: Sprite? = null
	var completedMapSprite: Sprite? = null

	val factions = Array<String>()

	// Active state data
	lateinit var theme: LevelTheme
	lateinit var grid: Grid
	lateinit var player: Player
	var completed = false
	var completeFun: (() -> Unit)? = null
	val onComplete = Event0Arg()

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
				else if (char == '~')
				{
					tile.canHaveOrb = false
					tile.isPit = true
					tile.sprite = theme.pit.copy()
				}
				else if (char == '=')
				{
					tile.canHaveOrb = true
					tile.sprite = theme.floor.copy()
					tile.block = Block(theme)
					tile.block!!.count = blockStrength
				}
				else if (char == '$')
				{
					tile.chest = Chest(true, theme)
					tile.canHaveOrb = false
					tile.sprite = theme.floor.copy()
					tile.chest!!.attachHandlers(grid)
				}
				else if (char == '£')
				{
					tile.chest = Chest(false, theme)
					tile.canHaveOrb = false
					tile.sprite = theme.floor.copy()
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
			val chosenFaction = Faction.load(factions.random())

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

			// convert groups into x by x arrays
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
				val monsterDesc = if (size == 1) chosenFaction.size1.random() else chosenFaction.size2.random()
				val monster =  Monster(monsterDesc)
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
					orb!!.special = Vertical4(orb)
				}
				else if (char == '-')
				{
					orb!!.special = Horizontal4(orb)
				}
				else if (char == '@')
				{
					orb!!.sealCount = sealStrength
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
			if (player.hp <= 0 || victory.isCompleted() || defeat.isCompleted())
			{
				completeFun = {complete()}
				completed = true
				onComplete()
			}

			if (completed && completeFun != null)
			{
				Future.call(completeFun!!, 0.5f, this)
			}
		}

		if (completed && completeFun != null && (!done || Mote.moteCount > 0))
		{
			Future.call(completeFun!!, 0.5f, this)
		}
	}

	private fun complete()
	{
		completeFun = null
		if (player.hp <= 0)
		{
			FullscreenMessage("You died", "", { Global.game.switchScreen(MainGame.ScreenEnum.MAP); defeatActions.forEach { it.apply(player) }; player.hp += player.regen }).show()
		}
		else if (victory.isCompleted())
		{
			FullscreenMessage(victoryText, "", { Global.game.switchScreen(MainGame.ScreenEnum.MAP); victoryActions.forEach { it.apply(player) }; player.hp += player.regen }).show()
		}
		else if (defeat.isCompleted())
		{
			FullscreenMessage(defeatText, "", { Global.game.switchScreen(MainGame.ScreenEnum.MAP); defeatActions.forEach { it.apply(player) }; player.hp += player.regen }).show()
		}
	}

	fun copy(): Level
	{
		val base = load(loadPath)[0]
		base.charGrid = charGrid
		return base
	}

	companion object
	{
		fun load(path: String): Array<Level>
		{
			val xml = XmlReader().parse(Gdx.files.internal("World/Levels/$path.xml"))

			val levels = Array<Level>()

			val grid = xml.getChildByName("Grid")
			for (ci in 0.. grid.childCount-1)
			{
				val cel = grid.getChild(ci)

				val level = Level(path)

				var rows = cel

				if (rows.name == "Path" || rows.text != null)
				{
					// We are importing from another file
					val gridPath = rows.text
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
				level.category = DungeonMapEntry.Type.valueOf(xml.get("Category").toUpperCase())
				level.type = xml.get("Type")
				level.minDepth = xml.getInt("MinDepth", level.minDepth)
				level.maxDepth = xml.getInt("MaxDepth", level.maxDepth)

				level.sealStrength = xml.getInt("SealStrength", 1)
				level.blockStrength = xml.getInt("BlockStrength", 1)

				level.factions.addAll(xml.get("Faction", "").split(",").asSequence())

				level.uncompletedMapSprite = AssetManager.tryLoadSprite(xml.getChildByName("UncompletedMapSprite"))
				level.completedMapSprite = AssetManager.tryLoadSprite(xml.getChildByName("CompletedMapSprite"))

				levels.add(level)
			}

			return levels
		}

		fun loadAll(dungeon: String): FastEnumMap<DungeonMapEntry.Type, Array<Level>>
		{
			val levels = FastEnumMap<DungeonMapEntry.Type, Array<Level>>(DungeonMapEntry.Type::class.java)
			for (type in DungeonMapEntry.Type.values()) levels[type] = Array()

			val xml = XmlReader().parse(Gdx.files.internal("World/Levels/LevelList.xml"))
			for (di in 0..xml.childCount-1)
			{
				val del = xml.getChild(di)

				if (del.name.toLowerCase() == dungeon.toLowerCase())
				{
					for (i in 0..del.childCount-1)
					{
						val el = del.getChild(i)

						val path = el.text
						val loadedlevels = load(path)

						for (level in loadedlevels) levels[level.category].add(level)
					}
				}
			}

			return levels
		}
	}
}
