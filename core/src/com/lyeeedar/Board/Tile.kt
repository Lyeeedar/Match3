package com.lyeeedar.Board

import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.Util.Point
import com.badlogic.gdx.utils.Array

/**
 * Created by Philip on 04-Jul-16.
 */

class Tile(x: Int, y: Int) : Point(x, y)
{
	lateinit var sprite: SpriteWrapper

	var orb: Orb? = null

	var connectedTo: Tile? = null
	var canSink: Boolean = false
	var canSpawn: Boolean = false
	var canHaveOrb: Boolean = true

	var isSelected: Boolean = false

	val effects: Array<Sprite> = Array()

	override fun toString(): String
	{
		return orb?.toString() ?: " "
	}

	fun getPosDiff(p: Point): FloatArray = getPosDiff(p.x, p.y)
	fun getPosDiff(px: Int, py: Int): FloatArray
	{
		val oldPos = floatArrayOf(px * Global.tileSize, py * Global.tileSize)
		val newPos = floatArrayOf(x * Global.tileSize, y * Global.tileSize)

		return floatArrayOf(oldPos[0]-newPos[0], (oldPos[1]-newPos[1]) * -1f)
	}
}
