package com.lyeeedar.Board

import com.lyeeedar.Board.CompletionCondition.CompletionConditionSink
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 22-Jul-16.
 */

class Chest(val spawnOrbs: Boolean = true, val theme: LevelTheme)
{
	var numToSpawn = 4
	var spacing = 3
	var spacingCounter = 0

	val sprite: Sprite
		get() = if (numToSpawn > 0) fullSprite else emptySprite

	val fullSprite = theme.chestFull.copy()
	val emptySprite = theme.chestEmpty.copy()

	fun attachHandlers(grid: Grid)
	{
		val victory = grid.level.victory
		if (victory is CompletionConditionSink)
		{
			// ensure we dont spawn too many orbs
			grid.onSpawn += {
				if (it is Sinkable)
				{
					val coinsOnBoard = grid.grid.filter { it.sinkable != null }.count() + 1
					val allowedToSpawn = victory.count - coinsOnBoard

					if (allowedToSpawn < numToSpawn)
					{
						numToSpawn = allowedToSpawn
					}
				}
			}
		}
	}

	fun spawn(grid: Grid): Swappable?
	{
		if (spawnOrbs)
		{
			if (numToSpawn <= 0) return Orb(Orb.validOrbs.random(), theme)

			// make sure we dont flood the board
			val coinsOnBoard = grid.grid.filter { it.sinkable != null }.count() + 1
			if (coinsOnBoard >= 7) return Orb(Orb.validOrbs.random(), theme)

			if (spacingCounter < spacing)
			{
				spacingCounter++
				return Orb(Orb.validOrbs.random(), theme)
			}
			else
			{
				spacingCounter = 0
				return Sinkable(theme.coin.copy())
			}
		}
		else
		{
			if (numToSpawn <= 0) return null
			return Sinkable(theme.coin.copy())
		}
	}
}