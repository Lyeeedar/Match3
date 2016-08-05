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

		skillTree = SkillTree()
		skillTree.baseIcon = AssetManager.loadSprite("Icons/Action", colour = Color.FIREBRICK)
		skillTree.rootSkills[3] = Skill(Ability(icon = AssetManager.loadSprite("Icons/Find", colour = Color.CYAN), cost = 1, elite = false))
		skillTree.rootSkills[1] = Skill(Ability(icon = AssetManager.loadSprite("Icons/Bash", colour = Color.FOREST), cost = 1, elite = false))
		skillTree.rootSkills[0] = Skill(Ability(icon = AssetManager.loadSprite("Icons/Aim", colour = Color.CHARTREUSE), cost = 1, elite = false))

		skillTree.rootSkills[1]!!.children[0] = Skill(Ability(icon = AssetManager.loadSprite("Icons/Gather", colour = Color.GOLDENROD), cost = 1, elite = false))
		skillTree.rootSkills[1]!!.children[1] = Skill(Ability(icon = AssetManager.loadSprite("Icons/Gather", colour = Color.GOLDENROD), cost = 1, elite = false))

		skillTree.assignLocations()
	}
}