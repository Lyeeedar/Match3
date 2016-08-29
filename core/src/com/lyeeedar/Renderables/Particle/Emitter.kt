package com.lyeeedar.Renderables.Particle

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Util.vectorToAngle

internal class Emitter
{
	val MAX_DELTA = 1f / 15f // dont update faster than 15fps

	enum class SimulationSpace
	{
		LOCAL,
		WORLD
	}

	enum class EmissionShape
	{
		CIRCLE,
		BOX
	}

	enum class EmissionArea
	{
		INTERIOR,
		BORDER
	}

	enum class EmissionDirection
	{
		RADIAL,
		RANDOM,
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	private val temp = Vector2()

	val particles: Array<Particle> = Array(false, 16)

	val position = Vector2()

	lateinit var simulationSpace: SimulationSpace
	val emissionRate = LerpTimeline()
	var particleSpeed: Float = 0f
	var particleRotation: Float = 0f
	lateinit var shape: EmissionShape
	var width: Float = 0f
	var height: Float = 0f
	lateinit var area: EmissionArea
	lateinit var dir: EmissionDirection

	var time: Float = 0f
	var emissionAccumulator: Float = 0f

	var stopped = false

	fun complete() = particles.firstOrNull{ !it.complete() } != null
	fun stop() { stopped = true }
	fun start() { stopped = false }

	fun update(delta: Float)
	{
		time += delta

		val scaledDelta = Math.min(delta, MAX_DELTA)

		if (!stopped)
		{
			val duration = emissionRate.length()
			val rate = emissionRate.valAt(0, time)

			// Keep particle count to emission rate
			if (duration == 0f)
			{
				val toSpawn = Math.max(0f, rate - particles.sumBy { it.particleCount() }).toInt()
				for (i in 1..toSpawn)
				{
					spawn()
				}
			}
			// use accumulator to spawn constantly
			else if (time < duration)
			{
				emissionAccumulator += scaledDelta * rate

				while (emissionAccumulator > 1f)
				{
					emissionAccumulator -= 1f
					spawn()
				}
			}
		}

		for (particle in particles)
		{
			particle.simulate(scaledDelta)
		}
	}

	fun spawn()
	{
		val pos = when (shape)
		{
			EmissionShape.CIRCLE -> spawnCircle()
			EmissionShape.BOX -> spawnBox()
			else -> throw RuntimeException("Invalid emitter shape! $shape")
		}

		val velocity: Vector2 = when (dir)
		{
			EmissionDirection.RADIAL -> Vector2(pos).nor()
			EmissionDirection.RANDOM -> Vector2().setToRandomDirection()
			EmissionDirection.UP -> Vector2(Direction.NORTH.x.toFloat(), Direction.NORTH.y.toFloat())
			EmissionDirection.DOWN -> Vector2(Direction.SOUTH.x.toFloat(), Direction.SOUTH.y.toFloat())
			EmissionDirection.LEFT -> Vector2(Direction.WEST.x.toFloat(), Direction.WEST.y.toFloat())
			EmissionDirection.RIGHT -> Vector2(Direction.EAST.x.toFloat(), Direction.EAST.y.toFloat())
			else -> throw RuntimeException("Invalid emitter direction type! $dir")
		}

		val speed = particleSpeed
		val rotation = particleRotation

		if (simulationSpace == SimulationSpace.WORLD)
		{
			pos.add(position)
		}

		// pick random particle
		val particle = particles.random()
		particle.spawn(pos, velocity, speed, rotation)
	}

	fun spawnCircle(): Vector2
	{
		if (area == EmissionArea.INTERIOR)
		{
			val ranVal = MathUtils.random()
			val sqrtRanVal = Math.sqrt(ranVal.toDouble()).toFloat()
			val phi = MathUtils.random() * (2f * Math.PI)
			val x = sqrtRanVal * Math.cos(phi).toFloat() * (width / 2f)
			val y = sqrtRanVal * Math.sin(phi).toFloat() * (height / 2f)

			temp.set(x, y)
		}
		else if (area == EmissionArea.BORDER)
		{
			val phi = MathUtils.random() * (2f * Math.PI)
			val x = Math.cos(phi).toFloat() * (width / 2f)
			val y = Math.sin(phi).toFloat() * (height / 2f)

			temp.set(x, y)
		}
		else throw RuntimeException("Invalid emitter area type $area")

		return temp
	}

	fun spawnBox(): Vector2
	{
		if (area == EmissionArea.BORDER)
		{
			val w2 = width/2f
			val h2 = height/2f
			val p1 = Vector2(-w2, h2) // top left
			val p2 = Vector2(w2, h2) // top right
			val p3 = Vector2(w2, -h2) // bottom right
			val p4 = Vector2(-w2, -h2) // bottom left
			val points = arrayOf(p1, p2, p3, p4)
			val dists = floatArrayOf(width, height, width, height)
			for (i in 1..dists.size-1) dists[i] += dists[i-1]

			val totalDist = dists.last()
			val chosenDst = MathUtils.random() * totalDist

			var i = 0
			while (i < dists.size)
			{
				if (dists[i] > chosenDst)
				{
					break
				}

				i++
			}
			if (i == points.size) i = points.size-1

			val delta = dists[i] - chosenDst
			val start = points[i]
			val end = if (i+1 == points.size) points[0] else points[i+1]
			val diff = start.dst(end)

			temp.set(start).lerp(end, delta / diff)
		}
		else if (area == EmissionArea.INTERIOR)
		{
			val x = MathUtils.random() * width - (width / 2f)
			val y = MathUtils.random() * height - (height / 2f)

			temp.set(x, y)
		}
		else throw RuntimeException("Invalid emitter area type $area")

		return temp
	}

	fun draw(batch: SpriteBatch, offsetx: Float, offsety: Float, tileSize: Float, colour: Color)
	{
		for (particle in particles)
		{
			val offsetx = offsetx + if (simulationSpace == SimulationSpace.LOCAL) position.x * tileSize else 0f
			val offsety = offsety + if (simulationSpace == SimulationSpace.LOCAL) position.y * tileSize else 0f

			particle.render(batch, offsetx, offsety, tileSize, colour)
		}
	}

	companion object
	{
		fun load(xml: XmlReader.Element): Emitter
		{
			val emitter = Emitter()

			emitter.simulationSpace = SimulationSpace.valueOf(xml.get("Space", "World").toUpperCase())
			emitter.particleSpeed = xml.getFloat("ParticleSpeed", 0f)
			emitter.particleRotation = xml.getFloat("ParticleRotation", 0f)
			emitter.shape = EmissionShape.valueOf(xml.get("Shape", "Box").toUpperCase())
			emitter.width = xml.get("Size", null)?.toFloat() ?: xml.getFloat("Width", 1f)
			emitter.height = xml.get("Size", null)?.toFloat() ?: xml.getFloat("Height", 1f)
			emitter.area = EmissionArea.valueOf(xml.get("Area", "Interior").toUpperCase())
			emitter.dir = EmissionDirection.valueOf(xml.get("Direction", "Radial").toUpperCase())

			val rateEls = xml.getChildByName("RateKeyframes")
			emitter.emissionRate.parse(rateEls, { it.toFloat() })

			val particlesEl = xml.getChildByName("Particles")
			for (i in 0..particlesEl.childCount-1)
			{
				val el = particlesEl.getChild(i)
				val particle = Particle.load(el)
				emitter.particles.add(particle)
			}

			return emitter
		}
	}
}