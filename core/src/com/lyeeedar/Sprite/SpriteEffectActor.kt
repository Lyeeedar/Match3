open class SpriteEffectActor(val sprite: Sprite, val width: Float, val height: Float val pos: Vector2, val completionFunc: (() -> Unit)? = null): Actor()
{
	init
	{
		Global.stage.addActor(this)
	}
	
	override fun act(delta: Float)
	{
		super.act(delta)
		val complete = sprite.update(delta)
		if (complete)
		{
			completionFunc?.invoke()
			remove()
		}
	}
	
	override fun draw(batch: Batch?, parentAlpha: Float)
	{
		super.draw(batch, parentAlpha)

		var x = pos.x
		var y = pos.y
		
		if ( sprite.spriteAnimation != null )
		{
			val offset = sprite.spriteAnimation?.renderOffset()

			if (offset != null)
			{
				x += offset[0]
				y += offset[1]
			}
		}
		
		sprite.render(batch as SpriteBatch, x, y, width, height)
	}
}
