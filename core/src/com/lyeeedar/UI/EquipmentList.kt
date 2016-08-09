package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Equipment.Equipment
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 06-Aug-16.
 */

class EquipmentList: FullscreenTable()
{
	val emptySlot = AssetManager.loadSprite("Icons/Empty")
	val table = Table()

	inline fun <reified T: Equipment> create(playerData: PlayerData, noinline func: () -> Unit)
	{
		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))

		this.add(table).expand().fill().pad(15f)

		val stack = Stack()
		table.add(stack).expand().fill()

		val equipmentTable = Table()
		equipmentTable.defaults().pad(5f)

		val current = playerData.getEquipment<T>()

		if (current != null)
		{
			equipmentTable.add(createButton<T>(current, playerData, func)).expandX().fillX()
			equipmentTable.row()
		}

		equipmentTable.add(createButton<T>(null, playerData, func)).expandX().fillX()
		equipmentTable.row()

		for (equipment in playerData.unlockedEquipment)
		{
			if (equipment != current && equipment is T)
			{
				equipmentTable.add(createButton<T>(equipment, playerData, func)).expandX().fillX()
				equipmentTable.row()
			}
		}

		val scroll = ScrollPane(equipmentTable)
		scroll.scrollTo(0f, 0f, 0f, 0f)
		stack.add(scroll)

		// close button
		val closeButton = Button(Global.skin, "close")
		closeButton.setSize(24f, 24f)
		closeButton.addClickListener({ remove() })
		val closeTable = Table()
		closeTable.add(closeButton).width(24f).height(24f).expand().top().right()
		stack.add(closeTable)
	}

	inline fun <reified T: Equipment> createButton(equipment: Equipment?, playerData: PlayerData, noinline func: () -> Unit): Button
	{
		val sprite = equipment?.icon?.copy() ?: emptySlot.copy()
		val name = equipment?.name ?: "Empty"
		val description = equipment?.description ?: null
		val stats = equipment?.stats() ?: null

		val textTable = Table()
		textTable.add(Label(name, Global.skin, "title")).expand().fill().left()
		if (description != null)
		{
			textTable.row()
			val label = Label(description, Global.skin)
			label.setWrap(true)
			textTable.add(label).expand().fill().left()
		}
		if (stats != null)
		{
			textTable.row()
			val label = Label(stats, Global.skin)
			textTable.add(label).expand().fill().left()
		}

		val spriteWidget = SpriteWidget(sprite, 48f, 48f)

		val button = Button(Global.skin)
		button.add(spriteWidget).padRight(10f).padLeft(10f)
		button.add(textTable).expand().fill()

		button.addClickListener {
			if (equipment != null) playerData.setEquipment(equipment)
			else playerData.clearEquipment<T>()
			func()
			remove()
		}

		return button
	}
}