package com.lyeeedar.Board

import com.badlogic.gdx.math.Vector2
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

	var orb: Orb?
		get() = contents as? Orb
		set(value) { contents = value }

	var block: Block?
		get() = contents as? Block
		set(value) { contents = value }

	var contents: Any? = null

	var connectedTo: Tile? = null
	var canHaveOrb: Boolean = true

	var isSelected: Boolean = false

	val effects: Array<Sprite> = Array()

	override fun toString(): String
	{
		return orb?.toString() ?: " "
	}

	fun getPosDiff(p: Point): kotlin.Array<Vector2> = getPosDiff(p.x, p.y)
	fun getPosDiff(px: Int, py: Int): kotlin.Array<Vector2>
	{
		val oldPos = Vector2(px * Global.tileSize, py * Global.tileSize)
		val newPos = Vector2(x * Global.tileSize, y * Global.tileSize)

		val diff = newPos.sub(oldPos)
		diff.x *= -1

		return arrayOf(diff, Vector2())
	}
}
