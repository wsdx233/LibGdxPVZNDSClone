package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.utils.Array
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

class Pea(private val gameScreen: GameScreen) : Actor() {
    private val textureAtlas = TextureAtlas("game/actor/peashooter/peashooter.atlas")
    private var stateTime = 0f
    private val speed = 200f
    val collisionBox = Rectangle(x, y, 12f, 12f)
    private var isHit = false

    var row = 0

    private val flyAnimation: Animation<TextureAtlas.AtlasRegion>
    private val hitAnimation: Animation<TextureAtlas.AtlasRegion>

    init {
        val flyFrames = Array<TextureAtlas.AtlasRegion>()
        flyFrames.add(textureAtlas.findRegion("pea", 0))
        flyAnimation = Animation(0.1f, flyFrames, Animation.PlayMode.NORMAL)

        val hitFrames = Array<TextureAtlas.AtlasRegion>()
        hitFrames.add(textureAtlas.findRegion("pea", 1))
        hitFrames.add(textureAtlas.findRegion("pea", 2))
        hitAnimation = Animation(0.1f, hitFrames, Animation.PlayMode.NORMAL)

        width = 12f
        height = 12f
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta

        if (!isHit) {
            x += speed * delta
            collisionBox.x = x
            collisionBox.y = y

            if (x > gameScreen.stage.width) {
                remove()
            }

            for (zombie in gameScreen.zombies) {
                if (collisionBox.overlaps(zombie.collisionBox) && zombie.row == this.row && zombie.isAlive()) {
                    zombie.receiveDamage(20)
                    isHit = true
                    stateTime = 0f
                    break
                }
            }
        } else {
            if (hitAnimation.isAnimationFinished(stateTime)) {
                remove()
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch.color = color
        val frame = if (isHit) {
            hitAnimation.getKeyFrame(stateTime, false)
        } else {
            flyAnimation.getKeyFrame(stateTime, true)
        }
        batch.color = Color.WHITE
        batch.draw(frame, x, y, frame.regionWidth.toFloat()*1.5f, frame.regionHeight.toFloat()*1.5f)
    }
}
