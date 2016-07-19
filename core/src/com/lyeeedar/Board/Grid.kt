package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
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
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.Sprite.TilingSprite
import com.lyeeedar.UI.FullscreenMessage
import com.lyeeedar.Util.*
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

class Grid(val width: Int, val height: Int, val level: Level)
{
	val grid: Array2D<Tile> = Array2D(width, height ){ x, y -> Tile(x, y) }
	val spawnCount: IntArray = IntArray(width)

	val refillSprite = AssetManager.loadSprite("EffectSprites/Heal/Heal", 0.1f)

	// ----------------------------------------------------------------------
	val validOrbs: Array<OrbDesc> = Array()
	val specialOrbs: Array<Explosion> = Array()

	// ----------------------------------------------------------------------
	var selected: Point = Point.MINUS_ONE
	var toSwap: Pair<Point, Point>? = null

	val animSpeed = 0.15f

	// ----------------------------------------------------------------------
	val onTurn = Event0Arg()
	val onTime = Event1Arg<Float>()
	val onPop = Event1Arg<Orb>()
	val onSunk = Event1Arg<Orb>()
	val onDamaged = Event0Arg()

	// ----------------------------------------------------------------------
	var noMatchTimer = 0f
	var matchHint: Pair<Point, Point>? = null

	// ----------------------------------------------------------------------
	val motes: Array<Mote> = Array(false, 16)

	// ----------------------------------------------------------------------
	init
	{
		val xml = XmlReader().parse(Gdx.files.internal("Orbs/Orbs.xml"))

		val template = xml.getChildByName("Template")
		val baseSprite = AssetManager.loadSprite(template.getChildByName("Sprite"))
		val deathSprite = AssetManager.loadSprite(template.getChildByName("Death"))

		val types = xml.getChildByName("Types")
		for (i in 0..types.childCount-1)
		{
			val type = types.getChild(i)
			val name = type.name
			val colour = AssetManager.loadColour(type.getChildByName("Colour"))

			val orbDesc = OrbDesc()
			orbDesc.sprite = baseSprite.copy()
			orbDesc.sprite.colour = colour
			orbDesc.name = name

			orbDesc.death = deathSprite
			orbDesc.death.colour = colour

			validOrbs.add(orbDesc)
		}
	}

	// ----------------------------------------------------------------------
	fun getExplosion(count:Int, dir: Direction): Explosion?
	{
		var best: Explosion? = null

		for (special in specialOrbs)
		{
			if (special.count <= count && special.dir == dir)
			{
				if (best == null)
				{
					best = special
				}
				else if (special.count > best.count)
				{
					best = special
				}
			}
		}

		return best
	}

	// ----------------------------------------------------------------------
	fun select(newSelection: Point)
	{
		if (hasAnim() || level.completed) return

		tile(selected)?.isSelected = false

		if (selected != Point.MINUS_ONE && newSelection != Point.MINUS_ONE)
		{
			// check if within 1
			if (newSelection.dist(selected) == 1)
			{
				toSwap = Pair(selected, newSelection)
				selected = Point.MINUS_ONE
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

	// ----------------------------------------------------------------------
	fun cascade(): Boolean
	{
		for (x in 0..width - 1) spawnCount[x] = 0

		var cascadeComplete = false

		while (!cascadeComplete)
		{
			cascadeComplete = true

			for (x in 0..width - 1)
			{
				val done = cascadeColumn(x)
				if (!done) cascadeComplete = false
			}
		}

		cascadeComplete = makeAnimations()

		val sunkComplete = processSunk()

		return cascadeComplete && sunkComplete
	}

	// ----------------------------------------------------------------------
	fun cascadeColumn(x: Int) : Boolean
	{
		var complete = true

		var currentY = height-1
		while (currentY >= 0)
		{
			val tile = grid[x, currentY]

			// read up column, find first gap
			if (tile.canHaveOrb && tile.orb == null)
			{
				// if gap found read up until solid / spawner
				var found: Tile? = null

				for (searchY in currentY downTo -1)
				{
					val stile = if (searchY >= 0) grid[x, searchY] else null
					if (stile == null)
					{
						found = tile
						break
					}
					else if (stile.orb != null)
					{
						val orb = stile.orb!!
						if (!orb.armed && !orb.sealed) found = stile
						break
					}
					else if (!stile.canHaveOrb)
					{
						break
					}
					else if (stile.block != null)
					{
						break
					}
				}

				// pull solid / spawn new down
				if (found != null)
				{
					var orb: Orb

					if (found == tile)
					{
						orb = Orb(validOrbs.random())
						orb.movePoints.add(Point(x, -1))
						orb.spawnCount = spawnCount[x]

						spawnCount[x]++
					}
					else
					{
						orb = found.orb!!
						found.orb = null
						if (orb.movePoints.size == 0) orb.movePoints.add(found)
					}

					orb.movePoints.add(tile)
					tile.orb = orb

					complete = false
				}
			}

			currentY--
		}

		// walk down column
		// each block with a clear, push 1 orb into the top from a neighbour

		if (complete)
		{
			currentY = 0
			var lookingForOrb = 0 // 0 = not looking, 1 = looking, 2 = placed
			while (currentY < height)
			{
				val tile = grid[x, currentY]
				if (tile.canHaveOrb && tile.orb == null && tile.block == null)
				{
					if (lookingForOrb == 0)
					{
						lookingForOrb = 1
					}
				}
				else if (!tile.canHaveOrb || lookingForOrb == 2 || tile.block != null)
				{
					lookingForOrb = 0
				}
				else if (tile.orb != null)
				{
					lookingForOrb = 0
				}

				if (lookingForOrb == 1)
				{
					// check neighbours for orb
					val diagL = tile(x - 1, currentY - 1)
					val diagR = tile(x + 1, currentY - 1)

					val diagLValid = diagL != null && diagL.orb != null && !diagL.orb!!.armed && !diagL.orb!!.sealed
					val diagRValid = diagR != null && diagR.orb != null && !diagR.orb!!.armed && !diagR.orb!!.sealed

					if (diagLValid || diagRValid)
					{
						fun pullIn(t: Tile)
						{
							val orb = t.orb!!
							t.orb = null

							if (orb.movePoints.size == 0) orb.movePoints.add(t)

							tile.orb = orb

							orb.movePoints.add(tile)

							complete = false
						}

						// if found one, pull in and set to 2
						if (diagLValid && diagRValid)
						{
							if (MathUtils.randomBoolean())
							{
								pullIn(diagL!!)
							}
							else
							{
								pullIn(diagR!!)
							}
						}
						else if (diagLValid)
						{
							pullIn(diagL!!)
						}
						else
						{
							pullIn(diagR!!)
						}

						lookingForOrb = 2
					}


				}

				currentY++
			}
		}

		return complete
	}

	// ----------------------------------------------------------------------
	fun makeAnimations(): Boolean
	{
		var doneAnimation = true

		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val orb = grid[x, y].orb ?: continue

				if (orb.movePoints.size > 0)
				{
					val firstIsNull = orb.movePoints[0].y == -1

					val pathPoints = Array(orb.movePoints.size){ i -> Vector2(orb.movePoints[i].x * Global.tileSize, orb.movePoints[i].y * Global.tileSize) }
					for (point in pathPoints)
					{
						point.x -= pathPoints.last().x
						point.y = pathPoints.last().y - point.y
					}

					val path = UnsmoothedPath(pathPoints)

					orb.sprite.spriteAnimation = MoveAnimation.obtain().set(0.1f + pathPoints.size * animSpeed, path, Interpolation.exp5In)
					orb.sprite.renderDelay = orb.spawnCount * 0.1f
					orb.spawnCount = 0

					if (firstIsNull)
					{
						orb.sprite.spriteAnimation = StretchAnimation.obtain().set(animSpeed, null, 0f, StretchAnimation.StretchEquation.EXPAND)
						orb.sprite.showBeforeRender = false
					}

					orb.movePoints.clear()

					doneAnimation = false
				}
			}
		}

		return doneAnimation
	}

	// ----------------------------------------------------------------------
	fun hasAnim(): Boolean
	{
		var hasAnim = false
		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				val tile = grid[x, y]
				if (tile.effects.size > 0)
				{
					hasAnim = true
					break
				}

				val orb = tile.orb
				if (orb != null && orb.sprite.spriteAnimation != null)
				{
					hasAnim = true
					break
				}
			}
		}

		return hasAnim
	}

	// ----------------------------------------------------------------------
	fun update(delta: Float)
	{
		// if in update, do animations
		cleanup()

		if (!hasAnim() && FullscreenMessage.instance == null)
		{
			val cascadeComplete = cascade()

			if (cascadeComplete)
			{
				val matchComplete = match()

				if (matchComplete)
				{
					val detonateComplete = detonate()

					if (!level.completed)
					{
						matchHint = findValidMove()

						if (detonateComplete && matchHint == null)
						{
							FullscreenMessage("No valid moves. Randomising.", "", { refill() }).show()
						}
						else
						{
							noMatchTimer += delta

							// handle input
							if (toSwap != null)
							{
								val swapSuccess = swap()
								if (swapSuccess) onTurn()

								noMatchTimer = 0f
							}

							onTime(delta)
						}
					}
				}
			}
		}

		for (mote in motes)
		{
			mote.update(delta)
		}

		motes.removeAll{ it.done }
	}

	// ----------------------------------------------------------------------
	fun swap(): Boolean
	{
		val oldTile = tile(toSwap!!.first)
		val newTile = tile(toSwap!!.second)

		toSwap = null

		if (oldTile == null || newTile == null) return false

		val oldOrb = oldTile.orb ?: return false
		val newOrb = newTile.orb ?: return false
		if (oldOrb.sealed || newOrb.sealed) return false

		oldTile.orb = newOrb
		newTile.orb = oldOrb

		val matches = findMatches()
		if (matches.size == 0)
		{
			oldTile.orb = oldOrb
			newTile.orb = newOrb

			oldOrb.sprite.spriteAnimation = BumpAnimation.obtain().set(animSpeed * 2f, Direction.Companion.getDirection(oldTile, newTile))
			return false
		}
		else
		{
			oldOrb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(newTile.getPosDiff(oldTile)), Interpolation.linear)
			newOrb.sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(oldTile.getPosDiff(newTile)), Interpolation.linear)
			return true
		}
	}

	// ----------------------------------------------------------------------
	fun cleanup()
	{
		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				val tile = grid[x, y]

				if (tile.orb != null)
				{
					val orb = tile.orb!!
					orb.x = x
					orb.y = y

					if (orb.markedForDeletion && orb.sprite.spriteAnimation == null && !orb.armed)
					{
						tile.orb = null
						onPop(orb)
					}
				}
				else if (tile.block != null)
				{
					val block = tile.block!!
					if (block.count <= 0)
					{
						tile.block = null
						tile.effects.add(block.death.copy())
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun match(): Boolean
	{
		val match5 = findMatches(5, true)
		clearMatches(match5)

		val match4 = findMatches(4, true)
		clearMatches(match4)

		val match3 = findMatches(3, true)
		clearMatches(match3)

		return match5.size == 0 && match4.size == 0 && match3.size == 0
	}

	// ----------------------------------------------------------------------
	fun findValidMove() : Pair<Point, Point>?
	{
		// find all 2 matches
		val matches = findMatches(2)

		// if none then no valid
		if (matches.size == 0) return null

		for (match in matches)
		{
			// check the 3 tiles around each end to see if it contains one of the correct colours
			val dir = Direction.getDirection(match.first, match.second)
			val key = grid[match.first].orb!!.key

			fun checkSurrounding(point: Point, dir: Direction, key: Int): Pair<Point, Point>?
			{
				val targetTile = tile(point)
				if (targetTile == null || targetTile.block != null || targetTile.orb?.sealed ?: false || !targetTile.canHaveOrb) return null

				fun canMatch(point: Point): Boolean
				{
					val tile = tile(point) ?: return false
					val orb = tile.orb ?: return false
					if (orb.sealed || orb.markedForDeletion) return false
					return orb.key == key
				}

				// check + dir
				if (canMatch(point + dir)) return Pair(point, point+dir)
				if (canMatch(point + dir.clockwise.clockwise)) return Pair(point, point+dir.clockwise.clockwise)
				if (canMatch(point + dir.anticlockwise.anticlockwise)) return Pair(point, point+dir.anticlockwise.anticlockwise)

				return null
			}

			// the one before first is at first-dir
			val beforeFirst = match.first + dir.opposite
			val beforeFirstPair = checkSurrounding(beforeFirst, dir.opposite, key)
			if (beforeFirstPair != null) return beforeFirstPair

			val afterSecond = match.second + dir
			val afterSecondPair = checkSurrounding(afterSecond, dir, key)
			if (afterSecondPair != null) return afterSecondPair
		}

		fun getTileKey(x: Int, y: Int): Int
		{
			val tile = tile(x, y) ?: return -1
			val orb = tile.orb ?: return -1
			if (orb.sealed || orb.markedForDeletion) return -1

			return orb.key
		}

		// check diamond pattern
		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				for (dir in Direction.CardinalValues)
				{
					val key = getTileKey(x + dir.x, y + dir.y)
					if (key != -1)
					{
						val d1 = dir.clockwise.clockwise
						val k1 = getTileKey(x + d1.x, y + d1.y)

						val d2 = dir.anticlockwise.anticlockwise
						val k2 = getTileKey(x + d2.x, y + d2.y)

						if (key == k1 && key == k2)
						{
							return Pair(Point(x, y), Point(x + dir.x, y + dir.y))
						}
					}
				}
			}
		}

		// else no valid

		return null
	}

	// ----------------------------------------------------------------------
	fun detonate(): Boolean
	{
		var complete = true

		val tilesToDetonate = Array<Tile>()

		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				val tile = grid[x, y]
				val orb = tile.orb ?: continue

				if (orb.armed)
				{
					tilesToDetonate.add(tile)
				}
			}
		}

		for (tile in tilesToDetonate)
		{
			detonatePattern(tile.x, tile.y, tile.orb!!.explosion!!)

			tile.orb!!.armed = false
			complete = false
		}

		return complete
	}

	// ----------------------------------------------------------------------
	fun detonatePattern(x: Int, y: Int, explosion: Explosion)
	{
		for (dir in Direction.CardinalValues)
		{
			val points = explosion.dirs[dir]
			for (point in points)
			{
				var delay = 0f

				val current = Point(x, y) + point

				while (true)
				{
					val tile = tile(current) ?: break

					if (!tile.canHaveOrb) break

					pop(tile.x, tile.y, delay+0.2f)

					val sprite = explosion.sprite.copy()
					sprite.renderDelay = delay
					tile.effects.add(sprite)

					delay += 0.1f

					current += dir
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun processSunk(): Boolean
	{
		var complete = true

		for (x in 0..width-1)
		{
			val tile = grid[x, height-1]
			val orb = tile.orb
			if (orb != null && orb.sinkable)
			{
				tile.orb = null
				onSunk(orb)

				complete = false
			}
		}

		return complete
	}

	// ----------------------------------------------------------------------
	fun refill()
	{
		val tempgrid: Array2D<Tile> = Array2D(width, height ){ x, y -> Tile(x, y) }
		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				tempgrid[x, y].contents = grid[x, y].contents
			}
		}

		fill()

		for (x in 0..width-1)
		{
			for (y in 0..height - 1)
			{
				val oldorb = tempgrid[x, y].orb
				if (oldorb == null) grid[x, y].contents = tempgrid[x, y].contents
				else
				{
					val orb = grid[x, y].orb!!

					if (oldorb.explosion != null) orb.explosion = oldorb.explosion
					if (oldorb.sealed) orb.sealed = true

					val delay = grid[x, y].taxiDist(Point.ZERO).toFloat() * 0.1f
					orb.sprite.renderDelay = delay + 0.2f
					orb.sprite.showBeforeRender = false

					val sprite = refillSprite.copy()
					sprite.colour = orb.sprite.colour
					sprite.renderDelay = delay
					sprite.showBeforeRender = false

					grid[x, y].effects.add(sprite)
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun fill()
	{
		for (x in 0..width-1)
		{
			for (y in 0..height-1)
			{
				if (grid[x, y].canHaveOrb && grid[x, y].block == null)
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

	// ----------------------------------------------------------------------
	fun findMatches() : Array<Pair<Point, Point>>
	{
		val matches = Array<Pair<Point, Point>>(false, 16)

		matches.addAll(findMatches(3))
		matches.addAll(findMatches(4))
		matches.addAll(findMatches(5))

		// clear duplicates
		var i = 0
		while (i < matches.size)
		{
			val pair = matches[i]

			var ii = i+1
			while (ii < matches.size)
			{
				val opair = matches[ii]

				if (opair.equals(pair))
				{
					matches.removeIndex(ii)
				}
				else
				{
					ii++
				}
			}

			i++
		}

		return matches
	}

	// ----------------------------------------------------------------------
	fun findMatches(length: Int, exact: Boolean = false) : Array<Pair<Point, Point>>
	{
		val matches = Array<Pair<Point, Point>>()

		fun addMatch(p1: Point, p2: Point)
		{
			fun check(dst: Int): Boolean
			{
				if (exact) return dst == length-1
				else return dst >= length-1
			}

			val dst = p1.dist(p2)
			if (check(dst))
			{
				// check not already added
				var found = false

				for (pair in matches)
				{
					if (pair.first == p1 && pair.second == p2)
					{
						found = true
						break
					}
				}

				if (!found) matches.add(Pair(p1, p2))
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

	// ----------------------------------------------------------------------
	fun clearMatches(matches: Array<Pair<Point, Point>>)
	{
		for (match in matches)
		{
			val xdiff = match.second.x - match.first.x
			val ydiff = match.second.y - match.first.y

			val diff = Math.max(xdiff, ydiff)

			val xstep = xdiff.toFloat() / diff.toFloat()
			val ystep = ydiff.toFloat() / diff.toFloat()

			val dir = Direction.getDirection(match.first, match.second)
			val desc = tile(match.first)?.orb?.desc

			val middle = tile(match.first.x + xdiff / 2, match.first.y + ydiff / 2)

			val coreTiles = Array<Tile>()

			for (i in 0..diff)
			{
				val x = match.first.x + (xstep * i).toInt()
				val y = match.first.y + (ystep * i).toInt()

				pop(x, y, 0f)

				coreTiles.add(grid[x, y])

				if (diff > 2 && desc != null)
				{
					val sprite = desc.sprite.copy()
					sprite.drawActualSize = false
					sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(middle!!.getPosDiff(x, y)), Interpolation.linear)

					middle.effects.add(sprite)
				}
			}

			// build border list
			val borderTiles = Array<Tile>()
			for (tile in coreTiles)
			{
				for (dir in Direction.CardinalValues)
				{
					val t = tile(tile.x + dir.x, tile.y + dir.y) ?: continue
					if (!coreTiles.contains(t, true) && !borderTiles.contains(t, true))
					{
						borderTiles.add(t)
					}
				}
			}

			// pop all borders
			for (t in borderTiles)
			{
				if (t.block != null)
				{
					t.block!!.count--
				}
				if (t.orb != null && t.orb!!.sealed)
				{
					t.orb!!.sealed = false
					t.effects.add(t.orb!!.sealBreak)
				}
			}

			// if 4 or 5 match then spawn new orb
			if (diff > 2 && desc != null)
			{
				if (middle?.orb?.explosion == null)
				{
					val special = getExplosion(diff+1, dir)

					if (special != null)
					{
						val orb = Orb(desc)
						orb.explosion = special

						if (middle != null && middle.orb != null)
						{
							middle.orb!!.x = middle.x
							middle.orb!!.y = middle.y
							onPop(middle.orb!!)
						}
						middle?.orb = orb
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun pop(x: Int, y: Int, delay: Float)
	{
		val tile = tile(x, y) ?: return

		if (tile.block != null)
		{
			tile.block!!.count--
			return
		}

		val orb = tile.orb ?: return
		if (orb.markedForDeletion) return // already completed, dont do it again
		if (orb.sinkable) return

		if (orb.sealed)
		{
			orb.sealed = false
			orb.sealBreak.renderDelay = delay
			tile.effects.add(orb.sealBreak)
			return
		}

		orb.markedForDeletion = true

		if (orb.explosion == null)
		{
			orb.sprite.visible = false

			val sprite = orb.desc.death.copy()
			sprite.colour = orb.sprite.colour
			sprite.renderDelay = delay

			tile.effects.add(sprite)
		}
		else
		{
			orb.armed = true
		}
	}

	// ----------------------------------------------------------------------
	fun tile(point: Point): Tile? = tile(point.x, point.y)

	// ----------------------------------------------------------------------
	fun tile(x: Int, y:Int): Tile?
	{
		if (x >= 0 && y >= 0 && x < width && y < height) return grid[x, y]
		else return null
	}

	// ----------------------------------------------------------------------
	fun buildTilingBitflag(bitflag: EnumBitflag<Direction>, x: Int, y: Int, id: Long)
	{
		// Build bitflag of surrounding tiles
		bitflag.clear()
		for (dir in Direction.Values)
		{
			val tile = tile( x - dir.x, y - dir.y )

			if (tile == null || tile.sprite.tilingSprite == null || tile.sprite.tilingSprite?.checkID != id)
			{
				bitflag.setBit(dir)
			}
		}
	}

	// ----------------------------------------------------------------------
	fun loadSpecials()
	{
		val xml = XmlReader().parse(Gdx.files.internal("Orbs/Specials.xml"))

		for (i in 0..xml.childCount-1)
		{
			val match = xml.getChild(i) // name in from matchX
			val name = match.name.replace("Match", "")
			val count = name.toInt()

			for (ii in 0..match.childCount-1)
			{
				val special = match.getChild(ii)
				val dir = Direction.valueOf(special.name)

				val explosion = Explosion.load(special)
				explosion.dir = dir
				explosion.count = count

				specialOrbs.add(explosion)
			}
		}
	}
}