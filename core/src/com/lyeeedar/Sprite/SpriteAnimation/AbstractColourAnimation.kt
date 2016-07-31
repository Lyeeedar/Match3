package com.lyeeedar.Sprite.SpriteAnimation

/**
 * Created by Philip on 31-Jul-16.
 */

abstract class AbstractColourAnimation() : AbstractSpriteAnimation()
{
	var oneTime = true

	override fun renderScale(): FloatArray? = null
	override fun renderOffset(): FloatArray? = null
}