package com.lyeeedar.Town

import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Ability.Skill
import com.lyeeedar.Player.Ability.SkillTree
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class House
{
	lateinit var sprite: Sprite

	lateinit var skillTree: SkillTree

	constructor(sprite: Sprite)
	{
		this.sprite = sprite

		skillTree = SkillTree.load("Fire")
	}
}