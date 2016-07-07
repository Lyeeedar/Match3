package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
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

class Orb(val desc: OrbDesc)
{
	//val properties: Array<OrbProperty> = Array()
	var armed: Boolean = false
	var explosion: Explosion? = null

	var markedForDeletion: Boolean = false

	var fallCount = 0

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