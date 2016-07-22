package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.FastEnumMap
import com.lyeeedar.Util.Point

/**
 * Created by Philip on 04-Jul-16.
 */

class Orb(val desc: OrbDesc): Point()
{
	var armed: Boolean = false
		set(value)
		{
			field = value
		}

	var explosion: Explosion? = null
		set(value)
		{
			if (sinkable) return

			field = value

			if (value != null)
			{
				val nsprite = value.icon.copy()
				nsprite.colour = sprite.colour

				sprite = nsprite
			}
		}

	var markedForDeletion: Boolean = false

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
		explosion = orb.explosion
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

class Explosion
{
	val dirs: FastEnumMap<Direction, Array<Point>> = FastEnumMap(Direction::class.java)

	lateinit var icon: Sprite
	lateinit var sprite: Sprite

	var count: Int = -1
	lateinit var dir: Direction

	init
	{
		for (dir in Direction.CardinalValues)
		{
			dirs[dir] = Array()
		}
	}

	companion object
	{
		fun load(xml: XmlReader.Element) : Explosion
		{
			val special = Explosion()

			val hitPatternElement = xml.getChildByName("HitPattern")
			val hitGrid = Array(hitPatternElement.childCount, { CharArray(0) })
			val centralPoint = Point()

			for (y in 0..hitPatternElement.childCount-1)
			{
				val lineElement = hitPatternElement.getChild( y )
				val text = lineElement.text

				hitGrid[ y ] = text.toCharArray()

				for (x in 0..hitGrid[ y ].size-1)
				{
					if (hitGrid[ y ][ x ] == '@')
					{
						centralPoint.x = x
						centralPoint.y = y
					}
				}
			}

			for (y in 0..hitGrid.size-1)
			{
				for (x in 0..hitGrid[0].size-1)
				{
					if (hitGrid[y][x] != '.')
					{
						val dx = x - centralPoint.x
						val dy = centralPoint.y - y

						val dir = when(hitGrid[y][x])
						{
							'L'-> Direction.WEST
							'R' -> Direction.EAST
							'D' -> Direction.SOUTH
							'U' -> Direction.NORTH
							else -> Direction.CENTRE
						}

						if (dir != Direction.CENTRE) special.dirs[dir].add( Point(dx, dy) )
					}
				}
			}

			special.icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
			special.sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"))

			return special
		}
	}
}