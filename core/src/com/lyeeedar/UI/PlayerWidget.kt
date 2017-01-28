package com.lyeeedar.UI

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Global
import com.lyeeedar.MainGame
import com.lyeeedar.Map.Objective.AbstractObjective
import com.lyeeedar.Player.Player
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Screens.TownScreen
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 29-Jul-16.
 */

class PlayerWidget(val player: Player, val parentWidget: DungeonMapWidget): FullscreenTable()
{
	val emptySlot = AssetManager.loadSprite("Icons/Empty")
	val hp_full: Sprite = AssetManager.loadSprite("GUI/health_full")
	val hp_empty: Sprite = AssetManager.loadSprite("GUI/health_empty")

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

		val portrait = Table()
		portrait.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))
		val sprite = player.portrait.copy()
		sprite.drawActualSize = true
		portrait.add(SpriteWidget(sprite, 48f, 48f)).padTop(10f)

		val hpBar = Table()
		val hpPipWidth = 48f / player.maxhp
		val hpPipHeight = 48f * 0.15f
		for (i in 0..player.maxhp-1)
		{
			val sprite = if(i < player.hp) hp_full else hp_empty
			val swidget = SpriteWidget(sprite, hpPipWidth, hpPipHeight)
			hpBar.add(swidget).bottom()
		}

		portrait.row()
		portrait.add(hpBar)

		val closeButtonTable = Table()
		val closeButton = Button(Global.skin, "close")
		closeButton.setSize(24f, 24f)
		closeButton.addClickListener({ remove() })
		closeButtonTable.add(closeButton).expand().width(24f).height(24f).top().right()

		val portraitTable = Table()
		portraitTable.add(portrait)

		val portraitStack = Stack()
		portraitStack.add(portraitTable)
		portraitStack.add(closeButtonTable)

		table.add(portraitStack).expandX().fillX().center().padBottom(10f)
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		val objectiveTable = Table()
		objectiveTable.defaults().pad(10f)
		objectiveTable.add(parentWidget.map.objective.createStaticTable(Global.skin)).expand().left()

		if (parentWidget.map.objective.isCompleted())
		{
			val button = TextButton("Complete\nQuest", Global.skin)
			button.addClickListener {
				MessageBox("Complete Quest", "Do you wish to complete the quest and return to town?",
						Pair("Yes", {this@PlayerWidget.remove(); parentWidget.completeDungeon()}),
						Pair("No", {}))
			}
			objectiveTable.add(button).expand().right()
		}
		else
		{
			val button = TextButton("Abandon\nQuest", Global.skin)
			button.addClickListener {
				MessageBox("Abandon Quest", "Do you wish you abandon the quest and return to town? You will lose half your collected gold if you leave.",
						Pair("Yes", {this@PlayerWidget.remove(); player.gold /= 2; parentWidget.completeDungeon()}),
						Pair("No", {}))
			}
			objectiveTable.add(button).expand().right()
		}

		table.add(objectiveTable).expandX().fillX()
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Abilities", Global.skin, "title")).padTop(5f)
		table.row()

		val abilityTable = Table()

		for (i in 0..3)
		{
			val ability = player.abilities[i]

			val sprite = ability?.icon?.copy() ?: emptySlot.copy()

			val widget = SpriteWidget(sprite, 48f, 48f)
			abilityTable.add(widget).expandX()

			if (ability != null)
			{
				widget.addClickListener {
					MessageBox(ability.name, ability.description, Pair("Okay", {}))
				}
			}
		}

		table.add(abilityTable).expandX().fillX().pad(10f)
		table.row()

		table.add(Seperator(Global.skin)).expandX().fillX()
		table.row()

		table.add(Label("Inventory", Global.skin, "title")).padTop(5f)
		table.row()

		val inventoryTable = Table()

		val goldButton = Button(Global.skin)
		goldButton.add(SpriteWidget(AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_gold", drawActualSize = true), 24f, 24f, true)).padRight(10f).padLeft(10f)
		goldButton.add(Label("Gold x ${player.gold}", Global.skin)).expand().fill()
		inventoryTable.add(goldButton).expandX().fillX()
		inventoryTable.row()

		for (item in player.inventory)
		{
			val button = Button(Global.skin)
			button.add(SpriteWidget(item.value.icon!!.copy(), 24f, 24f, true)).padRight(10f).padLeft(10f)
			button.add(Label("${item.value.name} x ${item.value.count}", Global.skin)).expand().fill()
			inventoryTable.add(button).expandX().fillX()
			inventoryTable.row()

			button.addClickListener {
				MessageBox(item.value.name, item.value.description, Pair("Okay", {}))
			}
		}

		val scroll = ScrollPane(inventoryTable)
		table.add(scroll).expand().fill()
	}
}