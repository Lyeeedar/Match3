package com.lyeeedar.Renderables

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.HDRColourSpriteBatch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.*
import com.badlogic.gdx.utils.Array
import com.lyeeedar.BlendMode
import com.lyeeedar.Direction
import com.lyeeedar.Global
import com.lyeeedar.Renderables.Particle.ParticleEffect
import com.lyeeedar.Renderables.Particle.Emitter
import com.lyeeedar.Renderables.Particle.Particle
import com.lyeeedar.Renderables.Particle.ParticleData
import com.lyeeedar.Renderables.Sprite.TilingSprite
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Util.*
import java.util.*
import ktx.collections.get
import ktx.collections.set

/**
 * Created by Philip on 04-Jul-16.
 */

class SortedRenderer(var tileSize: Float, val width: Float, val height: Float, val layers: Int)
{
	var batchID: Int = 0

	val tempVec = Vector2()
	val tempPoint = Point()
	val tempCol = Colour()
	val bitflag = EnumBitflag<Direction>()
	val heap: BinaryHeap<RenderSprite> = BinaryHeap()
	var tilingMap: ObjectMap<Point, ObjectSet<Long>> = ObjectMap()

	val setPool: Pool<ObjectSet<Long>> = getPool()

	var screenShakeRadius: Float = 0f
	var screenShakeAccumulator: Float = 0f
	var screenShakeSpeed: Float = 0f
	var screenShakeAngle: Float = 0f

	val BLENDMODES = BlendMode.values().size
	val MAX_INDEX = 3 * BLENDMODES
	val X_BLOCK_SIZE = layers * MAX_INDEX
	val Y_BLOCK_SIZE = X_BLOCK_SIZE * width
	val MAX_Y_BLOCK_SIZE = Y_BLOCK_SIZE * height
	val MAX_X_BLOCK_SIZE = X_BLOCK_SIZE * width

	var delta: Float = 0f

	var debugDrawSpeed = 1.0f
	var debugDrawAccumulator = 0.0f
	var debugDraw = false
	var inDebugFrame = false
	var debugDrawList = Array<RenderSprite>()

	var inBegin = false
	var offsetx: Float = 0f
	var offsety: Float = 0f

	// ----------------------------------------------------------------------
	fun begin(deltaTime: Float, offsetx: Float, offsety: Float)
	{
		if (inBegin) throw Exception("Begin called again before flush!")

		delta = deltaTime
		this.offsetx = offsetx
		this.offsety = offsety
		inBegin = true
	}

	// ----------------------------------------------------------------------
	fun setScreenShake(amount: Float)
	{
		screenShakeRadius = amount
	}

	// ----------------------------------------------------------------------
	fun flush(batch: Batch)
	{
		if (!inBegin) throw Exception("Flush called before begin!")

		// do screen shake
		if ( screenShakeRadius > 2 )
		{
			screenShakeAccumulator += delta
			while ( screenShakeAccumulator >= screenShakeSpeed )
			{
				screenShakeAccumulator -= screenShakeSpeed
				screenShakeAngle += ( 150 + MathUtils.random() * 60 )
				screenShakeRadius *= 0.9f
			}

			offsetx += Math.sin( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius
			offsety += Math.cos( screenShakeAngle.toDouble() ).toFloat() * screenShakeRadius
		}

		fun draw(rs: RenderSprite)
		{
			val localx = rs.x + offsetx
			val localy = rs.y + offsety
			val localw = rs.width * tileSize
			val localh = rs.height * tileSize

			batch.setBlendFunction(rs.blend.src, rs.blend.dst)

			if (batch is HDRColourSpriteBatch) batch.setColor(rs.colour)
			else batch.setColor(rs.colour.toFloatBits())

			rs.sprite?.render(batch, localx, localy, localw, localh, rs.scaleX, rs.scaleY )

			if (rs.tilingSprite != null)
			{
				bitflag.clear()
				for (dir in Direction.Values)
				{
					tempPoint.set(rs.point).plusAssign(dir)
					val keys = tilingMap[tempPoint]

					if (!(keys?.contains(rs.tilingSprite!!.checkID) ?: false))
					{
						bitflag.setBit(dir)
					}
				}

				val sprite = rs.tilingSprite!!.getSprite(bitflag)
				sprite.render(batch, localx, localy, localw, localh, rs.scaleX, rs.scaleY )
			}

			if (rs.texture != null)
			{
				if (batch is HDRColourSpriteBatch)
				{
					batch.draw(rs.texture, localx, localy, 0.5f, 0.5f, 1f, 1f, localw * rs.scaleX, localh * rs.scaleY, rs.rotation, rs.flipX, rs.flipY)
				}
				else
				{
					batch.draw(rs.texture, localx, localy, 0.5f, 0.5f, 1f, 1f, localw * rs.scaleX, localh * rs.scaleY, rs.rotation)
				}
			}
		}

		if (debugDraw)
		{
			for (rs in debugDrawList)
			{
				draw(rs)
			}

			val drawSpeed = if (debugDrawSpeed < 0) -1.0f / debugDrawSpeed else debugDrawSpeed

			debugDrawAccumulator += drawSpeed

			while (heap.size > 0 && debugDrawAccumulator > 1.0f)
			{
				if (debugDraw) inDebugFrame = true

				val rs = heap.pop()

				draw(rs)

				debugDrawList.add(rs)

				debugDrawAccumulator -= 1.0f
			}

			if (heap.size == 0)
			{
				debugDrawAccumulator = 0.0f
			}
		}
		else
		{
			while (heap.size > 0)
			{
				val rs = heap.pop()

				draw(rs)

				rs.free()
			}
		}

		if (!debugDraw || heap.size == 0)
		{
			inDebugFrame = false

			for (rs in debugDrawList) rs.free()
			debugDrawList.clear()

			batchID = random.nextInt()

			for (entry in tilingMap)
			{
				entry.key.free()
				setPool.free(entry.value)
			}
			tilingMap.clear()
		}

		inBegin = false

		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
	}

	// ----------------------------------------------------------------------
	fun getComparisonVal(x: Int, y: Int, layer: Int, index: Int, blend: BlendMode) : Float
	{
		if (index > MAX_INDEX-1) throw RuntimeException("Index too high! $index >= $MAX_INDEX!")
		if (layer > layers-1) throw RuntimeException("Layer too high! $index >= $layers!")

		val yBlock = MAX_Y_BLOCK_SIZE - y * Y_BLOCK_SIZE
		val xBlock = (MAX_X_BLOCK_SIZE - x * X_BLOCK_SIZE)
		val lBlock = layer * MAX_INDEX
		val iBlock = index * BLENDMODES

		return yBlock + xBlock + lBlock + iBlock + blend.ordinal
	}

	// ----------------------------------------------------------------------
	fun queueParticle(effect: ParticleEffect, ix: Float, iy: Float, layer: Int, index: Int, colour: Colour = Colour.WHITE, width: Float = 1f, height: Float = 1f)
	{
		if (!inBegin) throw Exception("Queue called before begin!")

		if (debugDraw && inDebugFrame) return

		if (effect.batchID != batchID) effect.update(delta)
		effect.batchID = batchID

		if (!effect.visible) return
		if (effect.renderDelay > 0 && !effect.showBeforeRender)
		{
			return
		}

		val x = ix
		val y = iy

		//val scale = effect.animation?.renderScale()?.get(0) ?: 1f
		val animCol = effect.animation?.renderColour() ?: Colour.WHITE

		for (emitter in effect.emitters)
		{
			for (particle in emitter.particles)
			{
				var offsetx = x
				var offsety = y

				if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL)
				{
					tempVec.set(emitter.offset.valAt(0, emitter.time))
					tempVec.scl(emitter.size)
					tempVec.rotate(emitter.rotation)

					offsetx += (emitter.position.x + tempVec.x)
					offsety += (emitter.position.y + tempVec.y)
				}

				for (pdata in particle.particles)
				{
					val tex = particle.texture.valAt(pdata.texStream, pdata.life)
					val col = tempCol.set(particle.colour.valAt(pdata.colStream, pdata.life))
					col.a = particle.alpha.valAt(pdata.alphaStream, pdata.life)
					val size = particle.size.valAt(pdata.sizeStream, pdata.life).lerp(pdata.ranVal)
					var sizex = size * width
					var sizey = size * height

					if (particle.allowResize)
					{
						sizex *= emitter.size.x
						sizey *= emitter.size.y
					}

					val rotation = if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL) pdata.rotation + emitter.rotation + emitter.emitterRotation else pdata.rotation

					col.mul(colour).mul(animCol).mul(effect.colour)

					tempVec.set(pdata.position)

					if (emitter.simulationSpace == Emitter.SimulationSpace.LOCAL) tempVec.scl(emitter.size).rotate(emitter.rotation + emitter.emitterRotation)

					val drawx = tempVec.x  + offsetx
					val drawy = tempVec.y + offsety

					val localx = drawx * tileSize + offsetx
					val localy = drawy * tileSize + offsety
					val localw = sizex * tileSize
					val localh = sizey * tileSize

					if (localx + localw < 0 || localx > Global.stage.width || localy + localh < 0 || localy > Global.stage.height) continue

					val comparisonVal = getComparisonVal((drawx-sizex*0.5f).toInt(), (drawy-sizey*0.5f).toInt(), layer, index, particle.blend)

					val rs = RenderSprite.obtain().set( null, null, tex, drawx * tileSize, drawy * tileSize, tempVec.x, tempVec.y, col, sizex, sizey, rotation, 1f, 1f, effect.flipX, effect.flipY, particle.blend, comparisonVal )

					heap.add( rs, rs.comparisonVal )
				}
			}
		}
	}

	// ----------------------------------------------------------------------
	fun queueSprite(tilingSprite: TilingSprite, ix: Float, iy: Float, layer: Int, index: Int, colour: Colour = Colour.WHITE, width: Float = 1f, height: Float = 1f)
	{
		if (!inBegin) throw Exception("Queue called before begin!")

		if (debugDraw && inDebugFrame) return

		if (tilingSprite.batchID != batchID) tilingSprite.update(delta)
		tilingSprite.batchID = batchID

		if (!tilingSprite.visible) return
		if (tilingSprite.renderDelay > 0 && !tilingSprite.showBeforeRender)
		{
			return
		}

		var lx = ix
		var ly = iy

		var x = ix * tileSize
		var y = iy * tileSize

		if ( tilingSprite.animation != null )
		{
			val offset = tilingSprite.animation?.renderOffset()

			if (offset != null)
			{
				x += offset[0] * tileSize
				y += offset[1] * tileSize

				lx += offset[0]
				ly += offset[1]
			}
		}

		// Add to map
		val point = Point.obtain().set(ix.toInt(), iy.toInt())
		var keys = tilingMap[point]
		if (keys == null)
		{
			keys = setPool.obtain()
			keys.clear()
		}
		keys.add(tilingSprite.checkID)
		tilingMap[point] = keys

		// check if onscreen
		if (!isSpriteOnscreen(tilingSprite, x, y, width, height)) return

		val comparisonVal = getComparisonVal(lx.toInt(), ly.toInt(), layer, index, BlendMode.MULTIPLICATIVE)

		val rs = RenderSprite.obtain().set( null, tilingSprite, null, x, y, ix, iy, colour, width, height, 0f, 1f, 1f, false, false, BlendMode.MULTIPLICATIVE, comparisonVal )

		heap.add( rs, rs.comparisonVal )
	}

	// ----------------------------------------------------------------------
	fun queueSprite(sprite: Sprite, ix: Float, iy: Float, layer: Int, index: Int, colour: Colour = Colour.WHITE, update: Boolean = true, width: Float = 1f, height: Float = 1f, scaleX: Float = 1f, scaleY: Float = 1f)
	{
		if (!inBegin) throw Exception("Queue called before begin!")

		if (debugDraw && inDebugFrame) return

		if (update)
		{
			if (sprite.batchID != batchID) sprite.update(delta)
		}
		sprite.batchID = batchID

		if (!sprite.visible) return
		if (sprite.renderDelay > 0 && !sprite.showBeforeRender)
		{
			return
		}

		var lx = ix
		var ly = iy

		var x = ix * tileSize
		var y = iy * tileSize

		if ( sprite.animation != null )
		{
			val offset = sprite.animation?.renderOffset()

			if (offset != null)
			{
				x += offset[0] * tileSize
				y += offset[1] * tileSize

				lx += offset[0]
				ly += offset[1]
			}
		}

		if (sprite.faceInMoveDirection)
		{
			val angle = getRotation(sprite.lastPos, tempVec.set(x, y))
			sprite.rotation = angle
			sprite.lastPos.set(x, y)
		}

		// check if onscreen
		if (!isSpriteOnscreen(sprite, x, y, width, height)) return

		val comparisonVal = getComparisonVal(lx.toInt(), ly.toInt(), layer, index, BlendMode.MULTIPLICATIVE)

		val rs = RenderSprite.obtain().set( sprite, null, null, x, y, ix, iy, colour, width, height, 0f, scaleX, scaleY, false, false, BlendMode.MULTIPLICATIVE, comparisonVal )

		heap.add( rs, rs.comparisonVal )
	}

	// ----------------------------------------------------------------------
	fun isSpriteOnscreen(sprite: Sprite, x: Float, y: Float, width: Float, height: Float): Boolean
	{
		var localx = x + offsetx
		var localy = y + offsety
		var localw = width * tileSize * sprite.size[0]
		var localh = height * tileSize * sprite.size[1]

		var scaleX = sprite.baseScale[0]
		var scaleY = sprite.baseScale[1]

		if (sprite.animation != null)
		{
			val scale = sprite.animation!!.renderScale()
			if (scale != null)
			{
				scaleX *= scale[0]
				scaleY *= scale[1]
			}
		}

		if (sprite.drawActualSize)
		{
			val texture = sprite.textures.items[sprite.animationState.texIndex]

			val widthRatio = width / 32f
			val heightRatio = height / 32f

			val regionWidth = sprite.referenceSize ?: texture.regionWidth.toFloat()
			val regionHeight = sprite.referenceSize ?: texture.regionHeight.toFloat()

			val trueWidth = regionWidth * widthRatio
			val trueHeight = regionHeight * heightRatio

			val widthOffset = (trueWidth - width) / 2

			localx -= widthOffset
			localw = trueWidth
			localh = trueHeight
		}

		if (sprite.rotation != 0f && sprite.fixPosition)
		{
			val offset = Sprite.getPositionCorrectionOffsets(x, y, width / 2.0f, height / 2.0f, width, height, scaleX, scaleY, sprite.rotation)
			localx -= offset.x
			localy -= offset.y
		}

		if (scaleX != 1f)
		{
			val newW = localw * scaleX
			val diff = newW - localw

			localx -= diff * 0.5f
			localw = newW
		}
		if (scaleY != 1f)
		{
			val newH = localh * scaleY
			val diff = newH - localh

			localy -= diff * 0.5f
			localh = newH
		}

		if (localx + localw < 0 || localx > Global.stage.width || localy + localh < 0 || localy > Global.stage.height) return false

		return true
	}

	// ----------------------------------------------------------------------
	fun isSpriteOnscreen(sprite: TilingSprite, x: Float, y: Float, width: Float, height: Float): Boolean
	{
		val localx = x + offsetx
		val localy = y + offsety
		val localw = width * tileSize
		val localh = height * tileSize

		if (localx + localw < 0 || localx > Global.stage.width || localy + localh < 0 || localy > Global.stage.height) return false

		return true
	}

	// ----------------------------------------------------------------------
	companion object
	{
		val random = Random()
	}
}

// ----------------------------------------------------------------------
class RenderSprite : BinaryHeap.Node(0f)
{
	val point = Point()
	val colour: Colour = Colour(1f, 1f, 1f, 1f)
	var sprite: Sprite? = null
	var tilingSprite: TilingSprite? = null
	var texture: TextureRegion? = null
	var x: Float = 0f
	var y: Float = 0f
	var width: Float = 1f
	var height: Float = 1f
	var rotation: Float = 0f
	var scaleX: Float = 1f
	var scaleY: Float = 1f
	var flipX: Boolean = false
	var flipY: Boolean = false
	var blend: BlendMode = BlendMode.MULTIPLICATIVE

	var comparisonVal: Float = 0f

	// ----------------------------------------------------------------------
	operator fun set(sprite: Sprite?, tilingSprite: TilingSprite?, texture: TextureRegion?,
					 x: Float, y: Float,
					 ix: Float, iy: Float,
					 colour: Colour,
					 width: Float, height: Float,
					 rotation: Float,
					 scaleX: Float, scaleY: Float,
					 flipX: Boolean, flipY: Boolean,
					 blend: BlendMode,
					 comparisonVal: Float): RenderSprite
	{
		this.point.x = ix.toInt()
		this.point.y = iy.toInt()
		this.colour.r = colour.r
		this.colour.g = colour.g
		this.colour.b = colour.b
		this.colour.a = colour.a
		this.sprite = sprite
		this.tilingSprite = tilingSprite
		this.texture = texture
		this.x = x
		this.y = y
		this.width = width
		this.height = height
		this.comparisonVal = comparisonVal
		this.blend = blend
		this.rotation = rotation
		this.scaleX = scaleX
		this.scaleY = scaleY
		this.flipX = flipX
		this.flipY = flipY

		return this
	}

	// ----------------------------------------------------------------------
	fun free() = pool.free(this)

	// ----------------------------------------------------------------------
	companion object
	{
		val pool: Pool<RenderSprite> = Pools.get( RenderSprite::class.java, Int.MAX_VALUE )
		fun obtain() = pool.obtain()
	}
}
