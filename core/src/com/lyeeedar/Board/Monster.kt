package com.lyeeedar.Board

import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.ColourAnimation
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.AssetManager

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
				sprite.colourAnimation = ColourAnimation.obtain().set(Color.RED, 0.15f, true)
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
}