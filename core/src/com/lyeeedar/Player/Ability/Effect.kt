package com.lyeeedar.Player.Ability

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Match5
import com.lyeeedar.Board.Orb
import com.lyeeedar.Board.Tile

/**
 * Created by Philip on 21-Jul-16.
 */

class Effect(val type: Type)
{
	enum class Type
	{
		POP,
		RANDOMISE,
		TEST
	}

	lateinit var apply: (tile: Tile, grid: Grid, delay: Float, data: ObjectMap<String, String>) -> Unit

	init
	{
		apply = when(type)
		{
			Type.POP -> fun (tile: Tile, grid: Grid, delay: Float, data: ObjectMap<String, String>) { grid.pop(tile, delay, skipPowerOrb = true) }
			Type.RANDOMISE -> fun (tile: Tile, grid: Grid, delay: Float, data: ObjectMap<String, String>) { val orb = tile.orb ?: return; tile.orb = Orb(grid.validOrbs.random()); tile.orb!!.setAttributes(orb) }
			Type.TEST ->  fun (tile: Tile, grid: Grid, delay: Float, data: ObjectMap<String, String>) { val orb = tile.orb ?: return; orb.special = Match5(orb) }
			else -> throw Exception("Invalid effect type $type")
		}
	}
}