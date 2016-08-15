package com.lyeeedar.Screens

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Particle.Effect
import com.lyeeedar.Renderables.Particle.Particle
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener
import javax.swing.JFileChooser

/**
 * Created by Philip on 14-Aug-16.
 */

class ParticleEditorScreen : AbstractScreen()
{
	var currentPath: String? = null
	lateinit var particle: Effect
	val batch = SpriteBatch()

	override fun create()
	{
		val browseButton = TextButton("...", Global.skin)
		val updateButton = TextButton("Update", Global.skin)

		browseButton.addClickListener {
			val fc = JFileChooser()
			fc.currentDirectory = Gdx.files.internal("Particles").file().absoluteFile
			val returnVal = fc.showOpenDialog(null)

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				val file = fc.selectedFile

				currentPath = file.absolutePath

				particle = Effect.Companion.load(currentPath!!)
				particle.setPosition(stage.width / 64f, stage.height / 64f)
			}
		}

		updateButton.addClickListener {
			particle = Effect.Companion.load(currentPath!!)
			particle.setPosition(stage.width / 64f, stage.height / 64f)
		}

		mainTable.add(browseButton).expandY().top()
		mainTable.add(updateButton).expandY().top()

		val background = TextureRegionDrawable(AssetManager.loadTextureRegion("grass"))
	//	mainTable.background = TiledDrawable(background).tint(Color.DARK_GRAY)

		particle = Effect()
	}

	override fun doRender(delta: Float)
	{
		particle.update(delta)

		batch.begin()
		particle.render(batch, 0f, 0f, 32f)
		batch.end()
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean
	{
		particle.setPosition(screenX / 32f, (stage.height - screenY) / 32f)

		return true
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean
	{
		particle.setPosition(screenX / 32f, (stage.height - screenY) / 32f)

		return true
	}
}