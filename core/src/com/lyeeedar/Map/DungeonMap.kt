package com.lyeeedar.Map

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Direction
import com.lyeeedar.Map.Objective.AbstractObjective
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.Event0Arg
import com.lyeeedar.Util.Event1Arg
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 24-Jul-16.
 */

class DungeonMap
{
	lateinit var theme: LevelTheme

	val playerPos: Point = Point()

	lateinit var objective: AbstractObjective

	val map: ObjectMap<Point, DungeonMapEntry> = ObjectMap()

	fun isFree(point: Point): Boolean = !map.containsKey(point)
	fun get(point: Point): DungeonMapEntry? = map[point]

	fun finishSetup()
	{
		for (room in map)
		{
			if (room.value.isRoom)
			{
				room.value.onComplete += {onRoomComplete(room.value)}
			}
		}

		onRoomComplete += {objective.update(this@DungeonMap, it)}
	}

	val onRoomComplete = Event1Arg<DungeonMapEntry>()
}

class DungeonMapEntry
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