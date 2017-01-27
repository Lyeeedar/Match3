package com.lyeeedar.Screens

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Bezier
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.*
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.Player.Player
import com.lyeeedar.Renderables.Animation.ExpandAnimation
import com.lyeeedar.Renderables.Animation.LeapAnimation
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.SpriteEffectActor
import ktx.scene2d.*
import ktx.actors.*

import com.lyeeedar.UI.*
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 20-Mar-16.
 */

class GridScreen(): AbstractScreen()
{
	val hp_full: Sprite = AssetManager.loadSprite("GUI/health_full")
	val hp_empty: Sprite = AssetManager.loadSprite("GUI/health_empty")
	val emptySlot = AssetManager.loadSprite("Icons/Empty")
	var hpBar = Table()
	var launchButton: TextButton? = null
	var grid: GridWidget? = null
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
		grid = gridWidget

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

			val diff = src.dst(dst).div(32f)

			val animDuration = 0.25f + diff * 0.025f
			sprite.animation = LeapAnimation.obtain().set(animDuration, src, dst, (1f + diff * 0.25f) * 32f)
			sprite.animation = ExpandAnimation.obtain().set(animDuration, 0.5f, 1.5f, false)

			SpriteEffectActor(sprite, 32f, 32f, Vector2(),
					{
						player.hp -= 1

						updateHpBar()

						SpriteEffectActor(AssetManager.loadSprite("EffectSprites/Hit/Hit", 0.1f), 32f, 32f, dst)
					})
		}

		mainTable.clear()
		//val table = mainTable

		val table = table {
			defaults().pad(10f).growX()
			background = TiledDrawable(TextureRegionDrawable(level.theme.floor.sprite!!.currentTexture)).tint(Color.DARK_GRAY)

			add(abilityTable)
			row()
			stack { cell -> cell.height(25f)
				add(powerBar)
				launchButton = textButton("Launch", "default", Global.skin) {
					isVisible = false
					onClick { inputEvent: InputEvent, kTextButton: KTextButton ->
						if (level.grid.activeAbility != null && level.grid.activeAbility!!.selectedTargets.size > 0)
						{
							level.grid.activeAbility!!.activate(level.grid)
							level.grid.activeAbility = null
						}
					}
				}
			}
			row()
			add(gridWidget).grow()
			row()
			table {
				add(victoryWidget).grow().left()

				table { cell -> cell.growX().center()
					add(playerPortrait).expand()
					row()
					add(hpBar).growX()
				}

				add(defeatWidget).grow().right()
			}
		}

		mainTable.add(table).grow()

		var message = ""
		message += "\n\nVictory Condition: " + level.victory.getTextDescription()
		message += "\n\nDefeat Condition: " + level.defeat.getTextDescription()

		val fswidget = FullscreenMessage(message, "", { val i = 9 })
		fswidget.setFillParent(true)

		Global.stage.addActor(fswidget)
	}

	override fun keyDown(keycode: Int): Boolean
	{
		if (keycode == Input.Keys.D)
		{
			grid!!.ground.debugDraw = !grid!!.ground.debugDraw
		}
		else if (keycode == Input.Keys.UP)
		{
			if (grid!!.ground.debugDraw)
			{
				grid!!.ground.debugDrawSpeed++
			}
		}
		else if (keycode == Input.Keys.DOWN)
		{
			if (grid!!.ground.debugDraw)
			{
				grid!!.ground.debugDrawSpeed--
			}
		}

		return false
	}

	// ----------------------------------------------------------------------
	override fun doRender(delta: Float)
	{
		level.update(delta)

		val ability = level.grid.activeAbility

		if (ability != null)
		{
			if (ability.selectedTargets.size != lastTargets)
			{
				idleTimer = 0f
				lastTargets = ability.selectedTargets.size
			}
			else if (idleTimer <= 2f && popup == null)
			{
				idleTimer += delta
			}
			else if (idleTimer > 2f && popup == null && !squashed)
			{
				popup = TutorialPopup("Click activate to use your ability on the selected tiles.", launchButton!!.localToStageCoordinates(Vector2()), "AbilityButton")
			}

			PowerBar.instance.isVisible = false
			launchButton!!.isVisible = true
			launchButton!!.color = if (ability.selectedTargets.size == 0) Color.DARK_GRAY else Color.WHITE
			launchButton!!.touchable = if (ability.selectedTargets.size == 0) Touchable.disabled else Touchable.enabled
			launchButton!!.setText("Activate (" + ability.selectedTargets.size + "/" + ability.targets + ")")
		}
		else
		{
			PowerBar.instance.isVisible = true
			launchButton!!.isVisible = false
			idleTimer = 0f
			lastTargets = 0
		}
	}
	var squashed = false
	var idleTimer = 0f
	var lastTargets = 0
	var popup: TutorialPopup? = null

	// ----------------------------------------------------------------------
	companion object
	{
		lateinit var instance: GridScreen
	}
}