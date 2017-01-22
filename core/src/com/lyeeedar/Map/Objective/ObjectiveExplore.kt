package com.lyeeedar.Map.Objective

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Level
import com.lyeeedar.Global
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.DungeonMapEntry

/**
 * Created by Philip on 29-Jul-16.
 */

class ObjectiveExplore(): AbstractObjective()
{
	var label: Label = Label("Explore n more rooms", Global.skin)
	var complete = false

	override fun getDescription(): String = "Explore all the rooms in the dungeon to complete the quest"

	override fun update(map: DungeonMap)
	{
		val roomCount = map.map.filter { it.value.isRoom }.count()
		val exploredCount = map.map.filter { it.value.isRoom && it.value.isCompleted }.count()

		val diff = roomCount - exploredCount

		if (diff <= 0)
		{
			label.setText("Complete")
			complete = true
		}
		else if (diff == 1)
		{
			label.setText("Explore $diff more room")
		}
		else
		{
			label.setText("Explore $diff more rooms")
		}
	}

	override fun isCompleted(): Boolean = complete

	override fun createDynamicTable(skin: Skin): Table
	{
		val table = Table()

		table.add(Label("Explore", skin)).left()
		table.row()

		table.add(label).left()

		return table
	}

	override fun createStaticTable(skin: Skin): Table
	{
		val table = Table()

		table.add(Label("Explore", skin)).left()
		table.row()

		table.add(Label(label.text, skin)).left()

		return table
	}

	override fun getRequiredLevels(): Array<Level> = Array()

	override fun parse(xml: XmlReader.Element)
	{

	}
}