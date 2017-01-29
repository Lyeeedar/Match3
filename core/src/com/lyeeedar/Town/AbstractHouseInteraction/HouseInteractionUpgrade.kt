package com.lyeeedar.Town.AbstractHouseInteraction

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Town.House
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

class HouseInteractionUpgrade : AbstractHouseInteraction()
{
	override fun apply(house: House, playerData: PlayerData)
	{
		val names = arrayOf("Max Health", "Attack Dam", "Ability Dam", "Power Gain")
		val keys = arrayOf("HpLvl", "AtkLvl", "AbLvl", "PgLvl")
		val baseCosts = intArrayOf(500, 2000, 750, 500)
		val lvls = IntArray(4)
		val costs = IntArray(4)

		for (i in 0..lvls.size-1)
		{
			val key = keys[i]
			val baseCost = baseCosts[i]

			val lvl = Global.settings.get(key, 0)
			lvls[i] = lvl
			costs[i] = baseCost + (baseCost.toDouble() * Math.sqrt(lvl.toDouble())).toInt()
		}

		val widget = Table()
		val closeButton = Button(Global.skin, "close")

		val table = ktx.scene2d.table {
			defaults().growX().pad(10f)
			background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))

			for (i in 0..lvls.size-1)
			{
				table {
					defaults().grow().pad(5f)
					label(names[i], "default", Global.skin)
					label((lvls[i]+1).toString(), "default", Global.skin)
					label(costs[i].toString() + "g", "default", Global.skin)

					if (playerData.gold >= costs[i])
					{
						textButton("Purchase", "default", Global.skin) {
							height = 50f
							addClickListener {

								if (i == 0) playerData.maxhp += 5
								else if (i == 1) playerData.attackDam += 1
								else if (i == 2) playerData.abilityDam += 1

								Global.settings.set(keys[i], lvls[i]+1)
								playerData.gold -= costs[i]
								widget.remove()
								closeButton.remove()
								apply(house, playerData)
							}
						}
					}
					else
					{
						label("" + playerData.gold + "/" + costs[i], "default", Global.skin)
					}
				}
				row()
			}
		}

		widget.add(table).pad(20f).grow()
		widget.row()

		widget.setFillParent(true)
		Global.stage.addActor(widget)

		closeButton.setSize(24f, 24f)
		closeButton.addClickListener({ widget.remove(); closeButton.remove(); house.advance(playerData) })
		Global.stage.addActor(closeButton)
		closeButton.setPosition(Global.stage.width - 75, Global.stage.height - 75)
	}

	override fun parse(xml: XmlReader.Element)
	{
	}

}