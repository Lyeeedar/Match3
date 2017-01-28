package com.lyeeedar.Player

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Grid
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget
import ktx.collections.get
import ktx.collections.set

/**
 * Created by Philip on 15-Jul-16.
 */

class Player()
{
	lateinit var portrait: Sprite

	var hp: Int = 0
		set(value)
		{
			field = value
			if (field < 0) field = 0
			if (field > maxhp) field = maxhp
		}

	var maxhp: Int = 10

	var gold: Int = 0
	val inventory = ObjectMap<String, Item>()

	// abilities and stuff
	val abilities = Array<Ability?>(4){e -> null}

	constructor(data: PlayerData) : this()
	{
		portrait = data.chosenSprite
		portrait.drawActualSize = false

		hp = maxhp

		for (i in 0..3)
		{
			abilities[i] = if (data.abilities[i] != null) data.getAbility(data.abilities[i]!!) else null
		}
	}

	fun addItem(item: Item)
	{
		val existing = inventory[item.name]

		if (existing != null)
		{
			existing.count += item.count
		}
		else
		{
			inventory[item.name] = item
		}
	}

	fun removeItem(item: Item)
	{
		val existing = inventory[item.name]

		if (existing != null)
		{
			existing.count -= item.count
			if (existing.count <= 0)
			{
				inventory.remove(existing.name)
			}
		}
	}
}