package com.lyeeedar.Board

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Direction
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BlinkAnimation
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Sprite.SpriteAnimation.ExtendAnimation
import com.lyeeedar.Util.*

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
				sprite.colourAnimation = BlinkAnimation.obtain().set(Color.RED, sprite.colour, 0.15f, true)
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
	var attackDelay: Float = 5f
	var attackAccumulator: Float = 1f

	val damSources = ObjectSet<Any>()

	val rewards = ObjectMap<String, Int>()

	init
	{
		attackSpeed = desc.attackSpeed
		attackDelay = desc.attackDelay
		size = desc.size
		sprite = desc.sprite.copy()
		death = desc.death.copy()
		maxhp = desc.hp

		attackAccumulator = MathUtils.random() * attackDelay

		for (reward in desc.rewards)
		{
			rewards[reward.key] = reward.value
		}
	}

	fun onTurn(grid: Grid)
	{
		attackAccumulator += 1f
		if (attackAccumulator >= attackDelay)
		{
			attackAccumulator -= attackDelay

			// do attack
			val tile = grid.grid.filter { it.orb != null && !it.orb!!.sinkable && it.orb!!.special == null && !it.orb!!.hasAttack }.random()

			if (tile != null)
			{
				tile.orb!!.hasAttack = true
				tile.orb!!.attackTimer = attackSpeed
				val diff = tile.getPosDiff(tiles[0, 0])
				diff[0].y *= -1
				sprite.spriteAnimation = BumpAnimation.obtain().set(0.2f, diff)

				val beam = AssetManager.loadSprite("EffectSprites/Beam/Beam")
				beam.rotation = getRotation(tiles[0, 0], tile) * -1
				beam.spriteAnimation = ExtendAnimation.obtain().set(0.25f, tile.getPosDiff(tiles[0, 0]))
				beam.colour = Color.RED
				tiles[0, 0].effects.add(beam)
			}
		}
	}
}