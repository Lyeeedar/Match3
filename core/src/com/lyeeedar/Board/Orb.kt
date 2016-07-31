package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BlinkAnimation
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 04-Jul-16.
 */

class Orb(val desc: OrbDesc): Point()
{
	var armed: ((point: Point, grid: Grid) -> Unit)? = null

	var special: Special? = null
		set(value)
		{
			if (sinkable) return

			field = value

			if (value != null)
			{
				val nsprite = value.sprite.copy()
				nsprite.colour = sprite.colour
				if (nsprite.colourAnimation == null) nsprite.colourAnimation = BlinkAnimation.obtain().set(nsprite.colour, 0.3f, 2.5f, false)

				sprite = nsprite
			}
		}

	var markedForDeletion: Boolean = false
	var deletionEffectDelay: Float = 0f

	val sealSprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/shield_vorpal_buckler")
	val sealBreak = AssetManager.loadSprite("EffectSprites/Aegis/Aegis", 0.1f, Color(0.2f, 0f, 0.2f, 1f), Sprite.AnimationMode.TEXTURE, null, false, true)
	var sealed = false

	var hasAttack: Boolean = false
	var attackTimer = 0
	var attackIcon = AssetManager.loadSprite("Oryx/uf_split/uf_items/weapon_magic_sword_hellfire", drawActualSize = true)

	val sinkable: Boolean
		get() = desc.sinkable

	val key: Int
		get() = desc.key

	var sprite: Sprite = desc.sprite.copy()

	val movePoints = Array<Point>()
	var spawnCount = -1

	override fun toString(): String
	{
		return desc.key.toString()
	}

	fun setAttributes(orb: Orb)
	{
		sealed = orb.sealed
		hasAttack = orb.hasAttack
		attackTimer = orb.attackTimer
		special = orb.special
	}
}

class OrbDesc()
{
	constructor(sprite: Sprite, death: Sprite, sinkable: Boolean, key: Int, name: String) : this()
	{
		this.sprite = sprite
		this.death = death
		this.sinkable = sinkable
		this.name = name
		this.key = key
	}

	lateinit var sprite: Sprite
	lateinit var death: Sprite
	var sinkable = false
	var key: Int = -1
	var name: String = ""
		set(value)
		{
			field = value
			key = value.hashCode()
		}
}