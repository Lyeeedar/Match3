package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Equipment.Equipment
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 06-Aug-16.
 */

class PlayerDataWidget(val playerData: PlayerData) : FullscreenTable()
{
	val emptySlot = AssetManager.loadSprite("Icons/Empty")
	val table = Table()

	init
	{
		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))

		this.add(table).expand().fill().pad(10f)

		buildUI()
	}

	fun buildUI()
	{
		table.clear()

		val portraitTable = Table()
		portraitTable.defaults().pad(10f)

		val portraitLeft = TextButton("<-", Global.skin)
		portraitLeft.addClickListener {
			var index = playerData.unlockedSprites.indexOf(playerData.chosenSprite)
			index--
			if (index < 0) index = playerData.unlockedSprites.size-1

			playerData.chosenSprite = playerData.unlockedSprites[index]
			buildUI()
		}

		val portrait = Table()
		portrait.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))
		val sprite = playerData.chosenSprite.copy()
		portrait.add(SpriteWidget(sprite, 48f, 48f)).padTop(10f)

		val portraitRight = TextButton("->", Global.skin)
		portraitRight.addClickListener {
			var index = playerData.unlockedSprites.indexOf(playerData.chosenSprite)
			index++
			if (index == playerData.unlockedSprites.size) index = 0

			playerData.chosenSprite = playerData.unlockedSprites[index]
			buildUI()
		}

		portraitTable.add(portraitLeft).expand()
		portraitTable.add(portrait)
		portraitTable.add(portraitRight).expand()

		// close button
		val closeButtonTable = Table()
		val closeButton = Button(Global.skin, "close")
		closeButton.setSize(24f, 24f)
		closeButton.addClickListener({ remove() })
		closeButtonTable.add(closeButton).expand().width(24f).height(24f).top().right()

		val portraitStack = Stack()
		portraitStack.add(portraitTable)
		portraitStack.add(closeButtonTable)

		table.add(portraitStack).expandX().fillX().center()
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Abilities", Global.skin, "title")).padTop(5f)
		table.row()

		val abilityTable = Table()

		for (i in 0..3)
		{
			val abName = playerData.abilities[i]
			var sprite: Sprite

			if (abName != null)
			{
				val ability = playerData.getAbility(abName)
				sprite = ability?.icon?.copy() ?: emptySlot.copy()
			}
			else
			{
				sprite = emptySlot.copy()
			}

			val widget = SpriteWidget(sprite, 48f, 48f)
			abilityTable.add(widget).expandX()

			widget.addClickListener {
				val trees = Array<UnlockTree<Ability>>(playerData.skillTrees.size) { i -> UnlockTree() }
				var ti = 0
				for (tree in playerData.skillTrees) trees[ti++] = tree.value
				UnlockablesList<Ability>(abName, trees, { it -> playerData.abilities[i] = it; buildUI()}, { !playerData.abilities.contains(it.key) })
			}
		}

		table.add(abilityTable).expandX().fillX().pad(10f)
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Equipment", Global.skin, "title")).padTop(5f)
		table.row()

		val equipmentTable = Table()

		for (slot in Equipment.EquipmentSlot.values())
		{
			val equip = playerData.equipment[slot.ordinal]
			var sprite: Sprite

			if (equip != null)
			{
				val ability = playerData.getEquipment(equip)
				sprite = ability?.icon?.copy() ?: emptySlot.copy()
			}
			else
			{
				sprite = emptySlot.copy()
			}

			val widget = SpriteWidget(sprite, 48f, 48f)
			equipmentTable.add(widget).expandX()

			widget.addClickListener {
				val trees = arrayOf(playerData.equipTree)
				UnlockablesList<Equipment>(equip, trees, { it -> playerData.equipment[slot.ordinal] = it; buildUI()}, { it.slot == slot })
			}
		}

		table.add(equipmentTable).expandX().fillX().pad(10f)
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Inventory", Global.skin, "title")).padTop(5f)
		table.row()

		val inventoryTable = Table()

		val goldButton = Button(Global.skin)
		goldButton.add(SpriteWidget(AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_gold", drawActualSize = true), 24f, 24f, true)).padRight(10f).padLeft(10f)
		goldButton.add(Label("Gold x ${playerData.gold}", Global.skin)).expand().fill()
		inventoryTable.add(goldButton).expandX().fillX()
		inventoryTable.row()

		for (item in playerData.inventory)
		{
			val button = Button(Global.skin)
			button.add(SpriteWidget(item.value.icon!!.copy(), 24f, 24f, true)).padRight(10f).padLeft(10f)
			button.add(Label("${item.value.name} x ${item.value.count}", Global.skin)).expand().fill()
			inventoryTable.add(button).expandX().fillX()
			inventoryTable.row()
		}

		val scroll = ScrollPane(inventoryTable)
		table.add(scroll).expand().fill()
	}
}