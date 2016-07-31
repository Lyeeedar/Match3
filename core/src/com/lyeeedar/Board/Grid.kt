package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.SpaceSlot
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteAnimation.AlphaAnimation
import com.lyeeedar.Sprite.SpriteAnimation.BumpAnimation
import com.lyeeedar.Sprite.SpriteAnimation.ExpandAnimation
import com.lyeeedar.Sprite.SpriteAnimation.MoveAnimation
import com.lyeeedar.Sprite.SpriteRenderer
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.Sprite.TilingSprite
import com.lyeeedar.UI.FullscreenMessage
import com.lyeeedar.Util.*
import com.sun.org.apache.xpath.internal.operations.Or
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

class Grid(val width: Int, val height: Int, val level: Level)
{
	val grid: Array2D<Tile> = Array2D(width, height ){ x, y -> Tile(x, y) }
	val spawnCount: Array2D<Int> = Array2D<Int>(width, height+1){ x, y -> 0 }

	val refillSprite = AssetManager.loadSprite("EffectSprites/Heal/Heal", 0.1f)

	// ----------------------------------------------------------------------
	val validOrbs: Array<OrbDesc> = Array()

	// ----------------------------------------------------------------------
	var selected: Point = Point.MINUS_ONE
	var toSwap: Pair<Point, Point>? = null
	var lastSwapped: Point = Point.MINUS_ONE

	val animSpeed = 0.15f

	// ----------------------------------------------------------------------
	val onTurn = Event0Arg()
	val onTime = Event1Arg<Float>()
	val onPop = Event2Arg<Orb, Float>()
	val onSunk = Event1Arg<Orb>()
	val onDamaged = Event1Arg<Monster>()
	val onSpawn = Event1Arg<Orb>()
	val onAttacked = Event1Arg<Orb>()

	// ----------------------------------------------------------------------
	var noMatchTimer = 0f
	var matchHint: Pair<Point, Point>? = null

	// ----------------------------------------------------------------------
	var activeAbility: Ability? = null
		set(value)
		{
			field = value

			if (value == null)
			{
				for (tile in grid)
				{
					tile.isSelected = false
				}
			}
			else
			{
				tile(selected)?.isSelected = false
				selected = Point.MINUS_ONE
			}
		}

	lateinit var updateFuns: kotlin.Array<() -> Boolean>

	// ----------------------------------------------------------------------
	init
	{
		updateFuns = arrayOf(
				fun() = cascade(),
				fun() = match(),
				fun() = sink(),
				fun() = detonate()
		)

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

		onTurn += {

			for (tile in grid)
			{
				val orb = tile.orb
				if (orb != null)
				{
					// Process attacks
					if (orb.hasAttack)
					{
						orb.attackTimer--
					}
				}

				// process monsters
				val monster = tile.monster
				if (monster != null && tile == monster.tiles[0, 0])
				{
					monster.onTurn(this@Grid)
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun getSpecial(count1: Int, count2: Int, dir: Direction, orb: Orb): Special?
	{
		if (count1 >= 5 || count2 >= 5)
		{
			return Match5(orb)
		}
		else if (count1 > 0 && count2 > 0)
		{
			return DualMatch(orb)
		}
		else if (dir.y != 0 && count1 == 4)
		{
			return Vertical4(orb)
		}
		else if (dir.x != 0 && count1 == 4)
		{
			return Horizontal4(orb)
		}

		return null
	}

	// ----------------------------------------------------------------------
	fun select(newSelection: Point)
	{
		if (hasAnim() || level.completed) return

		if (activeAbility != null)
		{
			val newTile = tile(newSelection) ?: return
			if (!activeAbility!!.targetter.isValid(newTile)) return

			if (newTile.isSelected)
			{
				newTile.isSelected = false
				activeAbility!!.selectedTargets.removeValue(newTile, true)
			}
			else
			{
				newTile.isSelected = true
				activeAbility!!.selectedTargets.add(newTile)
			}

			if (activeAbility!!.selectedTargets.size >= activeAbility!!.targets)
			{
				activeAbility!!.activate(this)
				activeAbility = null
			}
		}
		else
		{
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
				val newTile = tile(newSelection)
				if (newTile != null && newTile.canHaveOrb)
				{
					selected = newSelection
					newTile.isSelected = true
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun cascade(): Boolean
	{
		for (x in 0..width - 1) for (y in 0..height -1 ) spawnCount[x, y] = 0

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

		return cascadeComplete
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
			if (tile.canHaveOrb && tile.orb == null && tile.monster == null)
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
						if (orb.armed == null && !orb.sealed) found = stile
						break
					}
					else if (stile.chest != null)
					{
						found = stile
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
					else if (stile.monster != null)
					{
						break
					}
				}

				// pull solid / spawn new down
				if (found != null)
				{
					var orb: Orb? = null

					if (found == tile)
					{
						orb = Orb(validOrbs.random())
						orb.movePoints.add(Point(x, -1))
						orb.spawnCount = spawnCount[x, 0]

						spawnCount[x, 0]++

						onSpawn(orb)
					}
					else if (found.chest != null)
					{
						val o = found.chest!!.spawn(this)
						if (o != null)
						{
							orb = o
							orb.movePoints.add(Point(x, found.y))
							orb.spawnCount = spawnCount[x, found.y + 1]

							spawnCount[x, found.y + 1]++

							onSpawn(orb)
						}
					}
					else
					{
						orb = found.orb!!
						found.orb = null
						if (orb.movePoints.size == 0) orb.movePoints.add(found)
					}

					if (orb != null)
					{
						orb.movePoints.add(tile)
						tile.orb = orb

						complete = false
					}
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
				if (tile.canHaveOrb && tile.orb == null && tile.block == null && tile.monster == null)
				{
					if (lookingForOrb == 0)
					{
						lookingForOrb = 1
					}
				}
				else if (!tile.canHaveOrb || lookingForOrb == 2 || tile.block != null || tile.monster != null)
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

					val diagLValid = diagL != null && diagL.orb != null && diagL.orb!!.armed == null && !diagL.orb!!.sealed
					val diagRValid = diagR != null && diagR.orb != null && diagR.orb!!.armed == null && !diagR.orb!!.sealed

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
					val firstIsNull = orb.spawnCount >= 0

					val pathPoints = Array(orb.movePoints.size){ i -> Vector2(orb.movePoints[i].x * Global.tileSize, orb.movePoints[i].y * Global.tileSize) }
					for (point in pathPoints)
					{
						point.x -= pathPoints.last().x
						point.y = pathPoints.last().y - point.y
					}

					val path = UnsmoothedPath(pathPoints)

					orb.sprite.spriteAnimation = MoveAnimation.obtain().set(0.1f + pathPoints.size * animSpeed, path, Interpolation.exp5In)
					orb.sprite.renderDelay = orb.spawnCount * 0.1f
					orb.spawnCount = -1

					if (firstIsNull)
					{
						orb.sprite.spriteAnimation = ExpandAnimation.obtain().set(animSpeed)
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
	fun update(delta: Float): Boolean
	{
		var done = true

		// if in update, do animations
		cleanup()

		if (!hasAnim())
		{
			for (f in updateFuns)
			{
				val complete = f()
				if (!complete)
				{
					done = false
					return done
				}
			}

			if (!level.completed && FullscreenMessage.instance == null)
			{
				if (activeAbility == null) matchHint = findValidMove()
				if (activeAbility == null && matchHint == null)
				{
					FullscreenMessage("No valid moves. Randomising.", "", { refill() }).show()
				}
				else
				{
					if (activeAbility == null) noMatchTimer += delta

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
		else
		{
			done = false
		}

		return done
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

		// check for merges
		if (newOrb.special != null || oldOrb.special != null)
		{
			val armfun = newOrb.special?.merge(oldOrb) ?: oldOrb.special?.merge(newOrb)
			if (armfun != null)
			{
				val sprite = oldOrb.sprite.copy()
				sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(newTile.getPosDiff(oldTile)), Interpolation.linear)
				newTile.effects.add(sprite)

				onPop(oldOrb, 0f)
				oldTile.orb = null

				newOrb.armed = armfun
				newOrb.markedForDeletion = true

				return false
			}
		}

		oldTile.orb = newOrb
		newTile.orb = oldOrb

		val matches = findMatches()
		if (matches.size == 0)
		{
			oldTile.orb = oldOrb
			newTile.orb = newOrb

			oldOrb.sprite.spriteAnimation = BumpAnimation.obtain().set(animSpeed, Direction.Companion.getDirection(oldTile, newTile))
			return false
		}
		else
		{
			lastSwapped = newTile

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

					if (orb.markedForDeletion && orb.sprite.spriteAnimation == null && orb.armed == null)
					{
						tile.orb = null
						onPop(orb, orb.deletionEffectDelay)

						if (orb.deletionEffectDelay >= 0.2f)
						{
							val sprite = orb.sprite.copy()
							sprite.renderDelay = orb.deletionEffectDelay - 0.2f
							sprite.showBeforeRender = true
							sprite.spriteAnimation = AlphaAnimation.obtain().set(floatArrayOf(1f, 0f), sprite.colour, 0.2f)
							tile.effects.add(sprite)
						}
					}
					else if (orb.hasAttack && orb.attackTimer == 0 && orb.sprite.spriteAnimation == null)
					{
						onAttacked(orb)
						orb.hasAttack = false
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
				else if (tile.monster != null)
				{
					val monster = tile.monster!!
					if (monster.hp <= 0)
					{
						for (t in monster.tiles)
						{
							t.monster = null
						}
					}
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun match(): Boolean
	{
		val matches = findMatches(3)
		clearMatches(matches)

		lastSwapped = Point.MINUS_ONE

		return matches.size == 0
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
			val dir = match.direction()
			val key = grid[match.p1].orb!!.key

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
			val beforeFirst = match.p1 + dir.opposite
			val beforeFirstPair = checkSurrounding(beforeFirst, dir.opposite, key)
			if (beforeFirstPair != null) return beforeFirstPair

			val afterSecond = match.p2 + dir
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

				if (orb.armed != null)
				{
					tilesToDetonate.add(tile)
				}
			}
		}

		for (tile in tilesToDetonate)
		{
			tile.orb!!.armed!!.invoke(tile, this)

			tile.orb!!.armed = null
			complete = false
		}

		return complete
	}

	// ----------------------------------------------------------------------
	fun sink(): Boolean
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
				if (oldorb == null || oldorb.sinkable) grid[x, y].contents = tempgrid[x, y].contents
				else
				{
					val orb = grid[x, y].orb!!

					if (oldorb.special != null) orb.special = oldorb.special!!.copy(orb)
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
				if (grid[x, y].canHaveOrb && grid[x, y].block == null && grid[x, y].monster == null)
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
	fun findMatches() : Array<Match>
	{
		val matches = Array<Match>(false, 16)

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
	fun findMatches(length: Int, exact: Boolean = false) : Array<Match>
	{
		val matches = Array<Match>()

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
				matches.add(Match(p1, p2))
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
	fun clearMatches(matches: Array<Match>)
	{
		// mark all matched tiles with the matches associated with them
		for (tile in grid)
		{
			tile.associatedMatches[0] = null
			tile.associatedMatches[1] = null
		}

		for (match in matches)
		{
			for (point in match.points())
			{
				val tile = grid[point]
				if (tile.associatedMatches[0] == null)
				{
					tile.associatedMatches[0] = match
				}
				else
				{
					tile.associatedMatches[1] = match
				}
			}
		}

		val coreTiles = Array<Tile>()
		val borderTiles = ObjectSet<Tile>()

		// remove all orbs, activate all specials
		for (match in matches)
		{
			coreTiles.clear()
			borderTiles.clear()

			for (point in match.points())
			{
				coreTiles.add(grid[point])
				pop(point.x, point.y, 0f)
			}

			for (tile in coreTiles)
			{
				for (d in Direction.CardinalValues)
				{
					val t = tile(tile.x + d.x, tile.y + d.y) ?: continue
					if (!coreTiles.contains(t, true))
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
				if (t.monster != null)
				{
					t.monster!!.hp--
					onDamaged(t.monster!!)
				}
			}
		}

		// for each tile with 2 matches spawn the relevant special, and mark the matches as used, if cross point is used them spawn in a neighbouring tile that isnt specialed
		for (tile in grid)
		{
			if (tile.associatedMatches[0] != null && tile.associatedMatches[1] != null)
			{
				val orb = Orb(tile.orb!!.desc)
				val special = getSpecial(tile.associatedMatches[0]!!.length(), tile.associatedMatches[1]!!.length(), Direction.CENTRE, orb) ?: continue
				orb.special = special

				if (tile.orb != null)
				{
					tile.orb!!.x = tile.x
					tile.orb!!.y = tile.y
					onPop(tile.orb!!, 0f)
				}

				tile.orb = orb

				tile.associatedMatches[0]!!.used = true
				tile.associatedMatches[1]!!.used = true

				for (point in tile.associatedMatches[0]!!.points())
				{
					val sprite = orb.sprite.copy()
					sprite.drawActualSize = false
					sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(tile.getPosDiff(point)), Interpolation.linear)

					tile.effects.add(sprite)
				}

				for (point in tile.associatedMatches[1]!!.points())
				{
					val sprite = orb.sprite.copy()
					sprite.drawActualSize = false
					sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(tile.getPosDiff(point)), Interpolation.linear)

					tile.effects.add(sprite)
				}
			}
		}

		// for each unused match spawn the relevant special at the player swap pos, else at the center, else at a random unspecialed tile
		for (match in matches)
		{
			if (!match.used && match.length() > 3)
			{
				val tile = grid[(match.p1 + (match.p2 - match.p1)/2)]

				val orb = Orb(tile.orb!!.desc)
				val special = getSpecial(match.length(), 0, match.direction(), orb) ?: continue
				orb.special = special

				if (tile.orb != null)
				{
					tile.orb!!.x = tile.x
					tile.orb!!.y = tile.y
					onPop(tile.orb!!, 0f)
				}

				tile.orb = orb

				for (point in match.points())
				{
					val sprite = orb.sprite.copy()
					sprite.drawActualSize = false
					sprite.spriteAnimation = MoveAnimation.obtain().set(animSpeed, UnsmoothedPath(tile.getPosDiff(point)), Interpolation.linear)

					tile.effects.add(sprite)
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun pop(point: Point, delay: Float)
	{
		pop(point.x, point.y , delay)
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

		if (tile.monster != null)
		{
			tile.monster!!.hp--
			onDamaged(tile.monster!!)
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
		orb.deletionEffectDelay = delay

		if (orb.special == null)
		{
			orb.sprite.visible = false

			val sprite = orb.desc.death.copy()
			sprite.colour = orb.sprite.colour
			sprite.renderDelay = delay

			tile.effects.add(sprite)
		}
		else if (orb.special is Match5)
		{
			orb.markedForDeletion = false
		}
		else
		{
			orb.armed = orb.special!!.apply()
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
}

data class Match(val p1: Point, val p2: Point, var used: Boolean = false)
{
	fun length() = p1.dist(p2) + 1
	fun points() = p1.rangeTo(p2)
	fun direction() = Direction.getDirection(p1, p2)
}