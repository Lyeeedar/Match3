package com.lyeeedar.Player

import com.lyeeedar.Rarity
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 06-Aug-16.
 */

class Item
{
	lateinit var name: String
	lateinit var description: String
	var icon: Sprite? = null
	var value: Int = 0
	var rarity: Rarity = Rarity.COMMON

	var count = 1
}