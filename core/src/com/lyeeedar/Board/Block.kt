package com.lyeeedar.Board

import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 08-Jul-16.
 */

class Block
{
	val sprite = AssetManager.loadSprite("Oryx/uf_split/uf_terrain/crate")
	var count = 1

	init
	{
		sprite.drawActualSize = true
	}
}