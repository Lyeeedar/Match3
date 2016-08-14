package com.lyeeedar.Screens

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Bezier
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.*
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.Player.Player
import com.lyeeedar.Sprite.Sprite

import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteEffectActor
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.UI.*
import com.lyeeedar.Util.AssetManager
import java.awt.event.MouseListener

/**
 * Created by Philip on 20-Mar-16.
 */

class GridScreen(): AbstractScreen()
{
	val hp_full: Sprite = AssetManager.loadSprite("GUI/health_full")
	val hp_empty: Sprite = AssetManager.loadSprite("GUI/health_empty")
	val emptySlot = AssetManager.loadSprite("Icons/Empty")
	var hpBar = Table()
	lateinit var playerPortrait: SpriteWidget
	lateinit var player: Player
	lateinit var level: Level

	// ----------------------------------------------------------------------
	init
	{
		instance = this
	}

	// ----------------------------------------------------------------------
	override fun create()
	{
	}

	// ----------------------------------------------------------------------
	fun updateHpBar()
	{
		hpBar.clear()

		val hpPipWidth = 48f / player.maxhp
		val hpPipHeight = 48f * 0.15f

		for (i in 0..player.maxhp-1)
		{
			val sprite = if(i < player.hp) hp_full else hp_empty
			val swidget = SpriteWidget(sprite, hpPipWidth, hpPipHeight)
			hpBar.add(swidget).bottom()
		}
	}

	// ----------------------------------------------------------------------
	fun updateLevel(level: Level, player: Player)
	{
		if (!created)
		{
			baseCreate()
			created = true
		}

		this.player = player
		this.level = level

		val gridWidget = GridWidget(level.grid)

		val powerBar = PowerBar()

		val defeatWidget = level.defeat.createTable(Global.skin)
		val victoryWidget = level.victory.createTable(Global.skin)

		defeatWidget.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))
		victoryWidget.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))

		val abilityTable = Table()
		for (ability in player.abilities)
		{
			if (ability != null)
			{
				val widget = AbilityWidget(ability, 64f, 64f, level.grid)
				abilityTable.add(widget).expand()
			}
			else
			{
				abilityTable.add(SpriteWidget(emptySlot, 64f, 64f)).expand()
			}
		}

		playerPortrait = SpriteWidget(player.portrait.copy(), 48f, 48f)
		hpBar = Table()
		updateHpBar()
		level.grid.onAttacked += {

			val sprite = it.sprite.copy()

			val dst = playerPortrait.localToStageCoordinates(Vector2())
			val src = GridWidget.instance.pointToScreenspace(it)

			val vec = Vector2()
			vec.set(dst).sub(src).nor().rotate90(1).scl(64f)

			val vec2 = Vector2()
			vec2.set(src).lerp(dst, 0.5f).add(vec)

			val path = Bezier<Vector2>(src, vec2, dst)

			val diff = src.dst(dst).div(32f)

			sprite.spriteAnimation = MoveAnimation.obtain().set(0.2f + diff * 0.1f, path, Interpolation.exp5In)

			SpriteEffectActor(sprite, 32f, 32f, Vector2(),
					{
						player.hp -= 1

						updateHpBar()
					})
		}

		val playerTable = Table()
		playerTable.add(playerPortrait).expand()
		playerTable.row()
		playerTable.add(hpBar).expandX().fillX()

		mainTable.clear()
		val table = mainTable

		table.defaults().pad(10f)

		val background = TextureRegionDrawable(level.theme.floor.sprite!!.currentTexture)
		table.background = TiledDrawable(background).tint(Color.DARK_GRAY)

		table.add(abilityTable).expandX().fillX()
		table.row()
		table.add(powerBar).expandX().height(25f).fillX()
		table.row()
		table.add(gridWidget).expand().fill()
		table.row()

		val vdtable = Table()
		table.add(vdtable).expandX().fillX()

		vdtable.add(victoryWidget).width(Value.percentWidth(1f / 3f, vdtable)).expandY().fillY().left()
		vdtable.add(playerTable).width(Value.percentWidth(1f / 3f, vdtable)).center()
		vdtable.add(defeatWidget).width(Value.percentWidth(1f / 3f, vdtable)).expandY().fillY().right()

		var message = ""
		message += "\n\nVictory Condition: " + level.victory.getTextDescription()
		message += "\n\nDefeat Condition: " + level.defeat.getTextDescription()

		val fswidget = FullscreenMessage(message, "", { val i = 9 })
		fswidget.setFillParent(true)

		Global.stage.addActor(fswidget)
	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{
		level.update(delta)
	}

	// ----------------------------------------------------------------------
	companion object
	{
		lateinit var instance: GridScreen
	}
}