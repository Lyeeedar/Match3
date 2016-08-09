package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget

/**
 * Created by Philip on 09-Aug-16.
 */

class Armour : Equipment()
{
	var maxHP: Int = 0
	var regen: Int = 0

	override fun parse(xml: XmlReader.Element)
	{
		maxHP = xml.getInt("MaxHP")
		regen = xml.getInt("Regen")
	}

	override fun createSimpleTable(): Table
	{
		val table = Table()

		table.add(SpriteWidget(icon.copy(), 32f, 32f))

		val wordsTable = Table()
		wordsTable.add(Label(name, Global.skin))
		wordsTable.row()

		val statsTable = Table()
		statsTable.add(Label("Max HP: $maxHP", Global.skin))
		statsTable.add(Label("Regen: $regen", Global.skin))

		wordsTable.add(statsTable).expandX().fillX()

		table.add(wordsTable).expand().fill()

		return table
	}

	override fun createFullTable(): Table
	{
		val table = Table()

		return table
	}
}