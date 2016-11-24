package com.lyeeedar.Board

import com.badlogic.gdx.math.Vector2
import com.lyeeedar.Global
import com.lyeeedar.Util.Point
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.SpriteWrapper

/**
 * Created by Philip on 04-Jul-16.
 */

class Tile(x: Int, y: Int) : Point(x, y)
{
	lateinit var sprite: SpriteWrapper

	var orb: Orb?
		get() = contents as? Orb
		set(value) { contents = value }

	var sinkable: Sinkable?
		get() = contents as? Sinkable
		set(value) { contents = value }

	var shield: Shield?
		get() = contents as? Shield
		set(value) { contents = value }

	var swappable: Swappable?
		get() = contents as? Swappable
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
			if (value != null && !canHaveOrb)
			{
				com.lyeeedar.Util.error("Tried to put something in tile that can't should be empty. IsPit: $isPit, object: $value")
				return
			}
			field = value
		}

	var connectedTo: Tile? = null
	var canHaveOrb: Boolean = true
	var isPit: Boolean = false

	var isSelected: Boolean = false

	val effects: Array<Renderable> = Array()

	val associatedMatches = kotlin.Array<Match?>(2) {e -> null}

	override fun toString(): String
	{
		if (orb != null) return "o"
		if (sinkable != null) return "c"
		if (block != null) return "="
		if (monster != null) return "!"
		if (chest != null) return "£"

		return " "
	}

	fun getPosDiff(p: Point): kotlin.Array<Vector2> = getPosDiff(p.x, p.y)
	fun getPosDiff(px: Int, py: Int): kotlin.Array<Vector2>
	{
		val oldPos = Vector2(px.toFloat(), py.toFloat())
		val newPos = Vector2(x.toFloat(), y.toFloat())

		val diff = newPos.sub(oldPos)
		diff.x *= -1

		return arrayOf(diff, Vector2())
	}
}
