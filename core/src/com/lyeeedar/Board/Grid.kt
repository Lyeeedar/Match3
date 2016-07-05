package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteAnimation.StretchAnimation
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Util.Array2D
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.Point
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

class Grid(val width: Int, val height: Int)
{
	val grid: Array2D<Tile> = Array2D(width, height ){ x, y -> Tile(x, y) }

	val validOrbs: Array<OrbDesc> = Array()

	val sunkCount: IntIntMap = IntIntMap()

	var selected: Point = Point.MINUS_ONE

	val animSpeed = 0.2f

	init
	{
		validOrbs.add(OrbDesc.load("Red"))
		validOrbs.add(OrbDesc.load("Green"))
		validOrbs.add(OrbDesc.load("Yellow"))
		validOrbs.add(OrbDesc.load("Blue"))
	}

	fun select(newSelection: Point)
	{
		if (hasAnim()) return

		tile(selected)?.isSelected = false

		if (selected != Point.MINUS_ONE && newSelection != Point.MINUS_ONE)
		{
			// check if within 1
			if (newSelection.dist(selected) == 1)
			{
				val oldTile = tile(selected)!!
				val newTile = tile(newSelection)!!

				selected = Point.MINUS_ONE

				val oldOrb = oldTile.orb ?: return
				val newOrb = newTile.orb ?: return

				oldTile.orb = newOrb
				newTile.orb = oldOrb

				val matches = findMatches()
				if (matches.size == 0)
				{
					oldTile.orb = oldOrb
					newTile.orb = newOrb

					oldOrb.sprite.spriteAnimation = BumpAnimation.obtain().set(animSpeed, Direction.Companion.getDirection(oldTile, newTile))
				}
				else
				{
					oldOrb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, newTile.getPosDiff(oldTile), MoveAnimation.MoveEquation.LINEAR)
					newOrb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, oldTile.getPosDiff(newTile), MoveAnimation.MoveEquation.LINEAR)
				}
			}
			else
			{
				selected = newSelection
				tile(selected)?.isSelected = true
			}
		}
		else
		{
			selected = newSelection
			tile(selected)?.isSelected = true
		}
	}

	fun cascade(): Boolean
	{
		val vert = cascadeVert()
		val diag = if (vert) cascadeDiag() else false

		val sunkComplete = processSunk()
		val spawnComplete = spawn()

		return vert && diag && sunkComplete && spawnComplete
	}

	fun cascadeVert(): Boolean
	{
		var cascadeComplete = true

		// do below first
		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val tile = grid[x, y]
				val orb = tile.orb
				if (orb != null && !orb.armed)
				{
					val below = tile(x, y+1)
					// check for drop

					if (below != null && below.canHaveOrb && below.orb == null && (!below.canSink || orb.desc.canSink))
					{
						orb.sprite.spriteAnimation = MoveAnimation.obtain().set(0.15f, below.getPosDiff(tile), MoveAnimation.MoveEquation.LINEAR)
						below.orb = orb
						tile.orb = null
						cascadeComplete = false
					}
				}
			}
		}

		return cascadeComplete
	}

	fun cascadeDiag(): Boolean
	{
		var cascadeComplete = true

		// now try angles
		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				val tile = grid[x, y]
				val orb = tile.orb
				if (orb != null && !orb.armed)
				{
					val diagL = tile(x - 1, y + 1)
					val diagR = tile(x + 1, y + 1)

					// check for drop
					if (diagL != null && diagL.canHaveOrb && diagL.orb == null)
					{
						orb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, diagL.getPosDiff(tile), MoveAnimation.MoveEquation.LINEAR)
						diagL.orb = orb
						tile.orb = null
						cascadeComplete = false
					} else if (diagR != null && diagR.canHaveOrb && diagR.orb == null)
					{
						orb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, diagR.getPosDiff(tile), MoveAnimation.MoveEquation.LINEAR)
						diagR.orb = orb
						tile.orb = null
						cascadeComplete = false
					}
				}
			}
		}

		return cascadeComplete
	}

	fun hasAnim(): Boolean
	{
		var hasAnim = false
		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val orb = grid[x, y].orb
				if (orb != null && orb.sprite.spriteAnimation != null)
				{
					hasAnim = true
					break
				}
			}
		}

		return hasAnim
	}

	fun update()
	{
		// if in update, do animations
		if (!hasAnim())
		{
			cleanup()

			val cascadeComplete = cascade()

			if (cascadeComplete)
			{
				val detonateComplete = detonate()

				if (detonateComplete)
				{
					val matchComplete = match()
				}
			}
		}
		// else cascade
		// else detonate
		// else match
		// else wait for input
	}

	fun cleanup()
	{
		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				val tile = grid[x, y]
				val orb = tile.orb ?: continue

				if (orb.markedForDeletion && orb.sprite.spriteAnimation == null)
				{
					tile.orb = null
				}
			}
		}
	}

	fun spawn(): Boolean
	{
		var complete = true
		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val tile = grid[x, y]
				if (tile.canSpawn && tile.orb == null)
				{
					val orb = Orb(validOrbs.random())
					orb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, floatArrayOf(0f, Global.tileSize.toFloat()), MoveAnimation.MoveEquation.LINEAR)
					tile.orb = orb
					complete = false
				}
			}
		}

		return complete
	}

	fun match(): Boolean
	{
		val matches = findMatches()
		clearMatches(matches)

		return matches.size == 0
	}

	fun detonate(): Boolean
	{
		return true
	}

	fun processSunk(): Boolean
	{
		var complete = true

		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val tile = grid[x, y]
				val orb = tile.orb
				if (tile.canSink && orb != null)
				{
					tile.orb = null
					var count = sunkCount[orb.key, 0]
					count++
					sunkCount.put(orb.key, count)

					complete = false
				}
			}
		}

		return complete
	}

	fun fill()
	{
		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				if (grid[x, y].canHaveOrb)
				{
					val valid = Array(validOrbs)
					val l1 = tile(x-1, y)
					val l2 = tile(x-2, y)
					val u1 = tile(x, y-1)
					val u2 = tile(x, y-2)

					if (l1?.orb != null && l2?.orb != null && l1?.orb?.key == l2?.orb?.key)
					{
						valid.removeValue(l1!!.orb!!.desc, true)
					}
					if (u1?.orb != null && u2?.orb != null && u1?.orb?.key == u2?.orb?.key)
					{
						valid.removeValue(u1!!.orb!!.desc, true)
					}

					val desc = valid.random()
					val orb = Orb(desc)
					grid[x, y].orb = orb
				}
			}
		}
	}

	fun findMatches() : Array<Pair<Point, Point>>
	{
		val matches = Array<Pair<Point, Point>>()

		fun addMatch(p1: Point, p2: Point)
		{
			val dst = p1.dist(p2)
			if (dst >= 2)
			{
				matches.add(Pair(p1, p2))
			}
		}

		// Match rows
		for (y in 0..height-1)
		{
			var sx = -1
			var key = -1

			for (x in 0..width-1)
			{
				val tile = grid[x, y]
				val orb = tile.orb

				if (orb == null)
				{
					if (key != -1)
					{
						addMatch(Point(sx,y), Point(x-1,y))
					}

					key = -1
				}
				else
				{
					if (orb.key != key)
					{
						// if we were matching, close matching
						if (key != -1)
						{
							addMatch(Point(sx,y), Point(x-1,y))
						}

						sx = x
						key = orb.key
					}
				}
			}

			if (key != -1)
			{
				addMatch(Point(sx,y), Point(width-1,y))
			}
		}

		// Match columns
		for (x in 0..width-1)
		{
			var sy = -1
			var key = -1

			for (y in 0..height-1)
			{
				val tile = grid[x, y]
				val orb = tile.orb

				if (orb == null)
				{
					if (key != -1)
					{
						addMatch(Point(x,sy), Point(x,y-1))
					}

					key = -1
				}
				else
				{
					if (orb.key != key)
					{
						// if we were matching, close matching
						if (key != -1)
						{
							addMatch(Point(x,sy), Point(x,y-1))
						}

						sy = y
						key = orb.key
					}
				}
			}

			if (key != -1)
			{
				addMatch(Point(x,sy), Point(x,height-1))
			}
		}

		return matches
	}

	fun clearMatches(matches: Array<Pair<Point, Point>>)
	{
		for (match in matches)
		{
			val xdiff = match.second.x - match.first.x
			val ydiff = match.second.y - match.first.y

			val diff = Math.max(xdiff, ydiff)

			val xstep = xdiff.toFloat() / diff.toFloat()
			val ystep = ydiff.toFloat() / diff.toFloat()

			for (i in 0..diff)
			{
				val x = match.first.x + (xstep * i).toInt()
				val y = match.first.y + (ystep * i).toInt()

				val tile = tile(x, y) ?: continue
				val orb = tile.orb ?: continue

				orb.sprite.spriteAnimation = StretchAnimation.obtain().set(animSpeed, floatArrayOf(0f, 0f), 0f, StretchAnimation.StretchEquation.EXPAND)
				orb.markedForDeletion = true
			}

			// if 4 or 5 match then spawn new orb
		}
	}

	fun tile(point: Point): Tile? = tile(point.x, point.y)

	fun tile(x: Int, y:Int): Tile?
	{
		if (x >= 0 && y >= 0 && x < width && y < height) return grid[x, y]
		else return null
	}

	companion object
	{
		fun load(path: String) : Grid
		{
			val xml = XmlReader().parse(Gdx.files.internal("Boards/$path.xml"))

			val rows = xml.getChildByName("Rows")
			val width = rows.getChild(0).text.length
			val height = rows.childCount

			val grid = Grid(width, height)

			for (x in 0..width-1)
			{
				for (y in 0..height-1)
				{
					val tile = grid.tile(x, y)!!
					val char = rows.getChild(y).text[x]

					if (char == '#')
					{
						tile.canHaveOrb = false
					}
					else if (char == 's')
					{
						tile.canSpawn = true
					}
					else if (char == 'v')
					{
						tile.canSink = true
					}
				}
			}

			grid.fill()

			return grid
		}
	}
}