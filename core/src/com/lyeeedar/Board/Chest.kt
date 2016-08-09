package com.lyeeedar.Board

import com.lyeeedar.Board.CompletionCondition.CompletionConditionSink
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 22-Jul-16.
 */

class Chest(val spawnOrbs: Boolean = true)
{
	var numToSpawn = 4
	var spacing = 3
	var spacingCounter = 0

	val sprite: Sprite
		get() = if (numToSpawn > 0) fullSprite else emptySprite

	val fullSprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/chest_silver_open", drawActualSize = true)
	val emptySprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/chest_silver_empty", drawActualSize = true)

	val coinDesc = OrbDesc(AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_gold", drawActualSize = true), AssetManager.loadSprite("blank"), true, -1, "Coin")

	fun attachHandlers(grid: Grid)
	{
		val victory = grid.level.victory
		if (victory is CompletionConditionSink)
		{
			// ensure we dont spawn too many orbs
			grid.onSpawn += {
				if (it.sinkable == true)
				{
					val coinsOnBoard = grid.grid.filter { it.orb?.sinkable ?: false }.count() + 1
					val allowedToSpawn = victory.count - coinsOnBoard

					if (allowedToSpawn < numToSpawn)
					{
						numToSpawn = allowedToSpawn
					}
				}
			}
		}
	}

	fun spawn(grid: Grid): Orb?
	{
		if (spawnOrbs)
		{
			if (numToSpawn <= 0) return Orb(Orb.validOrbs.random())

			// make sure we dont flood the board
			val coinsOnBoard = grid.grid.filter { it.orb?.sinkable ?: false }.count() + 1
			if (coinsOnBoard >= 7) return Orb(Orb.validOrbs.random())

			if (spacingCounter < spacing)
			{
				spacingCounter++
				return Orb(Orb.validOrbs.random())
			}
			else
			{
				spacingCounter = 0
				return Orb(coinDesc)
			}
		}
		else
		{
			if (numToSpawn <= 0) return null
			return Orb(coinDesc)
		}
	}
}