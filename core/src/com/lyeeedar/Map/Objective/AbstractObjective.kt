package com.lyeeedar.Map.Objective

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.DungeonMapEntry

/**
 * Created by Philip on 29-Jul-16.
 */

abstract class AbstractObjective
{
	abstract fun update(map: DungeonMap, room: DungeonMapEntry)
	abstract fun isCompleted(): Boolean
	abstract fun createDynamicTable(skin: Skin): Table
	abstract fun createStaticTable(skin: Skin): Table
}