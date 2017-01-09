package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Renderables.Animation.BlinkAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.tryGet

/**
 * Created by Philip on 04-Jul-16.
 */

class Orb(val desc: OrbDesc, theme: LevelTheme): Swappable(theme)
{
	init
	{
		sprite = desc.sprite.copy()
	}

	var armed: ((point: Point, grid: Grid) -> Unit)? = null

	var special: Special? = null
		set(value)
		{
			field = value

			if (value != null)
			{
				val nsprite = value.sprite.copy()
				nsprite.colour = sprite.colour
				if (nsprite.colourAnimation == null) nsprite.colourAnimation = BlinkAnimation.obtain().set(nsprite.colour, 0.1f, 2.5f, false)

				sprite = nsprite
			}
		}

	var markedForDeletion: Boolean = false
	var deletionEffectDelay: Float = 0f
	var skipPowerOrb = false

	var hasAttack: Boolean = false
		set(value)
		{
			if (!field && value)
			{
				field = value
				val nsprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/skull_small", drawActualSize = true)
				nsprite.colour = sprite.colour
				sprite = nsprite
			}
		}

	var attackTimer = 0

	val key: Int
		get() = if (special is Match5) -1 else desc.key

	override val canMove: Boolean
		get() = !sealed && armed == null

	override fun toString(): String
	{
		return desc.key.toString()
	}

	fun setAttributes(orb: Orb)
	{
		sealCount = orb.sealCount
		hasAttack = orb.hasAttack
		attackTimer = orb.attackTimer
		special = orb.special
	}

	companion object
	{
		// ----------------------------------------------------------------------
		val validOrbs: Array<OrbDesc> = Array()

		fun getOrb(key: Int) = validOrbs.first { it.key == key }
		fun getOrb(name: String) = validOrbs.first { it.name == name }

		init
		{
			loadOrbs()
		}

		fun loadOrbs()
		{
			validOrbs.clear()

			val xml = XmlReader().parse(Gdx.files.internal("Orbs/Orbs.xml"))

			val template = xml.getChildByName("Template")
			val baseSprite = AssetManager.loadSprite(template.getChildByName("Sprite"))
			val deathEffect = AssetManager.loadParticleEffect(template.getChildByName("Death"))

			val types = xml.getChildByName("Types")
			for (i in 0..types.childCount-1)
			{
				val type = types.getChild(i)
				val name = type.name
				val colour = AssetManager.loadColour(type.getChildByName("Colour"))

				val orbDesc = OrbDesc()
				orbDesc.sprite = baseSprite.copy()
				orbDesc.sprite.colour = colour
				orbDesc.name = name

				orbDesc.death = deathEffect
				orbDesc.death.colour = colour

				validOrbs.add(orbDesc)
			}
		}
	}
}

class OrbDesc()
{
	constructor(sprite: Sprite, death: ParticleEffect, key: Int, name: String) : this()
	{
		this.sprite = sprite
		this.death = death
		this.name = name
		this.key = key
	}

	lateinit var sprite: Sprite
	lateinit var death: ParticleEffect
	var key: Int = -1
	var name: String = ""
		set(value)
		{
			field = value
			key = value.hashCode()
		}
}