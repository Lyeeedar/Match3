package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 20-Jul-16.
 */

class AbilityWidget(val ability: Ability, val w: Int, val h: Int) : Table()
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
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
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
