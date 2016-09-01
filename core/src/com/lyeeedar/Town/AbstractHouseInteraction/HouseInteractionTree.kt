package com.lyeeedar.Town.AbstractHouseInteraction

import com.badlogic.gdx.scenes.scene2d.ui.Button
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Town.House
import com.lyeeedar.UI.UnlockTreeWidget
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 11-Aug-16.
 */

class HouseInteractionTree : AbstractHouseInteraction()
{
	lateinit var tree: String

	override fun apply(house: House, playerData: PlayerData)
	{
		if (tree == "Equipment")
		{
			val widget = Table()
			val closeButton = Button(Global.skin, "close")
			closeButton.setSize(24f, 24f)

			val skills = UnlockTreeWidget(playerData.equipTree, playerData)
			val scroll = ScrollPane(skills)
			scroll.setFlingTime(0f)
			scroll.setOverscroll(false, false)
			widget.add(scroll).expand().fill()

			widget.setFillParent(true)
			Global.stage.addActor(widget)

			scroll.layout()
			scroll.scrollTo(skills.prefWidth/3, 0f, 1f, 1f, true, true)
			scroll.act(1f)

			closeButton.addClickListener({ widget.remove(); closeButton.remove(); house.advance(playerData) })
			Global.stage.addActor(closeButton)
			closeButton.setPosition(Global.stage.width - 50, Global.stage.height - 50)
		}
		else
		{
			val unlockTree = playerData.getSkillTree("UnlockTrees/$tree")

			val widget = Table()
			val closeButton = Button(Global.skin, "close")
			closeButton.setSize(24f, 24f)

			val skills = UnlockTreeWidget(unlockTree, playerData)
			val scroll = ScrollPane(skills)
			scroll.setFlingTime(0f)
			scroll.setOverscroll(false, false)
			widget.add(scroll).expand().fill()

			widget.setFillParent(true)
			Global.stage.addActor(widget)

			scroll.layout()
			scroll.scrollTo(skills.prefWidth/3, 0f, 1f, 1f, true, true)
			scroll.act(1f)

			closeButton.addClickListener({ widget.remove(); closeButton.remove(); house.advance(playerData) })
			Global.stage.addActor(closeButton)
			closeButton.setPosition(Global.stage.width - 50, Global.stage.height - 50)
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		tree = xml.get("Tree")
	}

}