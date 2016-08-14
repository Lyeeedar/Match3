package com.lyeeedar.Particle

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Util.getXml

/**
 * Created by Philip on 14-Aug-16.
 */

class Effect
{
	private var repeat = false
	private var warmupTime = 0f
	private var doneWarmup = false

	private val emitters = Array<Emitter>()

	fun update(delta: Float): Boolean
	{
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

		for (emitter in emitters) emitter.update(delta)

		if (complete())
		{
			if (repeat)
			{
				for (emitter in emitters) emitter.time = 0f
			}

			return true
		}
		else return false
	}

	fun complete() = emitters.firstOrNull{ !it.complete() } != null

	fun setPosition(x: Float, y: Float)
	{
		for (emitter in emitters) emitter.position.set(x, y)
	}

	fun draw(batch: SpriteBatch, offsetx: Float, offsety: Float, tileSize: Float)
	{
		for (emitter in emitters) emitter.draw(batch, offsetx, offsety, tileSize)
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