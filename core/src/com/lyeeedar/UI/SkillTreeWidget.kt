package com.lyeeedar.UI

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Widget
import com.lyeeedar.Player.Ability.Skill
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Player.Ability.SkillTreeRadiusStep
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 05-Aug-16.
 */

class SkillTreeWidget(val skillTree: SkillTree) : Widget()
{
	val lineVec1 = Vector2()
	val lineVec2 = Vector2()
	val tempVec = Vector2()
	val shape = ShapeRenderer()
	val iconSize = 48f
	val circleCol = Color(1f, 1f, 1f, 0.2f)
	val circle = AssetManager.loadTexture("Sprites/largecircle.png")
	val background = AssetManager.loadTexture("Sprites/Oryx/uf_split/uf_terrain/ground_dirt_dark_1.png", wrapping = Texture.TextureWrap.Repeat)

	val min = Vector2()
	val max = Vector2()
	var maxDepth = 0

	init
	{
		touchable = Touchable.enabled

		fun getMinMax(skill: Skill, depth: Int)
		{
			if (skill.location.x < min.x) min.x = skill.location.x
			if (skill.location.x > max.x) max.x = skill.location.x
			if (skill.location.y < min.y) min.y = skill.location.y
			if (skill.location.y > max.y) max.y = skill.location.y
			if (depth > maxDepth) maxDepth = depth

			if (skill.children[0] != null) getMinMax(skill.children[0]!!, depth + 1)
			if (skill.children[1] != null) getMinMax(skill.children[1]!!, depth + 1)
		}

		for (skill in skillTree.rootSkills)
		{
			if (skill != null) getMinMax(skill, 1)
		}
	}

	override fun getPrefWidth(): Float = (max.x - min.x) + 200
	override fun getPrefHeight(): Float = (max.y - min.y) + 200

	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		super.draw(batch, parentAlpha)

		tempVec.set(x + width * 0.5f, y + height * 0.5f)

		batch?.draw(background, 0f, 0f, width, height, -x / 64f, -y / 64f, (-x + width) / 64f, (-y + height) / 64f)

		// draw lines
		fun drawLines(skill: Skill)
		{
			if (skill.children[0] != null)
			{
				lineVec1.set(skill.location).add(tempVec)
				lineVec2.set(skill.children[0]!!.location).add(tempVec)
				shape.line(lineVec1, lineVec2)

				drawLines(skill.children[0]!!)
			}

			if (skill.children[1] != null)
			{
				lineVec1.set(skill.location).add(tempVec)
				lineVec2.set(skill.children[1]!!.location).add(tempVec)
				shape.line(lineVec1, lineVec2)

				drawLines(skill.children[1]!!)
			}
		}

		fun drawIcons(skill: Skill)
		{
			val x = tempVec.x + skill.location.x - iconSize * 0.5f
			val y = tempVec.y + skill.location.y - iconSize * 0.5f
			skill.ability.icon.render(batch as SpriteBatch, x, y, iconSize, iconSize)

			if (skill.children[0] != null) drawIcons(skill.children[0]!!)
			if (skill.children[1] != null) drawIcons(skill.children[1]!!)
		}

		// draw background
		batch?.color = circleCol
		val radius = SkillTreeRadiusStep * maxDepth + 100
		//batch?.draw(circle, tempVec.x - radius, tempVec.y - radius, radius * 2, radius * 2)
		batch?.color = Color.WHITE

		batch?.end()
		batch?.begin()

		shape.setAutoShapeType(true)
		shape.begin()
		for (skill in skillTree.rootSkills)
		{
			if (skill != null)
			{
				lineVec1.set(tempVec)
				lineVec2.set(skill.location).add(tempVec)
				shape.line(lineVec1, lineVec2)

				drawLines(skill)
			}
		}
		shape.end()

		batch?.end()
		batch?.begin()

		skillTree.baseIcon.render(batch as SpriteBatch, tempVec.x - iconSize * 0.5f, tempVec.y - iconSize * 0.5f, iconSize, iconSize)

		// draw icons
		for (skill in skillTree.rootSkills)
		{
			if (skill != null)
			{
				drawIcons(skill)
			}
		}
	}
}