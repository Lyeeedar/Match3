package com.lyeeedar.Board

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Player.Ability.Effect
import com.lyeeedar.Player.Ability.Permuter
import com.lyeeedar.Player.Ability.Targetter
import com.lyeeedar.Renderables.Animation.*
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.*
import ktx.collections.get
import ktx.collections.set

/**
 * Created by Philip on 22-Jul-16.
 */

class Monster(val desc: MonsterDesc)
{
	var hp: Int = 1
		set(value)
		{
			if (value < field)
			{
				sprite.colourAnimation = BlinkAnimation.obtain().set(Colour(Color.RED), sprite.colour, 0.15f, true)
			}

			field = value
			if (field < 0) field = 0
		}

	var maxhp: Int = 1
		set(value)
		{
			field = value
			hp = value
		}

	var size = 2
		set(value)
		{
			field = value
			tiles = Array2D(size, size){ x, y -> Tile(0, 0) }
		}

	lateinit var tiles: Array2D<Tile>

	lateinit var sprite: Sprite
	lateinit var death: Sprite

	var attackSpeed: Int = 5
	var attackDelay: Int = 5
	var attackAccumulator: Int = 1

	val damSources = ObjectSet<Any>()

	val rewards = ObjectMap<String, Pair<Int, Int>>()

	val abilities = Array<MonsterAbility>()
	var abilityCooldown = 10
	var abilityRate = 10

	init
	{
		attackSpeed = desc.attackSpeed
		attackDelay = desc.attackDelay
		abilityRate = desc.abilityRate
		size = desc.size
		sprite = desc.sprite.copy()
		death = desc.death.copy()
		maxhp = desc.hp
		abilities.addAll(desc.abilities)

		attackAccumulator = (MathUtils.random() * attackDelay).toInt()

		abilityCooldown = (MathUtils.random() * abilityRate).toInt() + abilityRate / 2

		for (reward in desc.rewards)
		{
			rewards[reward.key] = reward.value
		}
	}

	fun onTurn(grid: Grid)
	{
		attackAccumulator++
		if (attackAccumulator >= attackDelay)
		{
			attackAccumulator -= attackDelay

			// do attack
			val tile = grid.grid.filter { it.orb != null && it.orb!!.special == null && !it.orb!!.hasAttack }.random()

			if (tile != null)
			{
				tile.orb!!.attackTimer = attackSpeed
				val diff = tile.getPosDiff(tiles[0, 0])
				diff[0].y *= -1
				sprite.animation = BumpAnimation.obtain().set(0.2f, diff)

				val dst = tile.euclideanDist(tiles[0, 0])
				val animDuration = 0.25f + tile.euclideanDist(tiles[0, 0]) * 0.025f
				val attackSprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/skull_small", drawActualSize = true)
				attackSprite.colour = tile.orb!!.sprite.colour
				attackSprite.animation = LeapAnimation.obtain().set(animDuration, tile.getPosDiff(tiles[0, 0]), 1f + dst * 0.25f)
				attackSprite.animation = ExpandAnimation.obtain().set(animDuration, 0.5f, 1.5f, false)
				tile.effects.add(attackSprite)

				tile.orb!!.delayDisplayAttack = animDuration
			}
		}

		abilityCooldown--
		if (abilityCooldown <= 0)
		{
			if (MathUtils.randomBoolean())
			{
				abilityCooldown = (MathUtils.random() * (abilityRate / 2f)).toInt() + abilityRate / 2

				if (abilities.size > 0)
				{
					val ability = abilities.random()
					ability.activate(grid, this)
				}
			}
		}
	}
}

class MonsterAbility
{
	enum class Target
	{
		NEIGHBOUR,
		RANDOM
	}

	enum class Effect
	{
		ATTACK,
		SHIELD,
		SEAL,
		MOVE
	}

	lateinit var target: Target
	lateinit var targetRestriction: Targetter
	var targetCount: Int = 1
	lateinit var permuter: Permuter
	lateinit var effect: Effect
	val data = ObjectMap<String, String>()

	fun activate(grid: Grid, monster: Monster)
	{
		val availableTargets = Array<Tile>()

		if (target == Target.NEIGHBOUR)
		{
			for (tile in grid.grid)
			{
				val minDiff = monster.tiles.map { it.dist(tile) }.min()!!
				if (minDiff <= 1)
				{
					availableTargets.add(tile)
				}
			}
		}
		else if (target == Target.RANDOM)
		{
			availableTargets.addAll(grid.grid)
		}
		else
		{
			throw NotImplementedError()
		}

		val validTargets = availableTargets.filter { targetRestriction.isValid(it, data) }

		val chosen = validTargets.asSequence().random(targetCount)

		val finalTargets = Array<Tile>()

		for (target in chosen)
		{
			for (t in permuter.permute(target, grid, data))
			{
				if (!finalTargets.contains(t, true))
				{
					finalTargets.add(t)
				}
			}
		}

		if (effect == Effect.MOVE)
		{
			fun isValid(t: Tile): Boolean
			{
				for (x in 0..monster.size-1)
				{
					for (y in 0..monster.size-1)
					{
						val tile = grid.tile(t.x + x, t.y + y) ?: return false

						if (tile.orb != tile.contents && tile.monster != monster)
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

			val target = validTargets.filter(::isValid).asSequence().random()

			if (target != null)
			{
				val start = monster.tiles.first()

				for (tile in monster.tiles)
				{
					tile.monster = null
				}
				for (x in 0..monster.size-1)
				{
					for (y in 0..monster.size - 1)
					{
						val tile = grid.tile(target.x + x, target.y + y)!!

						if (tile.orb != null)
						{
							val orb = tile.orb!!

							val sprite = orb.desc.death.copy()
							sprite.colour = orb.sprite.colour

							tile.effects.add(sprite)
						}

						tile.monster = monster
						monster.tiles[x, y] = tile
					}
				}

				val end = monster.tiles.first()

				monster.sprite.animation = MoveAnimation.obtain().set(0.25f, UnsmoothedPath(end.getPosDiff(start)), Interpolation.linear)
			}

			return
		}

		for (target in finalTargets)
		{
			val strength = data.get("Strength", "1").toInt()

			if (effect == Effect.ATTACK)
			{
				target.orb!!.attackTimer = monster.attackSpeed
				val diff = target.getPosDiff(monster.tiles[0, 0])
				diff[0].y *= -1
				monster.sprite.animation = BumpAnimation.obtain().set(0.2f, diff)

				val dst = target.euclideanDist(monster.tiles[0, 0])
				val animDuration = 0.25f + dst * 0.025f
				val attackSprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/skull_small", drawActualSize = true)
				attackSprite.colour = target.orb!!.sprite.colour
				attackSprite.animation = LeapAnimation.obtain().set(animDuration, target.getPosDiff(monster.tiles[0, 0]), 1f + dst * 0.25f)
				attackSprite.animation = ExpandAnimation.obtain().set(animDuration, 0.5f, 1.5f, false)
				target.effects.add(attackSprite)

				target.orb!!.delayDisplayAttack = animDuration
			}
			else if (effect == Effect.SHIELD)
			{
				target.shield = Shield(grid.level.theme)
				target.shield!!.count = strength
			}
			else if (effect == Effect.SEAL)
			{
				target.swappable?.sealCount = strength
			}
			else if (effect == Effect.MOVE)
			{

			}
			else throw NotImplementedError()
		}
	}

	companion object
	{
		fun load(xml: XmlReader.Element) : MonsterAbility
		{
			val ability = MonsterAbility()

			ability.target = Target.valueOf(xml.get("Target", "NEIGHBOUR").toUpperCase())
			ability.targetCount = xml.getInt("Count", 1)

			ability.targetRestriction = Targetter(Targetter.Type.valueOf(xml.get("TargetRestriction", "Orb").toUpperCase()))
			ability.permuter = Permuter(Permuter.Type.valueOf(xml.get("Permuter", "Single").toUpperCase()))
			ability.effect = Effect.valueOf(xml.get("Effect", "Attack").toUpperCase())

			val dEl = xml.getChildByName("Data")
			if (dEl != null)
			{
				for (i in 0..dEl.childCount-1)
				{
					val el = dEl.getChild(i)
					val text = el.text.toUpperCase()
					val split = text.split(",")

					ability.data[split[0]] = split[1]
				}
			}

			return ability
		}
	}
}