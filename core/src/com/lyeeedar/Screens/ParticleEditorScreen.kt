package com.lyeeedar.Screens

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Particle.Effect
import com.lyeeedar.Renderables.Particle.Particle
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Util.*
import javax.swing.JFileChooser

/**
 * Created by Philip on 14-Aug-16.
 */

class ParticleEditorScreen : AbstractScreen()
{
	var currentPath: String? = null
	lateinit var particle: Effect
	val batch = SpriteBatch()
	lateinit var background: Array2D<Symbol>
	lateinit var collision: Array2D<Boolean>
	val spriteRender = SpriteRenderer(32f, 100f, 100f, 2)
	var playbackSpeed = 1f

	override fun create()
	{
		val browseButton = TextButton("...", Global.skin)
		val updateButton = TextButton("Update", Global.skin)
		val playbackSpeedBox = SelectBox<Float>(Global.skin)
		playbackSpeedBox.setItems(0.01f, 0.05f, 0.1f, 0.25f, 0.5f, 0.75f, 1f, 1.5f, 2f, 3f, 4f, 5f)
		playbackSpeedBox.selected = 1f
		playbackSpeedBox.addListener(object : ChangeListener()
		{
			override fun changed(event: ChangeEvent?, actor: Actor?)
			{
				playbackSpeed = playbackSpeedBox.selected
			}

		})

		browseButton.addClickListener {
			val fc = JFileChooser()
			fc.currentDirectory = Gdx.files.internal("Particles").file().absoluteFile
			val returnVal = fc.showOpenDialog(null)

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				val file = fc.selectedFile

				currentPath = file.absolutePath

				val nparticle = Effect.Companion.load(currentPath!!)
				nparticle.setPosition(particle.getPosition().x, particle.getPosition().y)
				particle = nparticle
			}
		}

		updateButton.addClickListener {

			val nparticle = Effect.Companion.load(currentPath!!)
			nparticle.setPosition(particle.getPosition().x, particle.getPosition().y)
			particle = nparticle
		}

		mainTable.add(browseButton).expandY().top()
		mainTable.add(updateButton).expandY().top()
		mainTable.add(playbackSpeedBox).expandY().top()

		particle = Effect()

		loadLevel()
	}

	fun loadLevel()
	{
		val xml = getXml("Particles/ParticleTestLevel")

		val symbolsEl = xml.getChildByName("Symbols")
		val symbolMap = ObjectMap<Char, Symbol>()

		for (i in 0..symbolsEl.childCount-1)
		{
			val el = symbolsEl.getChild(i)
			val symbol = Symbol.load(el)
			symbolMap[symbol.char] = symbol
		}

		val rowsEl = xml.getChildByName("Rows")
		val width = rowsEl.getChild(0).text.length
		val height = rowsEl.childCount

		background = Array2D(width, height) { x, y -> symbolMap[rowsEl.getChild(height - y - 1).text[x]].copy() }
		collision = Array2D(width, height) { x, y -> background[x, y].isWall }
	}

	override fun doRender(delta: Float)
	{
		particle.collisionGrid = collision
		//particle.update(delta * playbackSpeed)

		for (x in 0..background.xSize-1)
		{
			for (y in 0..background.ySize-1)
			{
				val symbol = background[x, y]
				var i = 0
				for (renderable in symbol.sprites)
				{
					if (renderable is Sprite)
					{
						spriteRender.queueSprite(renderable, x.toFloat(), y.toFloat(), 0, i++)
					}
					else if (renderable is TilingSprite)
					{
						spriteRender.queueSprite(renderable, x.toFloat(), y.toFloat(), 0, i++)
					}
				}
			}
		}
		spriteRender.queueParticle(particle, 0f, 0f, 1, 0)

		batch.color = Color.WHITE
		batch.begin()
		spriteRender.flush(delta * playbackSpeed, 0f, 0f, batch)
		//particle.render(batch, 0f, 0f, 32f)
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

class Symbol
{
	var char: Char = ' '
	val sprites: Array<Renderable> = Array()
	var isWall: Boolean = false

	fun copy(): Symbol
	{
		val symbol = Symbol()
		symbol.char = char
		for (sprite in sprites)
		{
			symbol.sprites.add(sprite.copy())
		}
		symbol.isWall = isWall

		return symbol
	}

	companion object
	{
		fun load(xml: XmlReader.Element) : Symbol
		{
			val symbol = Symbol()
			symbol.isWall = xml.getBooleanAttribute("IsWall", false)

			for (i in 0..xml.childCount-1)
			{
				val el = xml.getChild(i)
				if (el.name == "Char") symbol.char = el.text[0]
				else
				{
					if (el.name == "Sprite")
					{
						symbol.sprites.add(AssetManager.loadSprite(el))
					}
					else if (el.name == "TilingSprite")
					{
						symbol.sprites.add(AssetManager.loadTilingSprite(el))
					}
					else
					{
						throw RuntimeException("Invalid symbol data type '${el.name}'!")
					}
				}
			}

			return symbol
		}
	}
}