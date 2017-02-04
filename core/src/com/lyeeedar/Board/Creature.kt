package com.lyeeedar.Board

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Renderables.Animation.BlinkAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.Colour

abstract class Creature(maxHp: Int, size: Int, sprite: Sprite, death: Sprite)
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

	val damSources = ObjectSet<Any>()

	init
	{
		this.maxhp = maxHp
		this.size = size
		this.sprite = sprite
		this.death = death
	}

	abstract fun onTurn(grid: Grid)

	fun getBorderTiles(grid: Grid, range: Int = 1): Sequence<Tile>
	{
		fun isBorder(tile: Tile) = tiles.map { it.dist(tile) }.min()!! <= range
		return grid.grid.filter(::isBorder)
	}
}