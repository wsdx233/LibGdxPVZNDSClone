package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions

/**
 * 僵尸掉落物Actor
 * 用于处理僵尸死亡时掉落的物品效果（如帽子、装备等）
 *
 * @param texture 掉落物的纹理
 * @param startX 初始X位置
 * @param startY 初始Y位置
 * @param initialVelocityX 初始X方向速度
 * @param initialVelocityY 初始Y方向速度
 * @param initialAngularVelocity 初始角速度（度/秒）
 * @param targetY 最终掉落到的Y值
 */
class ZombieDrop(
    private val texture: TextureRegion,
    startX: Float,
    startY: Float,
    private val initialVelocityX: Float,
    private val initialVelocityY: Float,
    private val initialAngularVelocity: Float,
    private val targetY: Float
) : Actor() {

    // 运动状态
    private var isMoving = true
    private var velocityX = initialVelocityX
    private var velocityY = initialVelocityY
    private var angularVelocity = initialAngularVelocity

    // 重力加速度
    private val gravity = -500f  // 像素/秒²

    // 当前旋转角度
    private var rotation = 0f

    init {
        // 设置纹理大小
        setSize(texture.regionWidth.toFloat(), texture.regionHeight.toFloat())
        setPosition(startX, startY)
        setOrigin(width / 2, height / 2)  // 设置旋转中心为物体中心
    }

    override fun act(delta: Float) {
        super.act(delta)

        if (isMoving) {
            // 应用重力加速度
            velocityY += gravity * delta

            // 更新位置
            val newX = x + velocityX * delta
            val newY = y + velocityY * delta

            // 更新旋转角度
            rotation += angularVelocity * delta

            // 检查是否落地
            if (newY <= targetY) {
                setPosition(x, targetY)
                isMoving = false

                // 落地后开始淡出效果（0.3秒）
                addAction(
                    Actions.sequence(
                        Actions.fadeOut(0.3f),
                        Actions.run { this@ZombieDrop.remove() }
                    )
                )
            } else {
                setPosition(newX, newY)
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // 应用透明度
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha)

        // 绘制带旋转的纹理
        batch.draw(
            texture,
            x, y,                           // 位置
            originX, originY,               // 旋转中心
            width, height,                  // 尺寸
            scaleX, scaleY,                 // 缩放
            rotation,                       // 旋转角度
            false                           // 是否翻转
        )

        // 重置颜色
        batch.setColor(1f, 1f, 1f, 1f)
    }
}