package com.lyeeedar.desktop

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import java.io.File

/**
 * Created by Philip on 28-Jul-16.
 */

class LevelProcessor
{
	val paths = Array<String>()

	init
	{
		findFilesRecursive( File("Levels") )

		var output = "<Levels>\n"

		for (path in paths)
		{
			output += "\t<Level>$path</Level>\n"
		}

		output += "</Levels>"

		val outputHandle = FileHandle(File("Levels/LevelList.xml"))
		outputHandle.writeString(output, false)
	}

	fun parseXml( path: String )
	{
		val xml = XmlReader().parse(Gdx.files.internal(path))

		if (!xml.name.equals("Level")) return

		var p = path
		p = p.replace("\\", "/")
		p = p.replace("Levels/", "")
		p = p.replace(".xml", "")

		paths.add(p)
		System.out.println("Adding level $p")
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
				parseXml(file.path)
			}
		}
	}
}