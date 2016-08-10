package com.lyeeedar.Player.Ability

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Tile
import com.lyeeedar.Global
import com.lyeeedar.Screens.GridScreen
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.UI.GridWidget
import com.lyeeedar.UI.PowerBar
import com.lyeeedar.UI.Unlockable
import com.lyeeedar.Util.*

/**
 * Created by Philip on 20-Jul-16.
 */

class Ability() : Unlockable()
{
	lateinit var hitSprite: Sprite
	var flightSprite: Sprite? = null

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
			if (flightSprite != null)
			{
				val fs = flightSprite!!.copy()

				val p1 = GridScreen.instance.playerPortrait.localToStageCoordinates(Vector2())
				val p2 = GridWidget.instance.pointToScreenspace(target)

				val dist = p1.dst(p2) / Global.tileSize

				fs.spriteAnimation = MoveAnimation.obtain().set(0.05f + 0.025f * dist, arrayOf(p1, p2), Interpolation.linear)
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

			val hs = hitSprite.copy()
			hs.renderDelay = delay + 0.1f * dst
			delay += hs.lifetime * 0.6f

			target.effects.add(hs)

			effect.apply(target, grid, delay, data)
		}

		selectedTargets.clear()
	}

	override fun parse(xml: XmlReader.Element, resources: ObjectMap<String, XmlReader.Element>)
	{
		hitSprite = AssetManager.tryLoadSpriteWithResources(xml.getChildByName("HitSprite"), resources)
		flightSprite = if (xml.getChildByName("FlightSprite") != null) AssetManager.tryLoadSpriteWithResources(xml.getChildByName("FlightSprite"), resources) else null

		cost = xml.getInt("Cost")

		val effectDesc = xml.get("Effect")
		val split = effectDesc.toUpperCase().split(",")

		targets = split[0].toInt()
		targetter = Targetter(Targetter.Type.valueOf(split[1]))
		permuter = Permuter(Permuter.Type.valueOf(split[2]))
		effect = Effect(Effect.Type.valueOf(split[3]))

		val dataEl = xml.getChildByName("Data")
		if (dataEl != null)
		{
			for (i in 0..dataEl.childCount-1)
			{
				val el = dataEl.getChild(i)
				data[el.name.toUpperCase()] = el.text.toUpperCase()
			}
		}
	}
}