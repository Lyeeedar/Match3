package com.lyeeedar.Player

import com.badlogic.gdx.utils.ObjectMap
import com.lyeeedar.Rarity
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getXml
import ktx.collections.set

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
		val allItems: ObjectMap<String, Item> by lazy {
			loadAll()
		}

		fun load(name: String): Item
		{
			return allItems[name].copy()
		}

		private fun loadAll() : ObjectMap<String, Item>
		{
			val map = ObjectMap<String, Item>()

			val xml = getXml("Items/Items.xml")
			for (i in 0..xml.childCount-1)
			{
				val el = xml.getChild(i)

				val item = Item()

				item.name = el.get("Name")
				item.description = el.get("Description")
				item.icon = AssetManager.loadSprite(el.getChildByName("Icon"))
				item.value = el.getInt("Value", 0)

				map[item.name] = item
			}

			return map
		}
	}
}