package com.lyeeedar.Board.CompletionAction

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Mote
import com.lyeeedar.Player.Player
import com.lyeeedar.UI.DungeonMapWidget
import com.lyeeedar.UI.PlayerWidget
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 29-Jul-16.
 */

class CompletionActionMoney() : AbstractCompletionAction()
{
	var change: Int = 0

	override fun apply(player: Player)
	{
		if (change > 0)
		{
			for (i in 1..change)
			{
				val sprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/coin_gold", drawActualSize = true)
				val dst = PlayerWidget.instance.localToStageCoordinates(Vector2())
				val src = DungeonMapWidget.instance.getCenterInScreenspace()

				Mote(src, dst, sprite, { player.gold++ })
			}
		}
		else
		{
			player.gold += change
		}
	}

	override fun parse(xml: XmlReader.Element)
	{
		change = xml.text.toInt()
	}
}