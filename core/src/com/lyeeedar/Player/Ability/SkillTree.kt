package com.lyeeedar.Player.Ability

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.ObjectMap
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.getXml
import com.lyeeedar.Util.set

/**
 * Created by Philip on 05-Aug-16.
 */

val SkillTreeRadiusStep = 100f
private val tempVec = Vector2()

class SkillTree(val numBaseSkills: Int)
{
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

	fun descendants(boughtOnly: Boolean = false) : com.badlogic.gdx.utils.Array<Skill>
	{
		val skills = com.badlogic.gdx.utils.Array<Skill>()

		for (skill in rootSkills)
		{
			skill?.descendants(skills, boughtOnly)
		}

		return skills
	}

	companion object
	{
		fun load(path: String): SkillTree
		{
			val xml = getXml("Skills/$path")

			val resources = ObjectMap<String, XmlReader.Element>()
			val abilities = ObjectMap<String, Ability>()

			val resourcesEl = xml.getChildByName("Resources")
			for (i in 0..resourcesEl.childCount-1)
			{
				val el = resourcesEl.getChild(i)
				resources[el.getAttribute("Key")] = el
			}

			val abilitiesEl = xml.getChildByName("Abilities")
			for (i in 0..abilitiesEl.childCount-1)
			{
				val el = abilitiesEl.getChild(i)
				val ability = Ability.load(el, resources)
				abilities[el.name] = ability
			}

			val treeEl = xml.getChildByName("Tree")

			val tree = SkillTree(treeEl.childCount)
			tree.baseIcon = AssetManager.tryLoadSpriteWithResources(xml.getChildByName("Icon"), resources)

			for (i in 0..treeEl.childCount-1)
			{
				val el = treeEl.getChild(i)
				val ability = abilities[el.name]
				val skill = Skill(ability)
				skill.parse(el, abilities, resources)

				tree.rootSkills[i] = skill
			}

			tree.assignLocations()
			return tree
		}
	}
}

class Skill(val ability: Ability)
{
	val costs = ObjectMap<String, Int>()
	lateinit var unboughtDescription: String
	var bought = false

	val location: Vector2 = Vector2()
	val children: Array<Skill?> = Array(2){ e -> null }

	fun descendants(array: com.badlogic.gdx.utils.Array<Skill>, boughtOnly: Boolean)
	{
		array.add(this)

		if (boughtOnly && !bought) return

		for (child in children)
		{
			child?.descendants(array, boughtOnly)
		}
	}

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

	fun parse(xml: XmlReader.Element, abilities: ObjectMap<String, Ability>, resources: ObjectMap<String, XmlReader.Element>)
	{
		// parse cost
		val costsEl = xml.getChildByName("Cost")
		if (costsEl != null)
		{
			for (i in 0..costsEl.childCount - 1)
			{
				val el = costsEl.getChild(i)
				costs[el.name] = el.text.toInt()
			}

			unboughtDescription = xml.get("UnboughtDescription", ability.description)
		}
		else
		{
			bought = true
		}

		val childrenEl = xml.getChildByName("Children") ?: return
		for (i in 0..childrenEl.childCount-1)
		{
			val el = childrenEl.getChild(i)
			val ability = abilities[el.name]
			val skill = Skill(ability)
			skill.parse(el, abilities, resources)

			children[i] = skill
		}
	}
}
