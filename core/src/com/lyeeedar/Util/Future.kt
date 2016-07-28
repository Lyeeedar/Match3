package com.lyeeedar.Util

import com.badlogic.gdx.utils.Array

/**
 * Created by Philip on 28-Jul-16.
 */

class Future
{
	companion object
	{
		private val pendingCalls = Array<CallData>(false, 8)

		fun update(delta: Float)
		{
			val itr = pendingCalls.iterator()
			while (itr.hasNext())
			{
				val item = itr.next()
				item.delay -= delta

				if (item.delay <= 0f)
				{
					item.function.invoke()
					itr.remove()
				}
			}
		}

		fun call(function: () -> Unit, delay: Float, token: Any? = null)
		{
			if (token != null)
			{
				if (pendingCalls.firstOrNull { it.token == token } != null) return
			}

			pendingCalls.add(CallData(function, delay, token))
		}
	}
}

data class CallData(val function: () -> Unit, var delay: Float, val token: Any?)