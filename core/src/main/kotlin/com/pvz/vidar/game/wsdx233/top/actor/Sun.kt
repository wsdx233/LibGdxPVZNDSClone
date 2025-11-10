package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.actions.Actions.run
import com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

class Sun(
    private val gameScreen: GameScreen,
    private val sunType: SunType,
    startX: Float,
    startY: Float,
    targetY: Float = 50f  // 目标地面高度
) : Actor() {

    enum class SunType {
        FROM_SUNFLOWER,  // 向日葵生成的阳光（抛物线运动）
        FROM_SKY         // 从天空掉落的阳光（直线下落）
    }

    private val sunTextures = arrayOf(
        Assets.sun_frame_1,
        Assets.sun_frame_2
    )
    val points = Gdx.audio.newSound(Gdx.files.internal("fx/points.mp3"))
    private val animation = Animation(0.1f, *sunTextures)
    private var stateTime = 0f

    var picked = false

    // 运动状态
    private var isMoving = true
    private var velocityY = 0f
    private var velocityX = 0f
    private val gravity = -120f  // 重力加速度（缓慢）
    private var groundY = targetY  // 地面高度

    // 计时器用于10秒后消失
    private var lifeTime = 0f
    private val maxLifeTime = 10f

    init {
        setSize(50f, 50f)
        setPosition(startX, startY)
        color.a = 0.75f

        // 根据类型初始化运动参数
        when (sunType) {
            SunType.FROM_SUNFLOWER -> {
                // 向日葵生成：抛物线运动
                // 初始向上的速度，使其先上升再下落
                velocityY = 40f
                velocityX = 40f * Math.random().toFloat() - 20f
            }
            SunType.FROM_SKY -> {
                // 从天空掉落：缓慢直线下落
                velocityY = -40f  // 缓慢下降的速度
                velocityX = 0f
            }
        }

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (picked) return false
                points.play()

                picked = true
                gameScreen.sun += 25
                color.a = 0.6f
                this@Sun.addAction(sequence(Actions.moveTo(0f,320f,0.5f),Actions.fadeOut(0.5f), Actions.run { this@Sun.remove() }))
                return true
            }
        })
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta

        // 如果已被捡起，不处理后续逻辑
        if (picked) return

        // 增加生命计时
        lifeTime += delta

        // 10秒后开始淡出消失
        if (lifeTime >= maxLifeTime) {
            if (!hasActions()) {
                addAction(sequence(Actions.fadeOut(2f), Actions.run { this@Sun.remove() }))
            }
            return
        }

        // 运动逻辑
        if (isMoving) {
            when (sunType) {
                SunType.FROM_SUNFLOWER -> {
                    // 抛物线运动：应用重力
                    velocityY += gravity * delta

                    val newX = x + velocityX * delta
                    val newY = y + velocityY * delta

                    // 检查是否落地
                    if (newY <= groundY) {
                        setPosition(x, groundY)
                        isMoving = false
                        velocityY = 0f
                    } else {
                        setPosition(newX, newY)
                    }
                }
                SunType.FROM_SKY -> {
                    // 直线缓慢下落
                    val newY = y + velocityY * delta

                    // 检查是否落地
                    if (newY <= groundY) {
                        setPosition(x, groundY)
                        isMoving = false
                        velocityY = 0f
                    } else {
                        setPosition(x, newY)
                    }
                }
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)
        batch.draw(animation.getKeyFrame(stateTime, true), x, y, width, height)
    }
}
