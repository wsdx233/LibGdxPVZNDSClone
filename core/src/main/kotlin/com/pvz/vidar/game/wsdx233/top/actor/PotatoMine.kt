package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen
import kotlin.math.abs

enum class PotatoMineState {
    UNDERGROUND,  // 埋在地下，popping index=0
    POPPING,      // 正在出土
    IDLE,         // 出土后待命
    EXPLODING,    // 爆炸中
    DEAD          // 已爆炸
}

class PotatoMine(gameScreen: GameScreen) : Plant(gameScreen) {
    private val textureAtlas = TextureAtlas("game/actor/potato_mine/potato_mine.atlas")
    private var currentAnimation: Animation<TextureAtlas.AtlasRegion>
    private var stateTime = 0f
    private var undergroundTime = 0f
    private val armTime = 10f // 10秒后出土
    private var hasExploded = false

    var state = PotatoMineState.UNDERGROUND

    private val undergroundAnimation: Animation<TextureAtlas.AtlasRegion>
    private val poppingAnimation: Animation<TextureAtlas.AtlasRegion>
    private val idleAnimation: Animation<TextureAtlas.AtlasRegion>
    private val explosionAnimation: Animation<TextureAtlas.AtlasRegion>

    init {
        // Underground animation - just the first popping frame
        val undergroundFrames = Array<TextureAtlas.AtlasRegion>()
        undergroundFrames.add(textureAtlas.findRegion("popping", 0))
        undergroundAnimation = Animation(1f, undergroundFrames, Animation.PlayMode.LOOP)

        // Popping animation - emerging from ground
        val poppingFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..2) {
            poppingFrames.add(textureAtlas.findRegion("popping", i))
        }
        poppingAnimation = Animation(0.15f, poppingFrames, Animation.PlayMode.NORMAL)

        // Idle animation - waiting for zombies
        val idleFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..4) {
            idleFrames.add(textureAtlas.findRegion("idle", i))
        }
        idleAnimation = Animation(0.2f, idleFrames, Animation.PlayMode.LOOP_PINGPONG)

        // Explosion animation
        val explosionFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..7) {
            explosionFrames.add(textureAtlas.findRegion("explosion", i))
        }
        explosionAnimation = Animation(0.1f, explosionFrames, Animation.PlayMode.NORMAL)

        currentAnimation = undergroundAnimation
        width = 30f
        height = 30f
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta

        when (state) {
            PotatoMineState.UNDERGROUND -> {
                undergroundTime += delta
                if (undergroundTime >= armTime) {
                    // 时间到，开始出土
                    state = PotatoMineState.POPPING
                    currentAnimation = poppingAnimation
                    stateTime = 0f
                }
            }
            PotatoMineState.POPPING -> {
                if (poppingAnimation.isAnimationFinished(stateTime)) {
                    // 出土完成，进入待命状态
                    state = PotatoMineState.IDLE
                    currentAnimation = idleAnimation
                    stateTime = 0f
                }
            }
            PotatoMineState.IDLE -> {
                // 检测附近是否有僵尸
                val nearbyZombie = gameScreen.zombies.find { zombie ->
                    zombie.isAlive() &&
                    zombie.row == this.row &&
                    abs(zombie.x - this.x) < 40f // 检测范围
                }

                if (nearbyZombie != null && !hasExploded) {
                    // 发现僵尸，爆炸！
                    explode()
                }
            }
            PotatoMineState.EXPLODING -> {
                if (explosionAnimation.isAnimationFinished(stateTime)) {
                    // 爆炸动画结束，移除自己
                    state = PotatoMineState.DEAD
                }
            }
            PotatoMineState.DEAD -> {
                receiveDamage(10000)
            }
        }
    }

    private fun explode() {
        hasExploded = true
        state = PotatoMineState.EXPLODING
        currentAnimation = explosionAnimation
        stateTime = 0f

        // 播放爆炸音效
        com.pvz.vidar.game.wsdx233.top.Assets.potatoMineSound.play()

        // 触发屏幕震动效果 (持续0.3秒，强度5.0)
        gameScreen.shakeScreen(0.3f, 5.0f)

        // 对附近范围内的所有僵尸造成伤害
        gameScreen.zombies.forEach { zombie ->
            if (zombie.isAlive() &&
                zombie.row == this.row &&
                abs(zombie.x - this.x) < 60f) { // 爆炸范围
                zombie.receiveDamage(200)
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch.color = color

        val frame = currentAnimation.getKeyFrame(stateTime, true)

        // 根据不同状态调整绘制位置和大小
        val scale = when (state) {
            PotatoMineState.UNDERGROUND -> 1.5f
            PotatoMineState.POPPING -> 1.5f
            PotatoMineState.IDLE -> 1.5f
            PotatoMineState.EXPLODING -> 1.5f
            PotatoMineState.DEAD -> 1.5f
        }

        batch.draw(
            frame,
            x + 30f - frame.regionWidth.toFloat() * scale / 2,
            y,
            frame.regionWidth.toFloat() * scale,
            frame.regionHeight.toFloat() * scale
        )
    }
}
