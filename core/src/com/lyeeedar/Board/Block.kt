package com.lyeeedar.Board

import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.tryGet

/**
 * Created by Philip on 08-Jul-16.
 */

class Block(val theme: LevelTheme)
{
	var sprite = theme.blockSprites.tryGet(0).copy()

	val death = AssetManager.loadSprite("EffectSprites/Hit/Hit", 0.1f)
	var count = 1
		set(value)
		{
			field = value
			sprite = theme.blockSprites.tryGet(count-1).copy()
		}
}