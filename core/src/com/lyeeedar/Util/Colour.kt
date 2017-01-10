package com.lyeeedar.Util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.NumberUtils

/**
 * Created by Philip on 30-Mar-16.
 */

class Colour()
{
	@JvmField var r: Float = 0f
	@JvmField var g: Float = 0f
	@JvmField var b: Float = 0f
	@JvmField var a: Float = 0f

	constructor(col: Float) : this()
	{
		set(col)
	}

	constructor(col: Color) : this()
	{
		set(col.r, col.g, col.b, col.a)
	}

	constructor(r: Float, g:Float, b:Float, a:Float) : this()
	{
		set(r, g, b, a)
	}

	fun set(other: Colour): Colour
	{
		r = other.r
		g = other.g
		b = other.b
		a = other.a

		return this
	}

	fun set(col: Float): Colour
	{
		r = col
		g = col
		b = col
		a = col

		return this
	}

	fun set(col: Color, packed: Float? = null): Colour
	{
		r = col.r
		g = col.g
		b = col.b
		a = col.a

		if (packed != null)
		{
			cachedR = r
			cachedG = g
			cachedB = b
			cachedA = a
			cachedFB = packed
		}

		return this
	}

	fun set(r: Float, g:Float, b:Float, a:Float): Colour
	{
		this.r = r
		this.g = g
		this.b = b
		this.a = a

		return this
	}

	fun mul(other: Colour) : Colour
	{
		timesAssign(other)
		return this
	}

	fun mul(r: Float, g: Float, b: Float, a: Float): Colour
	{
		this.r *= r
		this.g *= g
		this.b *= b
		this.a *= a
		return this
	}

	operator fun timesAssign(other: Colour)
	{
		r *= other.r
		g *= other.g
		b *= other.b
		a *= other.a
	}

	operator fun timesAssign(other: Color)
	{
		r *= other.r
		g *= other.g
		b *= other.b
		a *= other.a
	}

	operator fun timesAssign(alpha: Float)
	{
		r *= alpha
		g *= alpha
		b *= alpha
		a *= alpha
	}

	operator fun plusAssign(other: Colour)
	{
		r += other.r;
		g += other.g;
		b += other.b;
		a += other.a;
	}

	operator fun divAssign(value: Float)
	{
		r /= value
		g /= value
		b /= value
		a /= value
	}

	fun lerp(target: Colour, t: Float) : Colour
	{
		this.r += t * (target.r - this.r)
		this.g += t * (target.g - this.g)
		this.b += t * (target.b - this.b)
		this.a += t * (target.a - this.a)

		return this
	}

	fun toFloatBits() : Float
	{
		if (cachedR == r && cachedG == g && cachedB == b && cachedA == a) return cachedFB
		else
		{
			val intBits = (255 * a).toInt() shl 24 or ((255 * b).toInt() shl 16) or ((255 * g).toInt() shl 8) or (255 * r).toInt()
			cachedFB = NumberUtils.intToFloatColor(intBits)

			cachedR = r
			cachedB = b
			cachedG = g
			cachedA = a

			return cachedFB
		}
	}
	var cachedR: Float = -1f
	var cachedG: Float = -1f
	var cachedB: Float = -1f
	var cachedA: Float = -1f
	var cachedFB: Float = -1f

	fun color() : Color
	{
		return Color(r, g, b, a)
	}

	companion object
	{
		val WHITE = Colour(Color.WHITE)
		val LIGHT_GRAY = Colour(Color.LIGHT_GRAY)
		val DARK_GRAY = Colour(Color.DARK_GRAY)
		val GOLD = Colour(Color.GOLD)
	}
}