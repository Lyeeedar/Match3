package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.lyeeedar.Board.Grid
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 20-Jul-16.
 */

class AbilityWidget(val ability: Ability, val w: Int, val h: Int, val grid: Grid) : Table()
{
	val white = AssetManager.loadSprite("white", colour = Color.CYAN)
	val padding = 3

	val widget = SpriteWidget(ability.icon, w, h)

	init
	{
		add(widget).expand().fill()

		PowerBar.instance.powerChanged += {
			if (PowerBar.instance.pips >= ability.cost)
			{
				widget.color = Color.WHITE
			}
			else
			{
				widget.color = Color.DARK_GRAY
			}
		}
		PowerBar.instance.powerChanged()

		addListener(object: ClickListener()
		{
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				if (PowerBar.instance.pips >= ability.cost)
				{
					if (grid.activeAbility == ability)
					{
						grid.activeAbility = null
					}
					else
					{
						grid.queuedAbility = ability
					}
				}
			}
		})

		touchable = Touchable.enabled
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		if (grid.activeAbility == ability)
		{
			widget.color = Color.GOLD
		}
		else
		{
			if (PowerBar.instance.pips >= ability.cost)
			{
				widget.color = Color.WHITE
			}
			else
			{
				widget.color = Color.DARK_GRAY
			}
		}

		super.draw(batch, parentAlpha)

		val pipSize = (width - (ability.cost+1) * padding) / ability.cost
		val filledPips = PowerBar.instance.pips

		batch!!.color = Color.WHITE

		for (i in 1..ability.cost)
		{
			white.colour = if (i <= filledPips) Color.CYAN else Color.LIGHT_GRAY

			white.render(batch as SpriteBatch, x + padding * i + (i-1) * pipSize, y, pipSize, 10f)
		}
	}
}
