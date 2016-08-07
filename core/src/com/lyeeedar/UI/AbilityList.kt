package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 06-Aug-16.
 */

class AbilityList(val playerData: PlayerData, val current: String?, val func: (String?) -> Unit): FullscreenTable()
{
	val emptySlot = AssetManager.loadSprite("Icons/Empty")
	val table = Table()

	init
	{
		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))

		this.add(table).expand().fill().pad(15f)

		val stack = Stack()
		table.add(stack).expand().fill()

		// build abilities
		val abilityTable = Table()
		abilityTable.defaults().pad(5f)

		if (current != null)
		{
			abilityTable.add(createButton(playerData.getAbility(current))).expandX().fillX()
			abilityTable.row()
		}

		abilityTable.add(createButton(null)).expandX().fillX()
		abilityTable.row()

		for (tree in playerData.trees)
		{
			for (skill in tree.value.boughtDescendants())
			{
				if (!playerData.abilities.contains(skill.key))
				{
					abilityTable.add(createButton(skill)).expandX().fillX()
					abilityTable.row()
				}
			}
		}

		val scroll = ScrollPane(abilityTable)
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

	fun createButton(ability: Ability?): Button
	{
		val sprite = ability?.icon?.copy() ?: emptySlot.copy()
		val name = ability?.name ?: "Empty"
		val description = ability?.description ?: null

		val textTable = Table()
		textTable.add(Label(name, Global.skin, "title")).expand().fill().left()
		if (description != null)
		{
			textTable.row()
			val label = Label(description, Global.skin)
			label.setWrap(true)
			textTable.add(label).expand().fill().left()
		}

		val spriteWidget = SpriteWidget(sprite, 48f, 48f)

		val button = Button(Global.skin)
		button.add(spriteWidget).padRight(10f).padLeft(10f)
		button.add(textTable).expand().fill()

		button.addClickListener {
			func(ability?.key)
			remove()
		}

		return button
	}
}