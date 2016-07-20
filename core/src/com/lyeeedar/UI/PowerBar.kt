package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.lyeeedar.Util.*

/**
 * Created by Philip on 19-Jul-16.
 */

class PowerBar() : Widget()
{
	init
	{
		instance = this
	}

	val powerChanged = Event0Arg()

	val blank = AssetManager.loadTextureRegion("white")

	val pipPadding = 5f

	val powerPerPip = 10
	var maxPower = 100

	val pipWidth: Float
		get()
		{
			val numPips = (maxPower / powerPerPip).toFloat()
			val widthWithoutPadding = width - numPips * pipPadding
			return widthWithoutPadding / numPips
		}

	var tempPower = 0

	val pips: Int
		get() = (power.toFloat() / powerPerPip.toFloat()).toInt()

	var power = 0
		set(value)
		{
			tempPower--

			if (value < maxPower)
			{
				field = value
			}
			else
			{
				field = maxPower
			}

			powerChanged()
		}

	fun getOrbDest(): Vector2?
	{
		if (power+tempPower >= maxPower)
		{
			return null
		}

		val dy = 0f

		val destPipVal = ((power + tempPower).toFloat() / powerPerPip.toFloat()).toInt()

		val pw = pipWidth

		val dx = destPipVal * pipPadding + destPipVal * pw

		tempPower++

		return localToStageCoordinates(Vector2(dx, dy))
	}

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		if (batch == null) return

		val numPips = (maxPower.toFloat() / powerPerPip.toFloat()).toInt()
		val pw = pipWidth

		var powerCounter = 0

		for (i in 0..numPips-1)
		{
			val powerDiff = power - powerCounter

			if (powerDiff > powerPerPip)
			{
				batch.color = Color.CYAN
			}
			else if (powerDiff < 0)
			{
				batch.color = Color.DARK_GRAY
			}
			else
			{
				batch.color = Color(Color.DARK_GRAY).lerp(Color.CYAN, powerDiff.toFloat() / powerPerPip.toFloat())
			}

			batch.draw(blank, x + pw * i + pipPadding * i, y, pw, height)

			powerCounter += powerPerPip
		}
	}

	companion object
	{
		lateinit var instance: PowerBar
	}
}