package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.pvz.vidar.game.wsdx233.top.Assets

class Soil : Actor() {
    private val soilAnimation: Animation<TextureRegion>
    private var stateTime = 0f

    init {
        val soilFrames = Assets.soil.findRegions("soil")
        soilAnimation = Animation(0.07f, soilFrames, Animation.PlayMode.NORMAL)
        val firstFrame = soilFrames.first()
        setSize(firstFrame.regionWidth.toFloat(), firstFrame.regionHeight.toFloat())
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta
        if (soilAnimation.isAnimationFinished(stateTime)) {
            remove()
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        val currentFrame = soilAnimation.getKeyFrame(stateTime, false)
        batch.draw(currentFrame, x, y, width, height)
    }
}
