package com.lyeeedar.Player.Ability

import com.badlogic.gdx.utils.XmlReader
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Tile

/**
 * Created by Philip on 21-Jul-16.
 */

class Targetter(val type: Type)
{
	enum class Type
	{
		ORB,
		BLOCK,
		EMPTY,
		SEALED,
		TILE
	}

	lateinit var isValid: (tile: Tile) -> Boolean

	init
	{
		isValid = when(type)
		{
			Type.ORB -> fun (tile: Tile) = tile.orb != null
			Type.BLOCK -> fun (tile: Tile) = tile.block != null
			Type.EMPTY -> fun (tile: Tile) = tile.contents == null && tile.canHaveOrb
			Type.SEALED -> fun (tile: Tile) = tile.orb?.sealed ?: false
			Type.TILE -> fun (tile: Tile) = tile.canHaveOrb
			else -> throw Exception("Invalid targetter type $type")
		}
	}
}