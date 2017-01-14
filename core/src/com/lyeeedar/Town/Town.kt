package com.lyeeedar.Town

import com.badlogic.gdx.utils.Array
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Player.SaveGame
import com.lyeeedar.Player.SavePlayerData
import com.lyeeedar.Player.SaveTown
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.getXml

/**
 * Created by Philip on 02-Aug-16.
 */

class Town(val playerData: PlayerData)
{
	val playerPos = Point(-1, -1)
	val houses: Array<House> = Array()

	init
	{
		val xml = getXml("Houses/Houses")
		for (i in 0..xml.childCount-1)
		{
			val el = xml.getChild(i)
			houses.add(House.load(el.name))
		}
	}

	fun save()
	{
		val save = SaveGame()
		save.town = SaveTown().store(this)
		save.playerData = SavePlayerData().store(playerData)

		save.save()
	}
}