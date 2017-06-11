package com.lyeeedar

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.lyeeedar.Board.Level
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Map.World
import com.lyeeedar.Player.Player
import com.lyeeedar.Player.PlayerData
import com.lyeeedar.Player.SaveGame
import com.lyeeedar.Screens.*
import com.lyeeedar.Town.Town
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.swing.JOptionPane

class MainGame : Game()
{
	val debugOverride = false

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
		if (debugOverride)
		{
			val playerData = PlayerData()
			val player = Player(playerData)

			val level = Level.load("Dungeon/Encounter/SmallFireElemental").first()
			val theme = LevelTheme.load("Dungeon")

			level.create(theme, player)
			Global.game.switchScreen(MainGame.ScreenEnum.GRID)
			GridScreen.instance.updateLevel(level, player)
		}
		else if (save == null)
		{
			val player = PlayerData()
			val world = World()
			val town = Town(player, world)
			Global.settings = Settings()
			screens.put(ScreenEnum.TOWN, TownScreen(player, town))

			println("New town")
		}
		else
		{
			try
			{
				val playerData = save.playerData.get()
				val world = save.world.get()
				val town = save.town.get(playerData, world)
				Global.settings = save.settings
				screens.put(ScreenEnum.TOWN, TownScreen(playerData, town))

				println("Loaded town")

				if (save.dungeon != null && save.player != null)
				{
					try
					{
						val dungeon = save.dungeon!!.get()
						val player = save.player!!.get(playerData)

						MapScreen.instance.setMap(dungeon, player, world.dungeons.first { it.name == dungeon.dungeonName })

						println("Loaded dungeon")
					}
					catch (ex: Exception)
					{
						System.err.println("Dungeon Load failed\n" + ex.message)

						save.dungeon = null
					}
				}
			}
			catch (ex: Exception)
			{
				System.err.println("Load failed\n" + ex.message)

				val player = PlayerData()
				val world = World()
				val town = Town(player, world)
				Global.settings = Settings()
				screens.put(ScreenEnum.TOWN, TownScreen(player, town))

				save = null

				println("New town")
			}
		}

		if (Global.PARTICLE_EDITOR)
		{
			setScreen(ParticleEditorScreen())
		}
		else if (debugOverride)
		{

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

	override fun setScreen(screen: Screen?)
	{
		super.setScreen(screen)

		val ascreen = screen as AbstractScreen
		Global.stage = ascreen.stage
	}
}
