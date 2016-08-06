package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Sprite.Sprite
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

		this.add(table).expand().fill().pad(15f)

		buildUI()
	}

	fun buildUI()
	{
		table.clear()

		// close button
		val closeButton = Button(Global.skin, "close")
		closeButton.setSize(24f, 24f)
		closeButton.addClickListener({ remove() })
		table.add(closeButton).width(24f).height(24f).expandX().top().right()
		table.row()

		val portraitTable = Table()
		portraitTable.defaults().pad(20f)

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
		portrait.add(SpriteWidget(sprite, 48, 48)).padTop(10f)

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

		table.add(portraitTable).expandX().center()
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Abilities", Global.skin, "title")).padTop(10f)
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

			val widget = SpriteWidget(sprite, 48, 48)
			abilityTable.add(widget).expandX()

			widget.addClickListener {
				AbilityList(playerData, abName, {it -> playerData.abilities[i] = it; buildUI()})
			}
		}

		table.add(abilityTable).expandX().fillX().pad(20f)
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Inventory", Global.skin, "title")).padTop(10f)
		table.row()

		val inventoryTable = Table()

		val goldButton = Button(Global.skin)
		goldButton.add(SpriteWidget(AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_gold", drawActualSize = true), 24, 24)).padRight(10f).padLeft(10f)
		goldButton.add(Label("Gold x ${playerData.gold}", Global.skin)).expand().fill()
		inventoryTable.add(goldButton).expandX().fillX()
		inventoryTable.row()

		for (item in playerData.inventory)
		{
			val button = Button(Global.skin)
			button.add(SpriteWidget(item.value.icon!!.copy(), 24, 24)).padRight(10f).padLeft(10f)
			button.add(Label("${item.value.name} x ${item.value.count}", Global.skin)).expand().fill()
			inventoryTable.add(button).expandX().fillX()
			inventoryTable.row()
		}

		val scroll = ScrollPane(inventoryTable)
		table.add(scroll).expand().fill()
	}
}