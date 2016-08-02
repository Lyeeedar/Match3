package com.lyeeedar.Map.Generators

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Direction
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.DungeonMapEntry
import com.lyeeedar.Map.Objective.ObjectiveExplore
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

	fun generate(theme: LevelTheme): DungeonMap
	{
		val map = DungeonMap()

		map.theme = theme
		map.objective = ObjectiveExplore()

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

		val emptyRooms = Array<DungeonMapEntry>()
		val goodRooms = Array<DungeonMapEntry>()
		val badRooms = Array<DungeonMapEntry>()
		var bossRoom: DungeonMapEntry? = null

		for (room in endOfChainRooms)
		{
			if (bossRoom == null)
			{
				bossRoom = room
				room.type = DungeonMapEntry.Type.BOSS
			}
			else if (room.depth > bossRoom.depth)
			{
				goodRooms.add(bossRoom)
				bossRoom.type = DungeonMapEntry.Type.GOOD

				bossRoom = room
				room.type = DungeonMapEntry.Type.BOSS
			}
			else
			{
				room.type = DungeonMapEntry.Type.GOOD
				goodRooms.add(room)
			}
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
		for (i in 1..empty)
		{
			val room = unfilledRooms.removeRandom(ran)
			room.type = DungeonMapEntry.Type.EMPTY
			emptyRooms.add(room)
		}

		val levels = Level.loadAll()
		bossRoom!!.level = if (levels[DungeonMapEntry.Type.BOSS].size > 0) levels[DungeonMapEntry.Type.BOSS].random(ran) else null

		fun assignLevels(type: DungeonMapEntry.Type, rooms: Array<DungeonMapEntry>)
		{
			val used = ObjectMap<Level, Int>()
			val weights = theme.roomWeights[type] ?: return
			val typeList = Array<String>()
			for (weight in weights)
			{
				for (i in 1..weight.value) typeList.add(weight.key)
			}
			val chosenType = typeList.random()

			for (room in rooms)
			{
				// build list of valid levels
				val valid = Array<Level>()
				for (level in levels[type])
				{
					if (level.minDepth > room.depth || level.maxDepth < room.depth || level.type != chosenType) continue

					val usedCount = used.get(level, 0)
					if (usedCount >= level.maxCountPerMap) continue

					for (i in 0..level.rarity.ordinal) valid.add(level)
				}

				if (valid.size > 0)
				{
					val level = valid.random(ran)
					room.level = level.copy()

					val usedCount = used.get(level, 0)
					used[level] = usedCount + 1
				}
				else
				{
					room.isRoom = false
				}
			}
		}

		assignLevels(DungeonMapEntry.Type.EMPTY, emptyRooms)
		assignLevels(DungeonMapEntry.Type.GOOD, goodRooms)
		assignLevels(DungeonMapEntry.Type.BAD, badRooms)

		map.finishSetup()
		map.objective.update(map, hub)

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