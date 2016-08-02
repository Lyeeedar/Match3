package com.lyeeedar.Town

import com.badlogic.gdx.utils.Array
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class Town
{
	val houses: Array<House> = Array()

	init
	{
		houses.add(House(AssetManager.loadSprite("Oryx/Custom/townmap/blacksmith")))
		houses.add(House(AssetManager.loadSprite("Oryx/Custom/townmap/pyro")))
		houses.add(House(AssetManager.loadSprite("Oryx/Custom/townmap/warrior")))
	}
}