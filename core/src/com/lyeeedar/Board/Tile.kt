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

	var chest: Chest?
		get() = contents as? Chest
		set(value) { contents = value }

	var monster: Monster?
		get() = contents as? Monster
		set(value) { contents = value }

	var contents: Any? = null
		set(value)
		{
			if (field != null && !canHaveOrb)
			{
				error("Tried to put something in tile that can't should be empty. IsPit: $isPit")
				return
			}
			field = value
		}

	var connectedTo: Tile? = null
	var canHaveOrb: Boolean = true
	var isPit: Boolean = false

	var isSelected: Boolean = false

	val effects: Array<Sprite> = Array()

	val associatedMatches = kotlin.Array<Match?>(2) {e -> null}

	override fun toString(): String
	{
		if (orb != null) return "o"
		if (block != null) return "="
		if (monster != null) return "!"
		if (chest != null) return "£"

		return " "
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
