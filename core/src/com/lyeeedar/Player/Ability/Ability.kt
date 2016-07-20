package com.lyeeedar.Player.Ability

import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 20-Jul-16.
 */

class Ability()
{
	constructor(icon: Sprite, cost: Int, elite: Boolean) : this()
	{
		this.icon = icon
		this.cost = cost
		this.elite = elite
	}

	lateinit var icon: Sprite
	var cost: Int = 2
	var elite: Boolean = false
}