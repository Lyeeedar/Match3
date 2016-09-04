package com.lyeeedar.Board

import com.badlogic.gdx.utils.Array
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.Point

abstract class Swappable : Point()
{
	lateinit var sprite: Sprite

	val movePoints = Array<Point>()
	var spawnCount = -1
	var cascadeCount = 0

	abstract val canMove: Boolean
}
