package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Value
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.lyeeedar.Global
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 15-Jul-16.
 */

class FullscreenMessage(val text: String, val style: String, val function: () -> Unit) : Table()
{
	lateinit var label: Label

	var textSpeed = 0.01f
	var timeAccumulator = 0f
	var letterCount = 0

	init
	{
		instance = this

		background = TextureRegionDrawable(AssetManager.loadTextureRegion("white")).tint(Color(0f, 0f, 0f, 0.4f))

		label = Label("", Global.skin)
		label.setWrap(true)

		val labelTable = Table()
		labelTable.background = NinePatchDrawable( NinePatch(AssetManager.loadTextureRegion("GUI/PanelHorizontal"), 24, 24, 24, 24) )
		labelTable.add(label).expand().fill()

		add(labelTable).width(Value.percentWidth(0.7f, this)).height(Value.percentHeight(0.7f, this))
		touchable = Touchable.enabled

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
					label.setText(text.substring(0, letterCount))
				}
			}
		})
	}

	fun show()
	{
		setFillParent(true)
		Global.stage.addActor(this)
	}

	override fun act(delta: Float)
	{
		super.act(delta)

		if (letterCount >= text.length) return

		timeAccumulator += delta
		while (timeAccumulator >= textSpeed)
		{
			timeAccumulator -= textSpeed

			letterCount++
			if (letterCount > text.length) letterCount = text.length

			label.setText(text.substring(0, letterCount))
		}
	}

	companion object
	{
		var instance: FullscreenMessage? = null
	}
}