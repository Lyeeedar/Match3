package com.lyeeedar

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.lyeeedar.Screens.AbstractScreen
import com.lyeeedar.Screens.GridScreen
import com.lyeeedar.Screens.LevelSelectScreen
import com.lyeeedar.Screens.MapScreen

import javax.swing.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap

class MainGame : Game()
{

	enum class ScreenEnum
	{
		GRID,
		LEVELSELECT,
		MAP,
		TOWN,
		SKILLSHOP
	}

	private val screens = HashMap<ScreenEnum, AbstractScreen>()

	override fun create()
	{
		Global.applicationChanger.processResources()
		Global.setup()

		if (!Global.android)
		{
			val sw = StringWriter()
			val pw = PrintWriter(sw)

			val handler = Thread.UncaughtExceptionHandler { myThread, e ->
				e.printStackTrace(pw)
				val exceptionAsString = sw.toString()

				val file = Gdx.files.local("error.log")
				file.writeString(exceptionAsString, false)

				JOptionPane.showMessageDialog(null, "A fatal error occurred. Please send the error.log to me so that I can fix it.", "An error occurred", JOptionPane.ERROR_MESSAGE)

				e.printStackTrace()
			}

			Thread.currentThread().uncaughtExceptionHandler = handler
		}

		screens.put(ScreenEnum.GRID, GridScreen())
		screens.put(ScreenEnum.LEVELSELECT, LevelSelectScreen())
		screens.put(ScreenEnum.MAP, MapScreen())

		switchScreen(ScreenEnum.MAP)
	}

	fun switchScreen(screen: AbstractScreen)
	{
		this.setScreen(screen)
	}

	fun switchScreen(screen: ScreenEnum)
	{
		this.setScreen(screens[screen])
	}
}
