package com.lyeeedar.Util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.*
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

inline fun <reified T : Any> getPool(): Pool<T> = Pools.get(T::class.java, Int.MAX_VALUE)

fun <T> com.badlogic.gdx.utils.Array<T>.random(ran: Random): T = this[ran.nextInt(this.size)]
fun <T> com.badlogic.gdx.utils.Array<T>.removeRandom(ran: Random): T
{
	val index = ran.nextInt(this.size)
	val item = this[index]
	this.removeIndex(index)

	return item
}

fun getRotation(p1: Point, p2: Point) : Float
{
	val vec = Pools.obtain(Vector2::class.java)
	vec.x = (p2.x - p1.x).toFloat()
	vec.y = (p2.y - p1.y).toFloat()
	vec.nor()
	val x = vec.x
	val y = vec.y
	val dot = (0 * x + 1 * y).toDouble() // dot product
	val det = (0 * y - 1 * x).toDouble() // determinant
	val angle = Math.atan2(det, dot).toFloat() * MathUtils.radiansToDegrees
	Pools.free(vec)

	return angle
}

fun error(message: String) { System.err.println(message) }

fun Float.abs() = Math.abs(this)
fun Float.ciel() = MathUtils.ceil(this)
fun Float.floor() = MathUtils.floor(this)

fun String.neaten() = this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()

operator fun IntIntMap.get(key: Int) = this.get(key, 0)
operator fun IntIntMap.set(key: Int, value: Int) = this.put(key, value)
operator fun <K, V> ObjectMap<K, V>.set(key: K, value: V) = this.put(key, value)

fun XmlReader.Element.ranChild() = this.getChild(MathUtils.random(this.childCount-1))!!

fun <T> Sequence<T>.random() = if (this.count() > 0) this.elementAt(MathUtils.random(this.count()-1)) else null
fun <T> Sequence<T>.random(ran: Random) = if (this.count() > 0) this.elementAt(ran.nextInt(this.count())) else null

fun Color.toHSV(out: FloatArray? = null): FloatArray
{
	val max = Math.max(this.r, Math.max(this.g, this.b))
	val min = Math.min(this.r, Math.min(this.g, this.b))
	val delta = max - min

	val saturation = if (delta == 0f) 0f else delta / max
	val hue = if (this.r == max) ((this.g - this.b) / delta) % 6
				else if (this.g == max) 2 + (this.b - this.r) / delta
					else 4 + (this.r - this.g) / delta
	val value = max

	val output = if (out != null && out.size >= 3) out else kotlin.FloatArray(3)
	output[0] = (hue * 60f) / 360f
	output[1] = saturation
	output[2] = value

	return output
}