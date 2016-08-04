package com.lyeeedar.Board.CompletionCondition

import com.badlogic.gdx.graphics.Color
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
import com.lyeeedar.Global
import com.lyeeedar.Player.Player
import com.lyeeedar.Sprite.SpriteAnimation.ExtendAnimation
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getRotation

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
			dst.y = Global.stage.height - dst.y
			val src = GridWidget.instance.pointToScreenspace(it)

			val newPos = Vector2(dst)
			val diff = newPos.sub(src)
			diff.x *= -1

			val path = arrayOf(diff, Vector2())

			val beam = AssetManager.loadSprite("EffectSprites/Beam/Beam")
			beam.rotation = getRotation(src, dst) * -1
			beam.spriteAnimation = ExtendAnimation.obtain().set(0.25f, path)
			beam.colour = Color.RED
			grid.tile(it)?.effects?.add(beam)

			player.hp -= 1

			val hp = player.hp
			val max = player.maxhp

			label.setText("$hp/$max")
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