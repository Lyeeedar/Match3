package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.graphics.Color

/**
 * Created by Philip on 31-Jul-16.
 */

abstract class AbstractMoveAnimation() : AbstractSpriteAnimation()
{
	override fun renderScale(): FloatArray? = null
	override fun renderColour(): Color? = null
}