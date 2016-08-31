package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Renderables.Renderable
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.getXml

/**
 * Created by Philip on 14-Aug-16.
 */

class Effect : Renderable()
{
	private var repeat = false
	private var warmupTime = 0f
	private var doneWarmup = false
	val emitters = Array<Emitter>()
	private val position = Vector2()

	var collisionGrid: Array2D<Boolean>? = null

	override fun doUpdate(delta: Float): Boolean
	{
		var complete = animation?.update(delta) ?: false
		if (complete)
		{
			for (emitter in emitters) emitter.stop()
			animation?.free()
			animation = null
		}

		val posOffset = animation?.renderOffset()
		val x = position.x + (posOffset?.get(0) ?: 0f)
		val y = position.y + (posOffset?.get(1) ?: 0f)

		for (emitter in emitters) emitter.position.set(x, y)

		if (warmupTime > 0f && !doneWarmup)
		{
			doneWarmup = true
			val deltaStep = 1f / 15f // simulate at 15 fps
			val steps = (warmupTime / deltaStep).toInt()
			for (i in 0..steps-1)
			{
				update(deltaStep)
			}
		}

		for (emitter in emitters) emitter.update(delta, collisionGrid)

		if (complete())
		{
			for (emitter in emitters) emitter.time = 0f
			complete = true
		}
		else
		{
			complete = false
		}

		return complete
	}

	fun complete() = emitters.firstOrNull{ !it.complete() } == null

	fun getPosition() = position

	fun setPosition(x: Float, y: Float)
	{
		position.set(x, y)
	}

	override fun doRender(batch: SpriteBatch, x: Float, y: Float, tileSize: Float)
	{
		val scale = animation?.renderScale()?.get(0) ?: 1f
		val colour = animation?.renderColour() ?: Color.WHITE

		for (emitter in emitters) emitter.draw(batch, x, y, tileSize * scale, colour)

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
	}

	override fun copy(): Effect
	{
		throw NotImplementedError("I aint done this yet")
	}

	companion object
	{
		fun load(xml: XmlReader.Element): Effect
		{
			val effect = Effect()

			effect.repeat = xml.getBoolean("Repeat", false)
			effect.warmupTime = xml.getFloat("Warmup", 0f)

			val emittersEl = xml.getChildByName("Emitters")
			for (i in 0..emittersEl.childCount-1)
			{
				val el = emittersEl.getChild(i)
				val emitter = Emitter.load(el)
				effect.emitters.add(emitter)
			}

			return effect
		}

		fun load(path: String): Effect
		{
			val xml = getXml(path)
			return load(xml)
		}
	}
}