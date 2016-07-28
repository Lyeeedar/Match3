package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Mote
import com.lyeeedar.Player.Player
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager

class CompletionConditionDeath() : AbstractCompletionCondition()
{
	lateinit var label: Label
	lateinit var player: Player

	override fun attachHandlers(grid: Grid)
	{
		player = grid.level.player

		grid.onAttacked += {

			val sprite = AssetManager.loadSprite("Oryx/uf_split/uf_items/crystal_blood")
			val dst = label.localToStageCoordinates(Vector2())
			val src = GridWidget.instance.pointToScreenspace(it)

			val mote = Mote(src, dst, sprite, grid, {
				player.hp -= 1

				val hp = player.hp
				val max = player.maxhp

				label.setText("$hp/$max")
			})
			grid.motes.add(mote)
		}
	}

	override fun isCompleted(): Boolean = player.hp <= 0

	override fun parse(xml: XmlReader.Element)
	{

	}

	override fun createTable(skin: Skin): Table
	{
		val hp = player.hp
		val max = player.maxhp

		label = Label("$hp/$max", skin)

		val table = Table()
		table.defaults().pad(10f)
		table.add(label)

		table.add(SpriteWidget(player.portrait, 24, 24))

		table.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("GUI/TilePanel"), 6, 6, 6, 6))

		return table
	}

	override fun getTextDescription(): String = "Your hp reaches 0"
}