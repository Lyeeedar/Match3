package com.lyeeedar.Sprite.SpriteAnimation

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.XmlReader
import java.util.HashMap

import com.badlogic.gdx.utils.XmlReader.Element
import com.badlogic.gdx.utils.reflect.ClassReflection
import com.badlogic.gdx.utils.reflect.ReflectionException

abstract class AbstractSpriteAnimation
{
	abstract fun renderOffset(): FloatArray?
	abstract fun renderScale(): FloatArray?
	abstract fun renderColour(): Color?

	abstract fun duration(): Float
	abstract fun time(): Float
	abstract fun update(delta: Float): Boolean
	abstract fun parse(xml: Element)

	abstract fun free()

	abstract fun copy(): AbstractSpriteAnimation

	companion object
	{
		fun load(xml: XmlReader.Element): AbstractSpriteAnimation
		{
			val uname = xml.name.toUpperCase()
			val c = getClass(uname)
			val instance = ClassReflection.getConstructor(c).newInstance() as AbstractSpriteAnimation

			instance.parse(xml)

			return instance
		}

		fun getClass(name: String): Class<out AbstractSpriteAnimation>
		{
			val type = when(name) {
				"MOVE" -> MoveAnimation::class.java
				"BOUNCE" -> BumpAnimation::class.java
				"BLINK" -> BlinkAnimation::class.java

			// ARGH everything broke
				else -> throw RuntimeException("Invalid sprite animation type: $name")
			}

			return type
		}
	}
}
