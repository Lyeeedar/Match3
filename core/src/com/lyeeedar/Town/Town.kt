package com.lyeeedar.Town

import com.badlogic.gdx.utils.Array
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class Town(val playerData: PlayerData)
{
	val houses: Array<House> = Array()

	init
	{
		houses.add(House(AssetManager.loadSprite("Oryx/Custom/townmap/blacksmith"), playerData))
		houses.add(House(AssetManager.loadSprite("Oryx/Custom/townmap/pyro"), playerData))
		houses.add(House(AssetManager.loadSprite("Oryx/Custom/townmap/warrior"), playerData))
	}
}