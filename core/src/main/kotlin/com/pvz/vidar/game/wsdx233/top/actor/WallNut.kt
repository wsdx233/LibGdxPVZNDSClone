package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

enum class WallNutHealthStage {
    FULL,      // 满血状态
    DAMAGED_1, // 第一阶段损伤
    DAMAGED_2  // 第二阶段损伤（最严重）
}

class WallNut(gameScreen: GameScreen) : Plant(gameScreen) {
    private val textureAtlas = TextureAtlas("game/actor/wall_nut/wall_nut.atlas")
    private var currentAnimation: Animation<TextureAtlas.AtlasRegion>
    private var stateTime = 0f
    private var currentHealthStage = WallNutHealthStage.FULL

    private val idleAnimation: Animation<TextureAtlas.AtlasRegion>
    private val damaged1Animation: Animation<TextureAtlas.AtlasRegion>
    private val damaged2Animation: Animation<TextureAtlas.AtlasRegion>

    init {
        // 设置坚果墙的高生命值
        hp = 4000

        // Idle animation - full health
        val idleFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..4) {
            idleFrames.add(textureAtlas.findRegion("idle", i))
        }
        idleAnimation = Animation(0.2f, idleFrames, Animation.PlayMode.LOOP)

        // Damaged 1 animation - first damage stage
        val damaged1Frames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..4) {
            damaged1Frames.add(textureAtlas.findRegion("damaged_1", i))
        }
        damaged1Animation = Animation(0.2f, damaged1Frames, Animation.PlayMode.LOOP)

        // Damaged 2 animation - second damage stage (most damaged)
        val damaged2Frames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..4) {
            damaged2Frames.add(textureAtlas.findRegion("damaged_2", i))
        }
        damaged2Animation = Animation(0.2f, damaged2Frames, Animation.PlayMode.LOOP)

        currentAnimation = idleAnimation
        width = 28f
        height = 32f
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta

        // 根据生命值更新健康阶段和动画
        updateHealthStage()
    }

    private fun updateHealthStage() {
        val previousStage = currentHealthStage

        currentHealthStage = when {
            hp > 2666 -> WallNutHealthStage.FULL      // 66.7% - 100%
            hp > 1333 -> WallNutHealthStage.DAMAGED_1 // 33.3% - 66.7%
            else -> WallNutHealthStage.DAMAGED_2      // 0% - 33.3%
        }

        // 如果健康阶段改变，更新动画
        if (previousStage != currentHealthStage) {
            currentAnimation = when (currentHealthStage) {
                WallNutHealthStage.FULL -> idleAnimation
                WallNutHealthStage.DAMAGED_1 -> damaged1Animation
                WallNutHealthStage.DAMAGED_2 -> damaged2Animation
            }
            stateTime = 0f // 重置动画时间以平滑过渡
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch.color = color

        val frame = currentAnimation.getKeyFrame(stateTime, true)
        val scale = 1.5f

        // 居中绘制
        batch.draw(
            frame,
            x + 30f - frame.regionWidth.toFloat() * scale / 2,
            y,
            frame.regionWidth.toFloat() * scale,
            frame.regionHeight.toFloat() * scale
        )
    }
}
