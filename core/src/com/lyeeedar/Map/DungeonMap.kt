package com.lyeeedar.Map

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Direction
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 24-Jul-16.
 */

class DungeonMap
{
	lateinit var theme: LevelTheme

	val playerPos: Point = Point()

	val map: ObjectMap<Point, DungeonMapEntry> = ObjectMap()

	fun isFree(point: Point): Boolean = !map.containsKey(point)
	fun get(point: Point): DungeonMapEntry? = map[point]
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


	val isCompleted: Boolean
		get() = level?.completed ?: true
	val completedSprite: Sprite?
		get() = level?.completedMapSprite
	val uncompletedSprite: Sprite?
		get() = level?.uncompletedMapSprite
}