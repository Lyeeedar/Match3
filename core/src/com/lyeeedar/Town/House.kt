package com.lyeeedar.Town

import com.badlogic.gdx.graphics.Color
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.UI.UnlockTree
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 02-Aug-16.
 */

class House
{
	lateinit var sprite: Sprite

	lateinit var skillTree: UnlockTree<Ability>

	constructor(sprite: Sprite, playerData: PlayerData)
	{
		this.sprite = sprite

		skillTree = playerData.getSkillTree("Skills/Fire")
	}
}