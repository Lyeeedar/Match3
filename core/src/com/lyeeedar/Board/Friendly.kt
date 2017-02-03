package com.lyeeedar.Board

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.UnsmoothedPath
import com.lyeeedar.Util.addAll
import com.lyeeedar.Util.random

class Friendly(val desc: FriendlyDesc) : Creature(desc.hp, desc.size, desc.sprite.copy(), desc.death.copy())
{
	override fun onTurn(grid: Grid)
	{
		val border = getBorderTiles(grid)
		for (tile in border)
		{
			if (tile.orb != null && tile.orb!!.hasAttack && !tile.orb!!.sealed)
			{
				grid.pop(tile, 0f)
				hp -= 1

				val closest = tiles.minBy { tile.euclideanDist2(it) }
				closest!!.effects.add(grid.hitSprite.copy())
			}
		}
	}

}

class FriendlyDesc
{
	lateinit var sprite: Sprite
	lateinit var death: Sprite
	var size: Int = 1
	var hp: Int = 25
}

abstract class FriendlyAbility
{
	var cooldownTimer: Int = 0
	var cooldownMin: Int = 1
	var cooldownMax: Int = 1
}

class MoveAbility : FriendlyAbility()
{
	enum class Target
	{
		NEIGHBOUR,
		RANDOM
	}

	enum class Destination
	{
		ATTACK,
		MONSTER,
		BLOCK,
		RANDOM
	}

	lateinit var target: Target
	lateinit var destination: Destination

	fun activate(friendly: Friendly, grid: Grid)
	{
		val availableTargets = Array<Tile>()

		if (target == Target.NEIGHBOUR)
		{
			availableTargets.addAll(friendly.getBorderTiles(grid))
		}
		else if (target == Target.RANDOM)
		{
			availableTargets.addAll(grid.grid)
		}
		else
		{
			throw NotImplementedError()
		}

		fun isValid(t: Tile): Boolean
		{
			for (x in 0..friendly.size-1)
			{
				for (y in 0..friendly.size-1)
				{
					val tile = grid.tile(t.x + x, t.y + y) ?: return false

					if (tile.orb != tile.contents && tile.friendly != friendly)
					{
						return false
					}

					if (!tile.canHaveOrb)
					{
						return false
					}
				}
			}

			return true
		}

		val validTargets = availableTargets.filter(::isValid)
		val destinations = when (destination)
		{
			Destination.ATTACK -> grid.grid.filter { it.orb != null && it.orb!!.hasAttack }
			Destination.MONSTER -> grid.grid.filter { it.monster != null }
			Destination.BLOCK -> grid.grid.filter { it.block != null }
			Destination.RANDOM -> grid.grid.asSequence()
			else -> return
		}

		if (destinations.count() == 0) return

		var chosen: Tile? = null

		if (destination == Destination.RANDOM)
		{
			chosen = validTargets.asSequence().random()
		}
		else
		{
			var current = Int.MAX_VALUE
			for (t in destinations)
			{
				for (tile in friendly.tiles)
				{
					val dist = tile.dist(t)
					if (dist < current) current = dist
				}
			}

			var chosenMin = Int.MAX_VALUE
			for (target in validTargets)
			{
				for (t in destinations)
				{
					val dist = target.dist(t)
					if (dist < chosenMin)
					{
						chosenMin = dist
						chosen = target
					}
				}
			}
		}

		if (chosen != null)
		{
			val start = friendly.tiles.first()

			for (tile in friendly.tiles)
			{
				tile.monster = null
			}
			for (x in 0..friendly.size-1)
			{
				for (y in 0..friendly.size - 1)
				{
					val tile = grid.tile(chosen.x + x, chosen.y + y)!!

					if (tile.orb != null)
					{
						val orb = tile.orb!!

						val sprite = orb.desc.death.copy()
						sprite.colour = orb.sprite.colour

						tile.effects.add(sprite)
					}

					tile.friendly = friendly
					friendly.tiles[x, y] = tile
				}
			}

			val end = friendly.tiles.first()

			friendly.sprite.animation = MoveAnimation.obtain().set(0.25f, UnsmoothedPath(end.getPosDiff(start)), Interpolation.linear)
		}
	}
}