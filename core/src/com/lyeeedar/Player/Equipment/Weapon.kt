package com.lyeeedar.Player.Equipment

import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 09-Aug-16.
 */

class Weapon : Equipment()
{
	var specialEffect: Sprite? = null
	var spellDam: Int = 0
	var physDam: Int = 0

	override fun parse(xml: XmlReader.Element)
	{
		val effectEl = xml.getChildByName("Sprite")
		specialEffect = if (effectEl != null) AssetManager.loadSprite(effectEl) else null
		spellDam = xml.getInt("SpellDam")
		physDam = xml.getInt("PhysDam")
	}

	override fun createSimpleTable(): Table
	{
		val table = Table()

		if (icon.drawActualSize) table.add(SpriteWidget(icon.copy(), 32f, 32f, fixHeight = true))
		else table.add(SpriteWidget(icon.copy(), 32f, 32f))

		val wordsTable = Table()
		wordsTable.add(Label(name, Global.skin))
		wordsTable.row()

		val statsTable = Table()
		statsTable.add(Label("Spell Damage: $spellDam", Global.skin))
		statsTable.add(Label("Physical Damage: $physDam", Global.skin))

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