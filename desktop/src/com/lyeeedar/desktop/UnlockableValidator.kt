package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.children
import com.lyeeedar.Util.getChildrenRecursively
import ktx.collections.get
import ktx.collections.set
import java.io.File

/**
 * Created by Philip on 09-Aug-16.
 */

class UnlockableValidator
{
	init
	{
		findFilesRecursive( File(".") )
	}

	private fun findFilesRecursive(dir: File)
	{
		val contents = dir.listFiles() ?: return

		for (file in contents)
		{
			if (file.isDirectory)
			{
				findFilesRecursive(file)
			}
			else if (file.path.endsWith(".xml"))
			{
				try
				{
					processFile(file.path)
				}
				catch (ex: Exception)
				{
					System.err.println(ex.message)
				}
			}
		}
	}

	private fun processFile(path: String)
	{
		val xml = XmlReader().parse(Gdx.files.internal(path))

		if (xml.name != "AbilityTree") return

		val resources = ObjectSet<String>()
		val resourcesEl = xml.getChildByName("Resources")
		if (resourcesEl != null)
		{
			for (i in 0..resourcesEl.childCount-1)
			{
				val el = resourcesEl.getChild(i)
				val key = el.getAttribute("Key")
				if (resources.contains(key)) throw Exception("Duplicate key '$key' in file '$path'")
				resources.add(key)
			}
		}

		val unlockables = ObjectMap<String, XmlReader.Element>()
		val unlockablesEl = xml.getChildByName("Abilities")
		for (i in 0..unlockablesEl.childCount-1)
		{
			val el = unlockablesEl.getChild(i)

			val name = el.get("Key")
			if (unlockables.containsKey(name)) throw Exception("Duplicate unlockable name $name. Path: $path")

			unlockables[name] = el
		}

		for (unlockable in unlockables)
		{
			val dataEl = unlockable.value.getChildByName("AbilityData")

			val upgrades = dataEl.get("Upgrades", null)
			if (upgrades != null)
			{
				if (!unlockables.containsKey(upgrades)) throw Exception("Upgraded unlockable does not exist! ${unlockable.key} -> $upgrades. Path: $path")
			}

			val effectDataEl = dataEl.getChildByName("EffectData")

			val flightSprite = effectDataEl.get("FlightSprite", null)
			if (flightSprite != null && !resources.contains(flightSprite)) throw Exception("Resource does not exist! Resource: $flightSprite, Unlockable: ${unlockable.key}, Path: $path")

			val hitSprite = effectDataEl.get("HitSprite", null)
			if (hitSprite != null && !resources.contains(hitSprite)) throw Exception("Resource does not exist! Resource: $hitSprite, Unlockable: ${unlockable.key}, Path: $path")

			val childrenEl = unlockable.value.getChildByName("Children")
			if (childrenEl != null)
			{
				for (child in childrenEl.children())
				{
					val childKey = child.text
					if (!unlockables.containsKey(childKey)) throw Exception("Linked child does not exist! ${unlockable.key} -> $childKey. Path: $path")
				}
			}
		}

		val tree = xml.getChildByName("Tree")
		for (child in tree.children())
		{
			val childKey = child.text
			if (!unlockables.containsKey(childKey)) throw Exception("Linked child does not exist! $childKey. Path: $path")
		}

	}
}