package com.lyeeedar.Map.Objective

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Level
import com.lyeeedar.Global
import com.lyeeedar.Map.DungeonMap

class ObjectiveEncounter : AbstractObjective()
{
	lateinit var levels: Array<Level>
	var label: Label = Label("Complete the encounter", Global.skin)
	var complete = false

	override fun getRequiredLevels(): Array<Level>
	{
		return levels
	}

	override fun update(map: DungeonMap)
	{
		val rooms = map.map.filter { it.value.level?.loadPath == levels[0].loadPath }
		val uncompletedCount = rooms.filter { !it.value.isCompleted }.count()

		if (uncompletedCount == 0)
		{
			label.setText("Complete")
			complete = true
		}
		else if (uncompletedCount > 1)
		{
			label.setText("Complete the encounters")
		}
		else
		{
			label.setText("Complete the encounter")
		}
	}

	override fun isCompleted(): Boolean = complete

	override fun createDynamicTable(skin: Skin): Table
	{
		val table = Table()

		table.add(Label("Encounter", skin)).left()
		table.row()

		table.add(label).left()

		return table
	}

	override fun createStaticTable(skin: Skin): Table
	{
		val table = Table()

		table.add(Label("Encounter", skin)).left()
		table.row()

		table.add(Label(label.text, skin)).left()

		return table
	}

	override fun parse(xml: XmlReader.Element)
	{
		levels = Level.load(xml.text)
	}
}