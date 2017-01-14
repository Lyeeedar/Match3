package com.lyeeedar.Player

import com.lyeeedar.Rarity
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getXml

/**
 * Created by Philip on 06-Aug-16.
 */

class Item
{
	lateinit var name: String
	lateinit var description: String
	lateinit var icon: Sprite
	var value: Int = 0

	var count = 1

	fun copy(): Item
	{
		val item = Item()

		item.name = name
		item.description = description
		item.icon = icon.copy()
		item.value = value

		return item
	}

	companion object
	{
		fun load(path: String): Item
		{
			val xml = getXml("Items/$path")

			val item = Item()

			item.name = xml.get("Name")
			item.description = xml.get("Description")
			item.icon = AssetManager.loadSprite(xml.getChildByName("Icon"))
			item.value = xml.getInt("Value", 0)

			return item
		}
	}
}