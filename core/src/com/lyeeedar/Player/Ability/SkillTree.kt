package com.lyeeedar.Player.Ability

import com.badlogic.gdx.math.Vector2
import com.lyeeedar.Sprite.Sprite

/**
 * Created by Philip on 05-Aug-16.
 */

val SkillTreeRadiusStep = 100f
private val tempVec = Vector2()

class SkillTree()
{
	val numBaseSkills = 5

	lateinit var baseIcon: Sprite

	val rootSkills: Array<Skill?> = Array(numBaseSkills){ e -> null }

	fun assignLocations()
	{
		// top 90 degrees is for the buttons
		val radiansRemaining = Math.PI.toFloat() * 2f - Math.toRadians(90.0).toFloat()
		val radiansStart = Math.toRadians(45.0).toFloat()

		// split rest between skills
		val radiansPerSkill = radiansRemaining / numBaseSkills

		for (i in 0..numBaseSkills-1)
		{
			val start = radiansStart + i * radiansPerSkill
			rootSkills[i]?.assignLocation(radiansPerSkill, start, SkillTreeRadiusStep)
		}
	}
}

class Skill(val ability: Ability)
{
	val location: Vector2 = Vector2()
	val children: Array<Skill?> = Array(2){ e -> null }

	fun assignLocation(angle: Float, start: Float, radius: Float)
	{
		val drawAngle = start + angle * 0.5f

		tempVec.set(0f, 1f)
		tempVec.rotateRad(drawAngle)
		tempVec.scl(radius)

		location.set(tempVec)

		if (children[0] != null && children[1] != null)
		{
			// split into 2
			val childAngle = angle * 0.5f

			children[0]?.assignLocation(childAngle, start, radius + SkillTreeRadiusStep)
			children[1]?.assignLocation(childAngle, start + childAngle, radius + SkillTreeRadiusStep)
		}
		else
		{
			// split into 1 or 0
			val child = children[0] ?: children[1]

			child?.assignLocation(angle, start, radius + SkillTreeRadiusStep)
		}
	}
}
