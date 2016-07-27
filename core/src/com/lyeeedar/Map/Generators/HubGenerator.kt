package com.lyeeedar.Map.Generators

import com.badlogic.gdx.utils.Array
import com.lyeeedar.Board.Level
import com.lyeeedar.Direction
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.DungeonMapEntry
import com.lyeeedar.Util.*
import java.util.*

/**
 * Created by Philip on 24-Jul-16.
 */

class HubGenerator
{
	val ran: Random = Random()
	val maxCorridorLength = 4
	val maxDepth = 10

	fun generate(): DungeonMap
	{
		val map = DungeonMap()

		val hub = DungeonMapEntry()
		hub.isRoom = true
		map.map[Point.ZERO] = hub

		for (dir in Direction.CardinalValues)
		{
			val placed = expand(map, Point(dir.x, dir.y), dir)
			if (placed)
			{
				hub.connections[dir] = map.map[Point.ZERO + dir]
			}
		}

		val endOfChainRooms = Array<DungeonMapEntry>()
		val unfilledRooms = Array<DungeonMapEntry>()

		for (room in map.map)
		{
			if (room.value.isRoom && room.value != hub)
			{
				if (room.value.connections.size == 1)
				{
					endOfChainRooms.add(room.value)
				}
				else
				{
					unfilledRooms.add(room.value)
				}
			}
		}

		// 0.1 emtpy
		// 0.7 bad
		// 0.2 good
		val totalCount = endOfChainRooms.size + unfilledRooms.size

		val empty = (unfilledRooms.size * 0.1f).toInt()
		val good = Math.max(0, (totalCount * 0.2f).toInt() - endOfChainRooms.size)
		val bad = unfilledRooms.size - (empty + good)

		val goodRooms = Array<DungeonMapEntry>()
		val badRooms = Array<DungeonMapEntry>()

		for (room in endOfChainRooms)
		{
			room.type = DungeonMapEntry.Type.GOOD
			goodRooms.add(room)
		}

		for (i in 1..good)
		{
			val room = unfilledRooms.removeRandom(ran)
			room.type = DungeonMapEntry.Type.GOOD
			goodRooms.add(room)
		}
		for (i in 1..bad)
		{
			val room = unfilledRooms.removeRandom(ran)
			room.type = DungeonMapEntry.Type.BAD
			badRooms.add(room)
		}

		var first = true
		for (room in badRooms)
		{
			if (first)
			{
				room.levelType = Level.LevelType.ENCOUNTER
			}
			else
			{
				if (ran.nextBoolean())
				{
					room.levelType = Level.LevelType.ENCOUNTER
				}
				else
				{
					room.levelType = Level.LevelType.TRAP
				}
			}

			first = false
		}

		for (room in goodRooms)
		{
			room.levelType = Level.LevelType.TREASURE
		}

		for (room in map.map)
		{
			if (room.value.levelType == Level.LevelType.ENCOUNTER)
			{
				room.value.uncompletesprite = AssetManager.loadSprite("Oryx/Custom/dungeonmap/combat")
				room.value.completesprite = AssetManager.loadSprite("Oryx/Custom/dungeonmap/combat_complete")
			}
			else if (room.value.levelType == Level.LevelType.TRAP)
			{
				room.value.uncompletesprite = AssetManager.loadSprite("Oryx/Custom/dungeonmap/spiketrap")
				room.value.completesprite = AssetManager.loadSprite("Oryx/Custom/dungeonmap/spiketrap_complete")
			}
			else if (room.value.levelType == Level.LevelType.TREASURE)
			{
				room.value.uncompletesprite = AssetManager.loadSprite("Oryx/Custom/dungeonmap/treasure")
				room.value.completesprite = AssetManager.loadSprite("Oryx/Custom/dungeonmap/treasure_complete")
			}
		}

		return map
	}

	fun expand(map: DungeonMap, point: Point, dir: Direction, corridorCount: Int = 0, depth: Int = 0): Boolean
	{
		if (!map.isFree(point)) return false

		val freeDirections = Array<Direction>()
		if (map.isFree(point + dir)) { freeDirections.add(dir); freeDirections.add(dir); freeDirections.add(dir) } // weight going forward more
		if (map.isFree(point + dir.cardinalClockwise)) freeDirections.add(dir.cardinalClockwise)
		if (map.isFree(point + dir.cardinalAnticlockwise)) freeDirections.add(dir.cardinalAnticlockwise)

		var makeRoom: Boolean

		if (freeDirections.size == 0)
		{
			if (corridorCount == 0) return false
			makeRoom = true
		}
		else if (corridorCount > 0)
		{
			// make a room, higher prob with more corridor
			makeRoom = ran.nextInt(maxCorridorLength) < corridorCount
		}
		else
		{
			makeRoom = false
		}

		val tempRoom = DungeonMapEntry()
		tempRoom.depth = depth
		tempRoom.connections[dir.opposite] = map.map[point + dir.opposite]
		map.map[point] = tempRoom

		if (!makeRoom)
		{
			// attempt to make a corridor
			val newDir = freeDirections.random(ran)
			val placed = expand(map, point + newDir, newDir, corridorCount+1, depth+1)
			if (!placed)
			{
				makeRoom = true
			}
			else
			{
				tempRoom.connections[newDir] = map.map[point + newDir]
			}
		}

		if (makeRoom)
		{
			tempRoom.isRoom = true

			if (freeDirections.size == 0)
			{
				tempRoom.type = DungeonMapEntry.Type.GOOD
			}
			else
			{
				// assign random type
				tempRoom.type = DungeonMapEntry.Type.values().asSequence().random(ran)!!

				if (depth < maxDepth)
				{
					val numRooms = sequenceOf(0, 1, 1, 1, 1, 1, 2, 2, 3).random(ran)!!

					for (i in 0..numRooms-1)
					{
						val newDir = freeDirections.removeRandom(ran)

						val placed = expand(map, point + newDir, newDir, 0, depth+1)
						if (placed)
						{
							tempRoom.connections[newDir] = map.map[point + newDir]
						}

						if (freeDirections.size == 0) break
					}
				}
			}
		}

		return true
	}
}