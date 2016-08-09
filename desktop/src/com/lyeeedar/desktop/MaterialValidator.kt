package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.set
import java.io.File

/**
 * Created by Philip on 09-Aug-16.
 */

class MaterialValidator
{
	val materials = ObjectMap<String, ObjectSet<String>>() // name, paths
	val costs = ObjectMap<String, ObjectSet<String>>() // name, paths

	init
	{
		findFilesRecursive( File("") )

		for (cost in costs)
		{
			if (!materials.containsKey(cost.key))
			{
				val paths = cost.value.joinToString("\n")
				throw Exception("Material ${cost.key} does not exist! Referenced in files:\n$paths")
			}
		}

		var unusedMaterials = ""
		for (material in materials)
		{
			if (!costs.containsKey(material.key))
			{
				unusedMaterials += "\n${material.key}"
			}

			if (!File("Materials/${material.key}.xml").exists()) throw Exception("Material ${material.key} does not exist!")
		}

		if (unusedMaterials != "")
		{
			System.err.println("Unused Materials!\n$unusedMaterials")
			val outputHandle = FileHandle(File("UnusedMaterials.xml"))
			outputHandle.writeString(unusedMaterials, false)
		}
	}

	private fun addCost(name: String, path: String)
	{
		if (!costs.containsKey(name)) costs[name] = ObjectSet()
		costs[name].add(path)
	}

	private fun addMaterial(name: String, path: String)
	{
		if (!materials.containsKey(name)) materials[name] = ObjectSet()
		materials[name].add(path)
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

		val costs = xml.getChildByNameRecursive("BuyCost")
		for (ci in 0..costs.childCount-1)
		{
			val cost = costs.getChild(ci)
			for (i in 0..cost.childCount-1)
			{
				val el = cost.getChild(i)
				if (el.name != "Gold") addCost(el.name, path)
			}
		}

		val materials = xml.getChildByNameRecursive("Reward")
		for (mi in 0.. materials.childCount-1)
		{
			val material = materials.getChild(mi)
			for (i in 0..material.childCount-1)
			{
				val el = material.getChild(i)
				if (el.name != "Gold") addMaterial(el.name, path)
			}
		}
	}
}