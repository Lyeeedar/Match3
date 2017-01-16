package com.lyeeedar

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.lyeeedar.Player.Player
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Player.SaveGame
import com.lyeeedar.Screens.*
import com.lyeeedar.Town.Town

import javax.swing.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.HashMap

class MainGame : Game()
{

	enum class ScreenEnum
	{
		GRID,
		MAP,
		TOWN
	}

	private val screens = HashMap<ScreenEnum, AbstractScreen>()

	override fun create()
	{
		Global.applicationChanger.processResources()
		Global.setup()

		if (Global.android)
		{
//			val sw = StringWriter()
//			val handler = Thread.UncaughtExceptionHandler { myThread, e ->
//				val exceptionAsString = sw.toString()
//				JOptionPane.showMessageDialog(null, "A fatal error occurred:\n" + exceptionAsString, "An error occurred", JOptionPane.ERROR_MESSAGE)
//			}
//
//			Thread.currentThread().uncaughtExceptionHandler = handler
		}
		else
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
		screens.put(ScreenEnum.MAP, MapScreen())

		var save = SaveGame.load()
		if (save == null)
		{
			val player = PlayerData()
			val town = Town(player)
			screens.put(ScreenEnum.TOWN, TownScreen(player, town))

			println("New town")
		}
		else
		{
			try
			{
				val playerData = save.playerData.get()
				val town = save.town.get(playerData)
				screens.put(ScreenEnum.TOWN, TownScreen(playerData, town))

				println("Loaded town")

				if (save.dungeon != null && save.player != null)
				{
					val dungeon = save.dungeon!!.get()
					val player = save.player!!.get(playerData)

					MapScreen.instance.setMap(dungeon, player)

					println("Loaded dungeon")
				}
			}
			catch (ex: Exception)
			{
				System.err.println("Load failed")

				val player = PlayerData()
				val town = Town(player)
				screens.put(ScreenEnum.TOWN, TownScreen(player, town))

				save = null

				println("New town")
			}
		}

		if (Global.PARTICLE_EDITOR)
		{
			setScreen(ParticleEditorScreen())
		}
		else if (save?.dungeon != null)
		{
			switchScreen(ScreenEnum.MAP)
		}
		else
		{
			switchScreen(ScreenEnum.TOWN)
		}
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
