package com.lyeeedar.Board

import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 08-Jul-16.
 */

class Block
{
	val sprite = AssetManager.loadSprite("Oryx/uf_split/uf_terrain/crate", drawActualSize = true)
	val death = AssetManager.loadSprite("EffectSprites/Hit/Hit", 0.1f)
	var count = 1
}