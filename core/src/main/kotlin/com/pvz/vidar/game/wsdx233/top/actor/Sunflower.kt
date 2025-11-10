package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

enum class SunflowerState {
    IDLE,
    PRODUCING,
    COOLDOWN
}

class Sunflower(gameScreen: GameScreen) : Plant(gameScreen) {
    private val textureAtlas = TextureAtlas("game/actor/sunflower/sunflower.atlas")
    private var currentAnimation: Animation<TextureAtlas.AtlasRegion>
    private var stateTime = 0f

    var state = SunflowerState.IDLE

    private val idleAnimation: Animation<TextureAtlas.AtlasRegion>
    private val producingAnimation: Animation<TextureAtlas.AtlasRegion>

    init {
        val idleFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..5) {
            idleFrames.add(textureAtlas.findRegion("idle", i))
        }

        for (i in 4 downTo 1) {
            idleFrames.add(textureAtlas.findRegion("idle", i))
        }
        idleAnimation = Animation(0.2f, idleFrames, Animation.PlayMode.LOOP)

        val producingFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..5) {
            producingFrames.add(textureAtlas.findRegion("producing", i))
        }
        producingAnimation = Animation(0.2f, producingFrames, Animation.PlayMode.NORMAL)

        currentAnimation = idleAnimation
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta

        when (state) {
            SunflowerState.IDLE -> {
                currentAnimation = idleAnimation
                // After some time, change to producing
                if (stateTime > 6f) {
                    state = SunflowerState.PRODUCING
                    stateTime = 0f
                }
            }
            SunflowerState.PRODUCING -> {
                currentAnimation = producingAnimation
                if (producingAnimation.isAnimationFinished(stateTime)) {
                    // 创建从向日葵生成的阳光（抛物线运动）
                    val sun = Sun(
                        gameScreen = gameScreen,
                        sunType = Sun.SunType.FROM_SUNFLOWER,
                        startX = x + 10,
                        startY = y + 20,
                        targetY = y  // 地面高度
                    )
                    gameScreen.addSun(sun)
                    state = SunflowerState.COOLDOWN
                }
            }
            SunflowerState.COOLDOWN -> {
                currentAnimation = idleAnimation
                if (stateTime > 8f) { // Cooldown time
                    state = SunflowerState.IDLE
                    stateTime = 0f
                }
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)
        batch.color = color
        val index = currentAnimation.getKeyFrameIndex(stateTime)
        var drawX = x
        val offsets = listOf(0f,1.5f,4f,6f,8f,8f,8f,6f,4f,1.5f)
        drawX += offsets[index] + 5f
        val frame = currentAnimation.getKeyFrame(stateTime, true)
        batch.draw(frame, drawX, y, frame.regionWidth.toFloat()*1.5f, frame.regionHeight.toFloat()*1.5f)
    }
}
