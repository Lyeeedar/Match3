package com.lyeeedar.Board

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ObjectSet
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Animation.ChromaticAnimation
import com.lyeeedar.Renderables.Animation.MoveAnimation
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.*

/**
 * Created by Philip on 30-Jul-16.
 */

abstract class Special(val orb: Orb)
{
	lateinit var sprite: Sprite

	abstract fun merge(other: Orb): ((point: Point, grid: Grid, orb: Orb) -> Unit)?
	abstract fun apply(): (point: Point, grid: Grid, orb: Orb) -> Unit
	abstract fun copy(orb: Orb): Special

	companion object
	{
		fun popTile(special: Special, tile: Tile, point: Point, grid: Grid, offset: Float = 0f)
		{
			val delay = tile.dist(point) * 0.1f + offset
			grid.pop(tile, delay + 0.2f, special, 1 + 2)
		}

		fun popColumn(special: Special, colour: Colour, x: Int, sy: Int, grid: Grid)
		{
			if (x < 0 || x >= grid.width) return

			fun launchAt(x: Int, y: Int)
			{
				val p2 = Vector2(x.toFloat(), sy.toFloat())
				val p1 = Vector2(x.toFloat(), y.toFloat())

				val dist = p1.dst(p2)

				val hitSet = ObjectSet<Tile>()

				val effect = AssetManager.loadParticleEffect("SpecialBeam")
				effect.colour = colour
				effect.animation = MoveAnimation.obtain().set(dist * effect.moveSpeed, arrayOf(p1, p2), Interpolation.linear)
				effect.rotation = getRotation(p1, p2)
				effect.collisionFun = fun(cx: Int, pcy: Int)
				{
					val cy = (grid.height-1) - pcy
					val tile = grid.tile(cx, cy)
					if (tile != null && cx == x && !hitSet.contains(tile))
					{
						hitSet.add(tile)
						grid.pop(cx, cy, 0f, special, 1+2)
					}
				}
				grid.grid[x, y].effects.add(effect)
			}

			var launchedUp = false
			for (y in sy+1..grid.height-1)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb && !tile.isPit)
				{
					launchedUp = true
					launchAt(x, y)

					break
				}
			}
			if (!launchedUp)
			{
				launchAt(x, grid.height-1)
			}

			var launchedDown = false
			for (y in sy-1 downTo 0)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb && !tile.isPit)
				{
					launchedDown = true
					launchAt(x, y)

					break
				}

			}
			if (!launchedDown)
			{
				launchAt(x, 0)
			}
		}

		fun popRow(special: Special, colour: Colour, sx: Int, y: Int, grid: Grid)
		{
			if (y < 0 || y >= grid.height) return

			fun launchAt(x: Int, y: Int)
			{
				val p1 = Vector2(sx.toFloat(), y.toFloat())
				val p2 = Vector2(x.toFloat(), y.toFloat())

				val dist = p1.dst(p2)

				val hitSet = ObjectSet<Tile>()

				val effect = AssetManager.loadParticleEffect("SpecialBeam")
				effect.colour = colour
				effect.animation = MoveAnimation.obtain().set(dist * effect.moveSpeed, arrayOf(p1, p2), Interpolation.linear)
				effect.rotation = getRotation(p1, p2)
				effect.collisionFun = fun(cx: Int, pcy: Int)
				{
					val cy = (grid.height-1) - pcy
					val tile = grid.tile(cx, cy)
					if (tile != null && cy == y && !hitSet.contains(tile))
					{
						hitSet.add(tile)
						grid.pop(cx, cy, 0f, special, 1 + 2)
					}

				}
				grid.grid[x, y].effects.add(effect)
			}

			var launchedRight = false
			for (x in sx+1..grid.width-1)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb && !tile.isPit)
				{
					launchedRight = true
					launchAt(x, y)

					break
				}
			}
			if (!launchedRight)
			{
				launchAt(grid.width-1, y)
			}

			var launchedLeft = false
			for (x in sx-1 downTo 0)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb && !tile.isPit)
				{
					launchedLeft = true
					launchAt(x, y)

					break
				}
			}
			if (!launchedLeft)
			{
				launchAt(0, y)
			}
		}
	}
}

abstract class Match4(orb: Orb) : Special(orb)
{
	override fun merge(other: Orb): ((point: Point, grid: Grid, orb: Orb) -> Unit)?
	{
		if (other.special != null && other.special is Match4)
		{
			return fun (point: Point, grid: Grid, orb: Orb)
			{
				popColumn(this, orb.sprite.colour * other.sprite.colour, point.x, point.y, grid)
				popRow(this, orb.sprite.colour * other.sprite.colour, point.x, point.y, grid)
			}
		}

		return null
	}
}

class Horizontal4(orb: Orb) : Match4(orb)
{
	init
	{
		sprite = AssetManager.loadSprite("Oryx/Custom/items/orb_vert", drawActualSize = true)
	}

	override fun copy(orb: Orb): Special = Horizontal4(orb)
	override fun apply() = fun (point: Point, grid: Grid, orb: Orb) {	popColumn(this, orb.sprite.colour, point.x, point.y, grid) }

}

class Vertical4(orb: Orb) : Match4(orb)
{
	init
	{
		sprite = AssetManager.loadSprite("Oryx/Custom/items/orb_hori", drawActualSize = true)
	}

	override fun copy(orb: Orb): Special = Vertical4(orb)
	override fun apply() = fun (point: Point, grid: Grid, orb: Orb) {	popRow(this, orb.sprite.colour, point.x, point.y, grid) }
}

class DualMatch(orb: Orb) : Special(orb)
{
	init
	{
		sprite = AssetManager.loadSprite("Oryx/Custom/items/orb_dual", drawActualSize = true)
	}

	override fun copy(orb: Orb): Special = DualMatch(orb)

	override fun merge(other: Orb): ((point: Point, grid: Grid, orb: Orb) -> Unit)?
	{
		val special = other.special
		if (special != null)
		{
			if (special is DualMatch)
			{
				return fun (point: Point, grid: Grid, orb: Orb)
				{
					val coreTile = grid.tile(point)

					val hitSet = ObjectSet<Tile>()

					val effect = AssetManager.loadParticleEffect("SpecialExplosion")
					effect.colour = orb.sprite.colour
					effect.size = 4f
					effect.collisionFun = fun(cx: Int, pcy: Int)
					{

						val cy = (grid.height-1) - pcy
						val tile = grid.tile(cx, cy)
						if (tile != null && !hitSet.contains(tile) && tile.dist(point) < 4)
						{
							hitSet.add(tile)
							grid.pop(cx, cy, 0f, this@DualMatch, 1+2)
						}
					}


					coreTile?.effects?.add(effect)
				}
			}
			else if (special is Horizontal4)
			{
				return fun (point: Point, grid: Grid, orb: Orb)
				{
					popColumn(this, orb.sprite.colour, point.x-1, point.y, grid)
					popColumn(this, orb.sprite.colour, point.x, point.y, grid)
					popColumn(this, orb.sprite.colour, point.x+1, point.y, grid)
				}
			}
			else if (special is Vertical4)
			{
				return fun (point: Point, grid: Grid, orb: Orb)
				{
					popRow(this, orb.sprite.colour, point.x, point.y-1, grid)
					popRow(this, orb.sprite.colour, point.x, point.y, grid)
					popRow(this, orb.sprite.colour, point.x, point.y+1, grid)
				}
			}
		}

		return null
	}

	override fun apply() = fun (point: Point, grid: Grid, orb: Orb)
	{
		val coreTile = grid.tile(point)

		val hitSet = ObjectSet<Tile>()

		val effect = AssetManager.loadParticleEffect("SpecialExplosion")
		effect.colour = orb.sprite.colour
		effect.size = 3f
		effect.collisionFun = fun(cx: Int, pcy: Int)
		{

			val cy = (grid.height-1) - pcy
			val tile = grid.tile(cx, cy)
			if (tile != null && !hitSet.contains(tile) && tile.dist(point) < 3)
			{
				hitSet.add(tile)
				grid.pop(cx, cy, 0f, this@DualMatch, 1+2)
			}
		}


		coreTile?.effects?.add(effect)
	}
}

class Match5(orb: Orb) : Special(orb)
{
	val flightTime = 0.3f

	init
	{
		sprite = AssetManager.loadSprite("Oryx/Custom/items/gem", drawActualSize = true)
		sprite.colourAnimation = ChromaticAnimation.obtain().set(15f)
	}

	override fun copy(orb: Orb): Special = Match5(orb)

	override fun merge(other: Orb): ((point: Point, grid: Grid, orb: Orb) -> Unit)?
	{
		val special = other.special
		if (special != null)
		{
			if (special is Match5)
			{
				return fun (point: Point, grid: Grid, orb: Orb)
				{
					for (tile in grid.grid)
					{
						if (tile.canHaveOrb)
						{
							popTile(this, tile, point, grid)
						}
					}
				}
			}
			else
			{
				val key = other.key

				return fun (point: Point, grid: Grid, orb: Orb)
				{
					for (tile in grid.grid)
					{
						if (tile.orb?.key == key)
						{
							if (tile.orb!!.special == null)
							{
								tile.orb!!.special = special.copy(tile.orb!!)
							}
							else
							{
								val func = tile.orb!!.special!!.merge(other) ?: special.merge(tile.orb!!)
								tile.orb!!.armed = func
							}

							popTile(this, tile, point, grid, flightTime)
							val delay = tile.dist(point) * 0.1f

							val s = sprite.copy()
							s.drawActualSize = false
							s.animation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
							s.renderDelay = delay
							tile.effects.add(s)
						}
						else if (tile.monster != null)
						{
							popTile(this, tile, point, grid, flightTime)
							val delay = tile.dist(point) * 0.1f

							val s = sprite.copy()
							s.drawActualSize = false
							s.animation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
							s.renderDelay = delay
							tile.effects.add(s)
						}
					}
				}
			}
		}
		else
		{
			val key = other.key

			return fun (point: Point, grid: Grid, orb: Orb)
			{
				for (tile in grid.grid)
				{
					if (tile.orb?.key == key)
					{
						popTile(this, tile, point, grid, flightTime)
						val delay = tile.dist(point) * 0.1f

						val s = sprite.copy()
						s.drawActualSize = false
						s.animation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
						s.renderDelay = delay
						tile.effects.add(s)
					}
					else if (tile.monster != null)
					{
						popTile(this, tile, point, grid, flightTime)
						val delay = tile.dist(point) * 0.1f

						val s = sprite.copy()
						s.drawActualSize = false
						s.animation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
						s.renderDelay = delay
						tile.effects.add(s)
					}
				}
			}
		}
	}

	override fun apply(): (Point, Grid, orb: Orb) -> Unit
	{
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}