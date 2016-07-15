package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 15-Jul-16.
 */

class FullscreenMessage(val text: String, val style: String, val function: () -> Unit) : Table()
{
	lateinit var label: Label

	var textSpeed = 0.1f
	var timeAccumulator = 0f
	var letterCount = 0

	init
	{
		instance = this

		background = TextureRegionDrawable(AssetManager.loadTextureRegion("white")).tint(Color(0f, 0f, 0f, 0.4f))

		label = Label("", Global.skin)

		add(label).center()

		addListener( object : ClickListener() {
			override fun clicked(event: InputEvent?, x: Float, y: Float)
			{
				if (letterCount == text.length)
				{
					function()
					remove()
					instance = null
				}
				else
				{
					letterCount = text.length
					label.setText(text.substring(letterCount))
				}
			}
		})
	}

	fun update(delta: Float)
	{
		if (letterCount >= text.length) return

		timeAccumulator += delta
		while (timeAccumulator >= textSpeed)
		{
			timeAccumulator -= textSpeed

			letterCount++
			if (letterCount > text.length) letterCount = text.length

			label.setText(text.substring(letterCount))
		}
	}

	companion object
	{
		var instance: FullscreenMessage? = null
	}
}