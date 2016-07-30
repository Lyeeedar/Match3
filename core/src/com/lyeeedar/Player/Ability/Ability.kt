package com.lyeeedar.Player.Ability

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Array
import com.lyeeedar.Board.Grid
import com.lyeeedar.Board.Tile
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.UI.PowerBar
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.UnsmoothedPath

/**
 * Created by Philip on 20-Jul-16.
 */

class Ability()
{
	constructor(icon: Sprite, cost: Int, elite: Boolean) : this()
	{
		this.icon = icon
		this.cost = cost
		this.elite = elite
	}

	lateinit var icon: Sprite
	var cost: Int = 2
	var elite: Boolean = false

	val selectedTargets = Array<Tile>()

	var targets = 1
	var targetter: Targetter = Targetter(Targetter.Type.ORB)
	var permuter: Permuter = Permuter(Permuter.Type.SINGLE)
	var effect: Effect = Effect(Effect.Type.TEST)

	var flightSprite: Sprite? = null
	var hitSprite: Sprite = AssetManager.loadSprite("EffectSprites/Explosion/Explosion", updateTime = 0.1f)

	fun activate(grid: Grid)
	{
		PowerBar.instance.pips -= cost

		val finalTargets = Array<Tile>()

		for (target in selectedTargets)
		{
			for (t in permuter.permute(target, grid))
			{
				if (!finalTargets.contains(t, true))
				{
					finalTargets.add(t)
				}
			}
		}

		selectedTargets.clear()

		for (target in finalTargets)
		{
			var delay = 0f
			if (flightSprite != null)
			{
				val fs = flightSprite!!.copy()

				fs.spriteAnimation = MoveAnimation.obtain().set(0.5f, UnsmoothedPath(target.getPosDiff(Point.MINUS_ONE)), Interpolation.linear)
				delay += fs.lifetime

				target.effects.add(fs)
			}

			val hs = hitSprite.copy()
			hs.renderDelay = delay
			delay += hs.lifetime * 0.6f

			target.effects.add(hs)

			effect.apply(target, grid, delay)
		}
	}
}