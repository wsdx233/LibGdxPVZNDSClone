package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Actor
import com.pvz.vidar.game.wsdx233.top.Assets

class ProgressBar : Actor() {

    private val bgRegion: TextureAtlas.AtlasRegion
    private val fillRegion: TextureAtlas.AtlasRegion
    private val flagRegion: TextureAtlas.AtlasRegion
    private val zombieRegion: TextureAtlas.AtlasRegion

    private val scale = 0.75f

    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    init {
        bgRegion = Assets.progressBarAtlas.findRegion("bg")
        fillRegion = Assets.progressBarAtlas.findRegion("fill")
        flagRegion = Assets.progressBarAtlas.findRegion("flag")
        zombieRegion = Assets.progressBarAtlas.findRegion("zombie")

        // 设置Actor的大小为背景的缩放大小
        width = bgRegion.regionWidth * scale
        height = bgRegion.regionHeight * scale
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val x = this.x
        val y = this.y

        // 1. 绘制背景
        batch.draw(
            bgRegion,
            x, y,
            bgRegion.regionWidth * scale,
            bgRegion.regionHeight * scale
        )

        // 2. 绘制进度填充（从右到左拉伸显示进度）
        if (progress > 0f) {
            val fillWidth = bgRegion.regionWidth * scale * progress
            val fillX = x + bgRegion.regionWidth * scale - fillWidth + 5f * scale  // 从右侧开始
            val fillY = y + (bgRegion.regionHeight * scale - fillRegion.regionHeight * scale) / 2f

            // 使用拉伸绘制填充条（从右到左）
            batch.draw(
                fillRegion,
                fillX, fillY,
                fillWidth - 10f * scale,  // 减去左右边距
                fillRegion.regionHeight * scale
            )
        }

        // 3. 绘制僵尸头（在进度条绿色条最左侧前方，从右到左移动）
        if (progress > 0f) {
            val fillWidth = bgRegion.regionWidth * scale * progress
            val zombieX = x + bgRegion.regionWidth * scale - fillWidth - zombieRegion.regionWidth * scale * 0.5f
            val zombieY = y + (bgRegion.regionHeight * scale - zombieRegion.regionHeight * scale) / 2f
            batch.draw(
                zombieRegion,
                zombieX, zombieY,
                zombieRegion.regionWidth * scale,
                zombieRegion.regionHeight * scale
            )
        }

        // 4. 绘制旗子（在最左侧，代表最终波，绘制在最上层）
        val flagX = x - flagRegion.regionWidth * scale * 0.5f + 10f
        val flagY = y + (bgRegion.regionHeight * scale - flagRegion.regionHeight * scale) / 2f + 5f
        batch.draw(
            flagRegion,
            flagX, flagY,
            flagRegion.regionWidth * scale,
            flagRegion.regionHeight * scale
        )
    }
}
