package com.lyeeedar.Renderables.Animation

import com.badlogic.gdx.graphics.Color

/**
 * Created by Philip on 31-Jul-16.
 */

abstract class AbstractScaleAnimation() : AbstractAnimation()
{
	override fun renderOffset(): FloatArray? = null
	override fun renderColour(): Color? = null
}