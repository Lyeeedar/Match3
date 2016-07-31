package com.lyeeedar.Board

import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Direction
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BlinkAnimation
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.random

/**
 * Created by Philip on 22-Jul-16.
 */

class Monster
{
	var hp = 10
		set(value)
		{
			if (value < field)
			{
				sprite.colourAnimation = BlinkAnimation.obtain().set(Color.RED, sprite.colour, 0.15f, true)
			}

			field = value
			if (field < 0) field = 0
		}

	var maxhp = 10

	var size = 2
		set(value)
		{
			field = value
			tiles = Array2D(size, size){ x, y -> Tile(0, 0) }
		}

	lateinit var tiles: Array2D<Tile>

	var sprite: Sprite = AssetManager.loadSprite("Oryx/uf_split/uf_heroes/rat_giant", updateTime = 0.5f, drawActualSize = true)

	var attackSpeed = 5
	var attackRate = 1
	var attackTimer = 0

	fun onTurn(grid: Grid)
	{
		attackTimer++
		if (attackTimer == attackRate)
		{
			attackTimer = 0

			// do attack
			val tile = grid.grid.filter { it.orb != null && !it.orb!!.sinkable && it.orb!!.special == null }.random()

			if (tile != null)
			{
				tile.orb!!.hasAttack = true
				tile.orb!!.attackTimer = attackSpeed
				sprite.spriteAnimation = BumpAnimation.obtain().set(0.2f, tile.getPosDiff(tiles[0, 0]))
			}
		}
	}
}