package com.lyeeedar.Town

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Equipment.Equipment
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Town.AbstractHouseInteraction.AbstractHouseInteraction
import com.lyeeedar.UI.UnlockTree
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getXml

/**
 * Created by Philip on 02-Aug-16.
 */

class House
{
	lateinit var sprite: Sprite

	var interactionIndex = -1
	val interactions = Array<AbstractHouseInteraction>()

	constructor(sprite: Sprite)
	{
		this.sprite = sprite
	}

	fun advance(playerData: PlayerData)
	{
		interactionIndex++
		if (interactionIndex == interactions.size)
		{
			interactionIndex = -1
			return
		}

		interactions[interactionIndex].apply(this, playerData)
	}

	companion object
	{
		fun load(path: String): House
		{
			val xml = getXml("Houses/$path")


			val sprite = AssetManager.loadSprite(xml.getChildByName("Sprite"))

			val house = House(sprite)

			val dialogueEl = xml.getChildByName("Dialogue")
			for (i in 0.. dialogueEl.childCount-1)
			{
				val el = dialogueEl.getChild(i)
				house.interactions.add(AbstractHouseInteraction.load(el))
			}

			return house
		}
	}
}

