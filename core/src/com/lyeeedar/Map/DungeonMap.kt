package com.lyeeedar.Map

import com.badlogic.gdx.utils.IntMap
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Direction
import com.lyeeedar.Map.Objective.AbstractObjective
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.*
import ktx.collections.get
import ktx.collections.set

/**
 * Created by Philip on 24-Jul-16.
 */

class DungeonMap(val seed: Long, val numRooms: Int)
{
	lateinit var theme: LevelTheme

	val playerPos: Point = Point()

	lateinit var objective: AbstractObjective

	val map: IntMap<DungeonMapEntry> = IntMap()

	fun isFree(point: Point): Boolean = !map.containsKey(point.hashCode())
	fun get(point: Point): DungeonMapEntry? = map[point.hashCode()]

	val width: Int
		get() = map.values().maxBy { it.point.x }!!.point.x

	val height: Int
		get() = map.values().maxBy { it.point.y }!!.point.y

	fun finishSetup()
	{
		val minx = map.values().minBy { it.point.x }!!.point.x
		val miny = map.values().minBy { it.point.y }!!.point.y

		val ox = minx * -1
		val oy = miny * -1

		val playerRoom = map[playerPos.hashCode()]

		val rooms = map.values().toArray()
		map.clear()
		for (room in rooms)
		{
			room.point.x += ox
			room.point.y += oy
			map[room.point.hashCode()] = room

		}

		playerPos.set(playerRoom.point)

		for (room in map)
		{
			if (room.value.isRoom)
			{
				room.value.onComplete += {onRoomComplete(room.value)}
			}
		}

		onRoomComplete += {objective.update(this@DungeonMap)}
	}

	val onRoomComplete = Event1Arg<DungeonMapEntry>()
}

class DungeonMapEntry(val point: Point)
{
	enum class Type
	{
		EMPTY,
		GOOD,
		BAD,
		BOSS
	}

	val connections: FastEnumMap<Direction, DungeonMapEntry> = FastEnumMap(Direction::class.java)

	var isRoom: Boolean = false
	var depth = 0
	var type: Type = Type.EMPTY
	var seen = false
	var level: Level? = null
		set(value)
		{
			field = value
			if (value != null)
			{
				value.onComplete += {onComplete()}
			}
		}

	val onComplete = Event0Arg()

	val isCompleted: Boolean
		get() = level?.completed ?: true
	val completedSprite: Sprite?
		get() = level?.completedMapSprite
	val uncompletedSprite: Sprite?
		get() = level?.uncompletedMapSprite
}