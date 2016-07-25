package com.lyeeedar.Map.Generators

import com.badlogic.gdx.utils.Array
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

		return map
	}

	fun expand(map: DungeonMap, point: Point, dir: Direction, corridorCount: Int = 0, depth: Int = 0): Boolean
	{
		if (!map.isFree(point)) return false
		val tempRoom = DungeonMapEntry()
		tempRoom.depth = depth
		tempRoom.connections[dir.opposite] = map.map[point + dir.opposite]
		map.map[point] = tempRoom
		System.out.println("adding room at $point")

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

						val placed = expand(map, point + newDir, newDir, corridorCount+1, depth+1)
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