package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 04-Jul-16.
 */

class Orb(val desc: OrbDesc)
{
	//val properties: Array<OrbProperty> = Array()
	var armed: Boolean = false

	var markedForDeletion: Boolean = false

	val key: Int
		get() = desc.key

	val sprite: Sprite = desc.sprite.copy()

	override fun toString(): String
	{
		return desc.key.toString()
	}
}

class OrbDesc
{
	lateinit var sprite: Sprite
	var key: Int = -1

	var canSink: Boolean = false
	var canMove: Boolean = true
	var isWildCard: Boolean = false
	var destroyOnNeighbourMatch: Boolean = false

	init
	{
		key = KeyCounter++
	}

	companion object
	{
		var KeyCounter = 0

		fun load(name: String) : OrbDesc
		{
			val xml = XmlReader().parse(Gdx.files.internal("Orbs/$name.xml"))

			val orb = OrbDesc()

			orb.sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"))

			return orb
		}
	}
}