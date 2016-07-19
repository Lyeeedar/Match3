package com.lyeeedar.Util

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.IntIntMap
import com.badlogic.gdx.utils.IntMap
import java.util.*

/**
 * Created by Philip on 04-Jul-16.
 */

fun <T> com.badlogic.gdx.utils.Array<T>.ran(ran: Random): T = this[ran.nextInt(this.size)]
fun <T> com.badlogic.gdx.utils.Array<T>.removeRan(ran: Random): T
{
	val index = ran.nextInt(this.size)
	val item = this[index]
	this.removeIndex(index)

	return item
}

fun Float.abs() = Math.abs(this)
fun Float.ciel() = MathUtils.ceil(this)
fun Float.floor() = MathUtils.floor(this)

fun String.neaten() = this.substring(0, 1).toUpperCase() + this.substring(1).toLowerCase()

operator fun IntIntMap.get(key: Int) = this.get(key, 0)
operator fun IntIntMap.set(key: Int, value: Int) = this.put(key, value)