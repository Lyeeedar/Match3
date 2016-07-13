package com.lyeeedar.Board

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.XmlReader
import com.lyeeedar.Sprite.Sprite
import com.lyeeedar.Sprite.SpriteWrapper
import com.lyeeedar.Util.AssetManager

/**
 * Created by Philip on 13-Jul-16.
 */

class LevelTheme
{
	lateinit var floor: SpriteWrapper
	lateinit var wall: SpriteWrapper
	lateinit var pit: SpriteWrapper

	companion object
	{
		fun load(path: String): LevelTheme
		{
			val xml = XmlReader().parse(Gdx.files.internal("Themes/$path.xml"))

			val theme = LevelTheme()
			theme.floor = SpriteWrapper.load(xml.getChildByName("Floor"))
			theme.wall = SpriteWrapper.load(xml.getChildByName("Wall"))
			theme.pit = SpriteWrapper.load(xml.getChildByName("Pit"))

			return theme
		}
	}
}