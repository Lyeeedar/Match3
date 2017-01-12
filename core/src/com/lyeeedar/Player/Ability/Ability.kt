package com.lyeeedar.Player.Ability

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Tile
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Screens.GridScreen
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.UI.PowerBar
import com.lyeeedar.UI.Unlockable
import com.lyeeedar.Util.*

/**
 * Created by Philip on 20-Jul-16.
 */

class Ability() : Unlockable()
{
	var hitEffect: ParticleEffect? = null
	var flightEffect: ParticleEffect? = null

	var cost: Int = 2

	var targets = 1
	var targetter: Targetter = Targetter(Targetter.Type.ORB)
	var permuter: Permuter = Permuter(Permuter.Type.SINGLE)
	var effect: Effect = Effect(Effect.Type.TEST)
	val data = ObjectMap<String, String>()

	val selectedTargets = Array<Tile>()

	fun activate(grid: Grid)
	{
		PowerBar.instance.pips -= cost

		val finalTargets = Array<Tile>()

		if (permuter.type == Permuter.Type.RANDOM)
		{
			for (t in permuter.permute(grid.tile(grid.width/2, grid.height/2)!!, grid, data))
			{
				if (!selectedTargets.contains(t, true))
				{
					selectedTargets.add(t)
				}
			}
		}

		val selectedDelays = ObjectMap<Tile, Float>()

		for (target in selectedTargets)
		{
			if (permuter.type == Permuter.Type.RANDOM)
			{
				finalTargets.add(target)
			}
			else
			{
				for (t in permuter.permute(target, grid, data))
				{
					if (!finalTargets.contains(t, true))
					{
						finalTargets.add(t)
					}
				}
			}

			var delay = 0f
			if (flightEffect != null)
			{
				val fs = flightEffect!!.copy()

				val p1 = GridScreen.instance.playerPortrait.localToStageCoordinates(Vector2())
				val p2 = GridWidget.instance.pointToScreenspace(target)

				p1.scl(1f / 32f)
				p2.scl(1f / 32f)

				val dist = p1.dst(p2)

				fs.animation = MoveAnimation.obtain().set(dist * fs.moveSpeed, arrayOf(p1, p2), Interpolation.linear)
				fs.rotation = getRotation(p1, p2)
				delay += fs.lifetime

				target.effects.add(fs)
			}

			selectedDelays[target] = delay
		}

		for (target in finalTargets)
		{
			val closest = selectedTargets.minBy { it.dist(target) }!!
			var delay = selectedDelays[closest]
			val dst = closest.dist(target)

			if (hitEffect != null)
			{
				val hs = hitEffect!!.copy()
				hs.renderDelay = delay + 0.1f * dst
				delay += hs.lifetime * 0.6f

				target.effects.add(hs)
			}

			effect.apply(target, grid, delay, data)
		}

		selectedTargets.clear()
	}

	override fun parse(xml: XmlReader.Element, resources: ObjectMap<String, XmlReader.Element>)
	{
		val dataEl = xml.getChildByName("EffectData")

		val hitEffectData = dataEl.getChildByName("HitEffect")
		if (hitEffectData != null) hitEffect = AssetManager.loadParticleEffect(hitEffectData)
		val flightEffectData = dataEl.getChildByName("FlightEffect")
		if (flightEffectData != null) flightEffect = AssetManager.loadParticleEffect(flightEffectData)

		cost = dataEl.getInt("Cost", 0)

		val effectDesc = dataEl.get("Effect")
		val split = effectDesc.toUpperCase().split(",")

		targets = split[0].toInt()
		targetter = Targetter(Targetter.Type.valueOf(split[1]))
		permuter = Permuter(Permuter.Type.valueOf(split[2]))
		effect = Effect(Effect.Type.valueOf(split[3]))

		val dEl = dataEl.getChildByName("Data")
		if (dEl != null)
		{
			for (i in 0..dEl.childCount-1)
			{
				val el = dEl.getChild(i)
				val text = el.text.toUpperCase()
				val split = text.split(",")

				data[split[0]] = split[1]
			}
		}
	}
}