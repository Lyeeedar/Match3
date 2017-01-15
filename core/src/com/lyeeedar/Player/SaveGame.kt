package com.lyeeedar.Player

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectMap
import com.esotericsoftware.kryo.Kryo
import com.lyeeedar.Renderables.Sprite.Sprite
import com.lyeeedar.Sound.SoundInstance
import com.lyeeedar.Renderables.Sprite.Sprite.AnimationMode
import com.badlogic.gdx.graphics.Color.rgba8888ToColor
import com.badlogic.gdx.utils.ObjectSet
import com.badlogic.gdx.utils.XmlReader
import com.esotericsoftware.kryo.Serializer
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.kryo.FastEnumMapSerializer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.Generators.HubGenerator
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Town.Town
import com.lyeeedar.Util.*
import java.util.zip.GZIPOutputStream
import java.nio.file.Files.delete
import com.sun.deploy.util.SystemUtils.readBytes
import ktx.collections.set
import ktx.collections.toGdxArray
import java.util.zip.GZIPInputStream

class SaveGame
{
	lateinit var playerData: SavePlayerData
	lateinit var town: SaveTown
	var dungeon: SaveDungeonMap? = null
	var player: SavePlayer? = null

	fun save()
	{
		val attemptFile = Gdx.files.local("attempt_save.dat")

		var output: Output? = null
		try
		{
			output = Output(GZIPOutputStream(attemptFile.write(false)))
		}
		catch (e: Exception)
		{
			e.printStackTrace()
			return
		}

		kryo.writeObject(output, this)

		output.close()

		val bytes = attemptFile.readBytes()
		val actualFile = Gdx.files.local("save.dat")
		actualFile.writeBytes(bytes, false)

		attemptFile.delete()

		println("Saved")
	}

	companion object
	{
		fun load(): SaveGame?
		{
			var input: Input? = null
			try
			{
				input = Input(GZIPInputStream(Gdx.files.local("save.dat").read()))
			}
			catch (e: Exception)
			{
				e.printStackTrace()
				return null
			}

			val save = kryo.readObject(input, SaveGame::class.java)
			input.close()

			return save
		}

		val kryo: Kryo by lazy { setup() }

		fun setup(): Kryo
		{
			val kryo = Kryo()
			kryo.isRegistrationRequired = true
			kryo.fieldSerializerConfig.isUseAsm = true

			registerSerializers(kryo)
			registerClasses(kryo)

			return kryo
		}

		private fun registerSerializers(kryo: Kryo)
		{
			kryo.register(FastEnumMap::class.java, FastEnumMapSerializer())

			kryo.register(Sprite::class.java, object : Serializer<Sprite>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<Sprite>): Sprite
				{
					val fileName = input.readString()
					val animDelay = input.readFloat()
					val repeatDelay = input.readFloat()
					val colour = kryo.readObject(input, Colour::class.java)
					val modeVal = input.readInt()
					val mode = AnimationMode.values()[modeVal]
					val scale = input.readFloats(2)
					val drawActualSize = input.readBoolean()

					val sprite = AssetManager.loadSprite(fileName, animDelay, colour, mode, drawActualSize)
					sprite.baseScale = scale
					sprite.repeatDelay = repeatDelay
					return sprite
				}

				override fun write(kryo: Kryo, output: Output, sprite: Sprite)
				{
					output.writeString(sprite.fileName)
					output.writeFloat(sprite.animationDelay)
					output.writeFloat(sprite.repeatDelay)
					kryo.writeObject(output, sprite.colour)
					output.writeInt(sprite.animationState.mode.ordinal)
					output.writeFloats(sprite.baseScale)
					output.writeBoolean(sprite.drawActualSize)
				}
			})

			kryo.register(Point::class.java, object : Serializer<Point>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<Point>): Point
				{
					val x = input.readInt()
					val y = input.readInt()

					return Point.obtain().set(x, y)
				}

				override fun write(kryo: Kryo, output: Output, point: Point)
				{
					output.writeInt(point.x)
					output.writeInt(point.y)
				}
			})

			kryo.register(Colour::class.java, object : Serializer<Colour>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<Colour>): Colour
				{
					val r = input.readFloat()
					val g = input.readFloat()
					val b = input.readFloat()
					val a = input.readFloat()

					return Colour(r, g, b, a)
				}

				override fun write(kryo: Kryo, output: Output, colour: Colour)
				{
					output.writeFloat(colour.r)
					output.writeFloat(colour.g)
					output.writeFloat(colour.b)
					output.writeFloat(colour.a)
				}
			})

			kryo.register(Array::class.java, object : Serializer<Array<*>>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<Array<*>>): Array<*>
				{
					val array = Array<Any>()
					kryo.reference(array)

					val length = input.readInt(true)
					array.ensureCapacity(length)

					for (i in 0..length-1)
					{
						val obj = kryo.readClassAndObject(input)
						array.add(obj)
					}

					return array
				}

				override fun write(kryo: Kryo, output: Output, array: Array<*>)
				{
					val length = array.size
					output.writeInt(length, true)

					for (i in 0..length-1)
					{
						kryo.writeClassAndObject(output, array[i])
					}
				}
			})

			kryo.register(ObjectMap::class.java, object : Serializer<ObjectMap<*, *>>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<ObjectMap<*, *>>): ObjectMap<*, *>
				{
					val map = ObjectMap<Any, Any>()
					kryo.reference(map)

					val length = input.readInt(true)
					map.ensureCapacity(length)

					for (i in 0..length-1)
					{
						val key = kryo.readClassAndObject(input)
						val value = kryo.readClassAndObject(input)

						map[key] = value
					}

					return map
				}

				override fun write(kryo: Kryo, output: Output, map: ObjectMap<*, *>)
				{
					val length = map.size
					output.writeInt(length, true)

					for (entry in map)
					{
						kryo.writeClassAndObject(output, entry.key)
						kryo.writeClassAndObject(output, entry.value)
					}
				}
			})

			kryo.register(XmlReader.Element::class.java, object : Serializer<XmlReader.Element>()
			{
				override fun read(kryo: Kryo, input: Input, type: Class<XmlReader.Element>): XmlReader.Element
				{
					val xml = input.readString()

					val reader = XmlReader()
					val element = reader.parse(xml)
					return element
				}

				override fun write(kryo: Kryo, output: Output, element: XmlReader.Element)
				{
					output.writeString(element.toString())
				}
			})
		}

		private fun registerClasses(kryo: Kryo)
		{
			kryo.register(Point::class.java)

			kryo.register(SaveGame::class.java)
			kryo.register(SavePlayerData::class.java)
			kryo.register(SavePlayer::class.java)
			kryo.register(SaveTown::class.java)
			kryo.register(SaveDungeonMap::class.java)

			kryo.register(Item::class.java)

			kryo.register(kotlin.Array<String>::class.java)
			kryo.register(kotlin.Array<Any>::class.java)
		}
	}
}

interface SaveableObject<T>
{
	fun store(data: T): SaveableObject<T>
	fun get(vararg obj: Any): T
}

class SavePlayerData : SaveableObject<PlayerData>
{
	var chosenSprite: Int = 0
	var unlockedSprites = Array<Sprite>()
	var unlockedAbilities = ObjectMap<String, Array<String>>()
	var equippedAbilities = Array<String?>(4){e -> null}
	var gold = 0
	var inventory = ObjectMap<String, Item>()

	override fun store(data: PlayerData) : SavePlayerData
	{
		unlockedSprites.addAll(data.unlockedSprites)
		chosenSprite = unlockedSprites.indexOf(data.chosenSprite)
		gold = data.gold

		for (i in 0..3)
		{
			equippedAbilities[i] = data.abilities[i]
		}

		for (obj in data.inventory)
		{
			inventory[obj.key] = obj.value.copy()
		}

		for (tree in data.skillTrees)
		{
			val bought = tree.value.boughtDescendants().map{ it.key }
			unlockedAbilities[tree.key] = bought.asGdxArray()
		}

		return this
	}

	override fun get(vararg obj: Any) : PlayerData
	{
		val data = PlayerData()
		data.unlockedSprites.clear()
		data.unlockedSprites.addAll(unlockedSprites)
		for (sprite in unlockedSprites) sprite.drawActualSize = true

		data.chosenSprite = unlockedSprites[chosenSprite]
		data.gold = gold

		for (i in 0..3)
		{
			data.abilities[i] = equippedAbilities[i]
		}

		for (item in inventory)
		{
			data.inventory[item.key] = item.value
		}

		for (unlocked in unlockedAbilities)
		{
			val tree = data.skillTrees[unlocked.key]
			val abilities = tree.descendants()

			for (item in unlocked.value)
			{
				abilities[item].bought = true
			}
		}

		return data
	}
}

class SaveTown : SaveableObject<Town>
{
	lateinit var playerPos: Point

	override fun store(data: Town) : SaveTown
	{
		playerPos = data.playerPos.copy()

		return this
	}

	override fun get(vararg obj: Any): Town
	{
		val town = Town(obj[0] as PlayerData)

		town.playerPos.set(playerPos)

		return town
	}
}

class SaveDungeonMap : SaveableObject<DungeonMap>
{
	lateinit var theme: String
	var seed: Long = 0
	var numRooms: Int = 0
	lateinit var playerPos: Point
	lateinit var completedRooms: Array<Int>
	lateinit var seenRooms: Array<Int>

	override fun get(vararg obj: Any): DungeonMap
	{
		val themeObj = LevelTheme.load(theme)
		val generator = HubGenerator(seed)
		val dungeon = generator.generate(themeObj, numRooms)

		for (seen in seenRooms)
		{
			dungeon.map[seen].seen = true
		}

		for (completed in completedRooms)
		{
			dungeon.map[completed].level!!.completed = true
		}

		dungeon.playerPos.set(playerPos)

		return dungeon
	}

	override fun store(data: DungeonMap) : SaveDungeonMap
	{
		theme = data.theme.name
		seed = data.seed
		numRooms = data.numRooms
		playerPos = data.playerPos.copy()

		seenRooms = Array()
		completedRooms = Array()

		for (room in data.map.entries())
		{
			if (room.value.seen) seenRooms.add(room.key)
			if (room.value.isCompleted && room.value.level != null) completedRooms.add(room.key)
		}

		return this
	}
}

class SavePlayer : SaveableObject<Player>
{
	lateinit var portrait: Sprite
	var hp: Int = 0
	var regen: Int = 0
	var gold: Int = 0
	var inventory = ObjectMap<String, Item>()
	var equippedAbilities = Array<String?>(4){e -> null}

	override fun store(data: Player) : SavePlayer
	{
		portrait = data.portrait.copy()
		hp = data.hp
		regen = data.regen
		gold = data.gold

		for (i in 0..3)
		{
			equippedAbilities[i] = data.abilities[i]?.key
		}

		for (obj in data.inventory)
		{
			inventory[obj.key] = obj.value.copy()
		}

		return this
	}

	override fun get(vararg obj: Any): Player
	{
		val data = obj[0] as PlayerData

		val player = Player()
		player.portrait = portrait
		portrait.drawActualSize = true

		player.hp = hp
		player.regen = regen
		player.gold = gold

		for (item in inventory)
		{
			player.inventory[item.key] = item.value
		}

		for (i in 0..3)
		{
			if (equippedAbilities[i] != null)
			{
				player.abilities[i] = data.getAbility(equippedAbilities[i]!!)
			}
		}

		return player
	}
}