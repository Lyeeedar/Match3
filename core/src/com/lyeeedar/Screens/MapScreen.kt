package com.lyeeedar.Screens

import com.badlogic.gdx.graphics.g2d.NinePatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Touchable
import com.badlogic.gdx.scenes.scene2d.ui.Stack
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable
import com.lyeeedar.Board.LevelTheme
import com.lyeeedar.Global
import com.lyeeedar.Map.DungeonMap
import com.lyeeedar.Map.Generators.HubGenerator
import com.lyeeedar.Player.Ability.Ability
import com.lyeeedar.Player.Player
import com.lyeeedar.UI.DungeonMapWidget
import com.lyeeedar.UI.PlayerWidget
import com.lyeeedar.UI.SpriteWidget
import com.lyeeedar.Util.AssetManager
import com.lyeeedar.Util.addClickListener

/**
 * Created by Philip on 25-Jul-16.
 */

class MapScreen(): AbstractScreen()
{
	init
	{
		instance = this
	}

	lateinit var portrait: SpriteWidget
	var dungeonWidget: DungeonMapWidget? = null

	fun getPortraitPos(): Vector2 = portrait.localToStageCoordinates(Vector2(portrait.width * 0.5f, portrait.height * 0.5f))

	override fun create()
	{

	}

	fun setMap(map: DungeonMap, player: Player)
	{
		if (!created){
			baseCreate()
			created = true
		}

		map.objective.update(map, map.map[map.playerPos.hashCode()])

		mainTable.clear()

		dungeonWidget = DungeonMapWidget(map, player)
		portrait = SpriteWidget(player.portrait.copy(), 48f, 48f)

		val stack = Stack()
		stack.add(dungeonWidget)

		val overlay = Table()
		overlay.defaults().pad(10f)

		val portraitTable = Table()
		portraitTable.background = NinePatchDrawable(NinePatch(AssetManager.loadTextureRegion("Sprites/GUI/background.png"), 24, 24, 24, 24))
		portraitTable.add(portrait)
		portraitTable.add(map.objective.createDynamicTable(Global.skin))
		portraitTable.touchable = Touchable.enabled
		portraitTable.addClickListener { PlayerWidget(player, map.objective) }

		overlay.add(portraitTable).expand().top().left()
		stack.add(overlay)

		mainTable.add(stack).expand().fill()
	}

	override fun doRender(delta: Float)
	{

	}

	override fun show()
	{
		super.show()
		dungeonWidget?.save()
	}

	companion object
	{
		lateinit var instance: MapScreen
	}
}