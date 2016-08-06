package com.lyeeedar.Player.Ability

import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Tile

/**
 * Created by Philip on 21-Jul-16.
 */

class Permuter(val type: Type)
{
	enum class Type
	{
		SINGLE,
		ALLOFTYPE,
		COLUMN,
		ROW,
		BLOCK1,
		BLOCK2,
		BLOCK3,
	}

	lateinit var permute: (tile: Tile, grid: Grid) -> Sequence<Tile>

	init
	{
		permute = when(type)
		{
			Type.SINGLE -> fun (tile: Tile, grid: Grid) = sequenceOf(tile)
			Type.ALLOFTYPE -> fun (tile: Tile, grid: Grid) = grid.grid.filter{ it.orb?.key == tile.orb!!.key }
			Type.COLUMN ->  fun (tile: Tile, grid: Grid) = grid.grid.filter{ it.x == tile.x }
			Type.ROW ->  fun (tile: Tile, grid: Grid) = grid.grid.filter{ it.y == tile.y }
			Type.BLOCK1 ->  fun (tile: Tile, grid: Grid) = grid.grid.filter{ it.taxiDist(tile) <= 1 }
			Type.BLOCK2 ->  fun (tile: Tile, grid: Grid) = grid.grid.filter{ it.taxiDist(tile) <= 2 }
			Type.BLOCK3 ->  fun (tile: Tile, grid: Grid) = grid.grid.filter{ it.taxiDist(tile) <= 3 }
			else -> throw Exception("Invalid permuter type $type")
		}
	}
}