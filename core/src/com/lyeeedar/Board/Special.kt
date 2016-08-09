package com.lyeeedar.Board

import com.badlogic.gdx.math.Interpolation
import com.lyeeedar.Global
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BlinkAnimation
import com.lyeeedar.Sprite.SpriteAnimation.ChromaticAnimation
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import com.lyeeedar.Util.UnsmoothedPath
import com.lyeeedar.Util.getRotation

/**
 * Created by Philip on 30-Jul-16.
 */

abstract class Special(val orb: Orb)
{
	lateinit var sprite: Sprite

	abstract fun merge(other: Orb): ((point: Point, grid: Grid) -> Unit)?
	abstract fun apply(): (point: Point, grid: Grid) -> Unit
	abstract fun copy(orb: Orb): Special

	companion object
	{
		fun popTile(special: Special, tile: Tile, point: Point, grid: Grid, offset: Float = 0f)
		{
			val delay = tile.dist(point) * 0.1f + offset
			grid.pop(tile, delay + 0.2f, special, 1 + grid.level.player.physDam)

			val sprite = grid.level.player.specialHitEffect.copy()
			sprite.renderDelay = delay
			tile.effects.add(sprite)
		}

		fun popColumn(special: Special, x: Int, sy: Int, grid: Grid)
		{
			if (x < 0 || x >= grid.width) return

			for (y in sy+1..grid.height-1)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb) break
				popTile(special, tile, Point(x, sy), grid)
			}

			for (y in sy-1 downTo 0)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb) break
				popTile(special, tile, Point(x, sy), grid)
			}
		}

		fun popRow(special: Special, sx: Int, y: Int, grid: Grid)
		{
			if (y < 0 || y >= grid.height) return

			for (x in sx+1..grid.width-1)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb) break
				popTile(special, tile, Point(sx, y), grid)
			}

			for (x in sx-1 downTo 0)
			{
				val tile = grid.grid[x, y]
				if (!tile.canHaveOrb) break
				popTile(special, tile, Point(sx, y), grid)
			}
		}
	}
}

abstract class Match4(orb: Orb) : Special(orb)
{
	override fun merge(other: Orb): ((point: Point, grid: Grid) -> Unit)?
	{
		if (other.special != null && other.special is Match4)
		{
			return fun (point: Point, grid: Grid)
			{
				popColumn(this, point.x, point.y, grid)
				popRow(this, point.x, point.y, grid)
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
	override fun apply() = fun (point: Point, grid: Grid) {	popColumn(this, point.x, point.y, grid) }

}

class Vertical4(orb: Orb) : Match4(orb)
{
	init
	{
		sprite = AssetManager.loadSprite("Oryx/Custom/items/orb_hori", drawActualSize = true)
	}

	override fun copy(orb: Orb): Special = Vertical4(orb)
	override fun apply() = fun (point: Point, grid: Grid) {	popRow(this, point.x, point.y, grid) }
}

class DualMatch(orb: Orb) : Special(orb)
{
	init
	{
		sprite = AssetManager.loadSprite("Oryx/Custom/items/orb_dual", drawActualSize = true)
	}

	override fun copy(orb: Orb): Special = DualMatch(orb)

	override fun merge(other: Orb): ((point: Point, grid: Grid) -> Unit)?
	{
		val special = other.special
		if (special != null)
		{
			if (special is DualMatch)
			{
				return fun (point: Point, grid: Grid)
				{
					for (tile in grid.grid)
					{
						if (tile.taxiDist(point) < 4)
						{
							popTile(this, tile, point, grid)
						}
					}
				}
			}
			else if (special is Horizontal4)
			{
				return fun (point: Point, grid: Grid)
				{
					popColumn(this, point.x-1, point.y, grid)
					popColumn(this, point.x, point.y, grid)
					popColumn(this, point.x+1, point.y, grid)
				}
			}
			else if (special is Vertical4)
			{
				return fun (point: Point, grid: Grid)
				{
					popRow(this, point.x, point.y-1, grid)
					popRow(this, point.x, point.y, grid)
					popRow(this, point.x, point.y+1, grid)
				}
			}
		}

		return null
	}

	override fun apply() = fun (point: Point, grid: Grid)
	{
		for (tile in grid.grid)
		{
			if (tile.dist(point) < 3)
			{
				popTile(this, tile, point, grid)
			}
		}
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

	override fun merge(other: Orb): ((point: Point, grid: Grid) -> Unit)?
	{
		if (other.sinkable) return null

		val special = other.special
		if (special != null)
		{
			if (special is Match5)
			{
				return fun (point: Point, grid: Grid)
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

				return fun (point: Point, grid: Grid)
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
							s.spriteAnimation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
							s.renderDelay = delay
							tile.effects.add(s)
						}
						else if (tile.monster != null)
						{
							popTile(this, tile, point, grid, flightTime)
							val delay = tile.dist(point) * 0.1f

							val s = sprite.copy()
							s.drawActualSize = false
							s.spriteAnimation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
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

			return fun (point: Point, grid: Grid)
			{
				for (tile in grid.grid)
				{
					if (tile.orb?.key == key)
					{
						popTile(this, tile, point, grid, flightTime)
						val delay = tile.dist(point) * 0.1f

						val s = sprite.copy()
						s.drawActualSize = false
						s.spriteAnimation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
						s.renderDelay = delay
						tile.effects.add(s)
					}
					else if (tile.monster != null)
					{
						popTile(this, tile, point, grid, flightTime)
						val delay = tile.dist(point) * 0.1f

						val s = sprite.copy()
						s.drawActualSize = false
						s.spriteAnimation = MoveAnimation.obtain().set(flightTime, UnsmoothedPath(tile.getPosDiff(point)))
						s.renderDelay = delay
						tile.effects.add(s)
					}
				}
			}
		}
	}

	override fun apply(): (Point, Grid) -> Unit
	{
		throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}