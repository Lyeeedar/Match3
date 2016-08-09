package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.getChildrenRecursively
import com.lyeeedar.Util.set
import java.io.File

/**
 * Created by Philip on 09-Aug-16.
 */

class AbilityValidator
{
	init
	{
		findFilesRecursive( File("Skills") )
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
				processFile(file.path)
			}
		}
	}

	private fun processFile(path: String)
	{
		val xml = XmlReader().parse(Gdx.files.internal(path))

		if (!xml.name.equals("SkillTree")) return

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

		val abilities = ObjectMap<String, XmlReader.Element>()
		val xmlAbilityNames = ObjectSet<String>()
		val abilitiesEl = xml.getChildByName("Abilities")
		for (i in 0..abilitiesEl.childCount-1)
		{
			val el = abilitiesEl.getChild(i)

			val name = el.get("Name")
			if (abilities.containsKey(name)) throw Exception("Duplicate ability name $name. Path: $path")

			abilities[name] = el

			if (xmlAbilityNames.contains(el.name)) throw Exception("Duplicate ability xml name ${el.name}. Path: $path")
			xmlAbilityNames.add(el.name)
		}

		for (ability in abilities)
		{
			val upgrades = ability.value.get("Upgrades", null)
			if (upgrades != null)
			{
				if (!abilities.containsKey(upgrades)) throw Exception("Upgraded ability does not exist! ${ability.key} -> $upgrades. Path: $path")
			}

			val flightSprite = ability.value.get("FlightSprite", null)
			if (flightSprite != null && !resources.contains(flightSprite)) throw Exception("Resource does not exist! Resource: $flightSprite, Ability: ${ability.key}, Path: $path")

			val hitSprite = ability.value.get("HitSprite", null)
			if (hitSprite != null && !resources.contains(hitSprite)) throw Exception("Resource does not exist! Resource: $hitSprite, Ability: ${ability.key}, Path: $path")
		}

		val tree = xml.getChildByName("Tree")
		for (el in tree.getChildrenRecursively())
		{
			if (!xmlAbilityNames.contains(el.name)) throw Exception("Ability in tree does not exist! Ability: ${el.name}, Path: $path")
		}

	}
}