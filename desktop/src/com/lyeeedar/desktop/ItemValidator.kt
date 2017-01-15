package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import ktx.collections.get
import ktx.collections.set
import java.io.File

/**
 * Created by Philip on 09-Aug-16.
 */

class ItemValidator
{
	val items = ObjectMap<String, ObjectSet<String>>() // name, paths
	val drops = ObjectMap<String, ObjectSet<String>>() // name, paths
	val costs = ObjectMap<String, ObjectSet<String>>() // name, paths

	init
	{
		findFilesRecursive( File(".") )

		for (cost in costs)
		{
			if (!drops.containsKey(cost.key))
			{
				val paths = cost.value.joinToString("\n")
				throw Exception("Cost ${cost.key} is never dropped! Referenced in files:\n$paths")
			}
		}

		for (drop in drops)
		{
			if (!items.containsKey(drop.key))
			{
				val paths = drop.value.joinToString("\n")
				throw Exception("Drop ${drop.key} does not exist! Referenced in files:\n$paths")
			}
		}

		var unuseditems = ""
		for (item in items)
		{
			if (!drops.containsKey(item.key))
			{
				unuseditems += "\n${item.key}"
			}
		}

		if (unuseditems != "")
		{
			System.err.println("Unused items!\n$unuseditems")
			val outputHandle = FileHandle(File("Unuseditems.xml"))
			outputHandle.writeString(unuseditems, false)
		}
	}

	private fun addCost(name: String, path: String)
	{
		if (!costs.containsKey(name)) costs[name] = ObjectSet()
		costs[name].add(path)
	}

	private fun addDrop(name: String, path: String)
	{
		if (!drops.containsKey(name)) drops[name] = ObjectSet()
		drops[name].add(path)
	}

	private fun addItem(name: String, path: String)
	{
		if (!items.containsKey(name)) items[name] = ObjectSet()
		items[name].add(path)
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
					System.err.println(ex.message!!)
				}
			}
		}
	}

	private fun processFile(path: String)
	{
		val xml = XmlReader().parse(Gdx.files.internal(path))

		val costsEls = xml.getChildrenByNameRecursively("BuyCost")
		for (costs in costsEls)
		{
			for (ci in 0..costs.childCount - 1)
			{
				val costEl = costs.getChild(ci)
				val cost = costEl.text.split(",")[0]
				if (cost != "Gold") addCost(cost, path)
			}
		}

		val itemEls = xml.getChildrenByNameRecursively("Item")
		for (itemEl in itemEls)
		{
			if (itemEl.childCount == 0)
			{
				val cost = itemEl.text.split(",")[0]
				if (cost != "Gold") addDrop(cost, path)
			}
			else
			{
				val name = itemEl.get("Name")
				addItem(name, path)
			}
		}
	}
}