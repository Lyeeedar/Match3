package com.lyeeedar.Board

import com.lyeeedar.Renderables.Sprite.Sprite

class Sinkable : Swappable
{
	override val canMove: Boolean
		get() = true

	constructor(sprite: Sprite)
		: super()
	{
		this.sprite = sprite
	}


}
