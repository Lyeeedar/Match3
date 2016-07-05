package com.lyeeedar

/**
 * Created by Philip on 04-Jul-16.
 */

class Global
{
	companion object
	{
		var fps = 60
		val tileSize = 32f
		var android = false
		var release = false
		lateinit var game: MainGame
		lateinit var applicationChanger: AbstractApplicationChanger

		fun setup()
		{

		}
	}
}