package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

interface PlantDeathListener {
    fun onPlantDied(plant: Plant)
}

open class Plant(protected val gameScreen: GameScreen) : Actor() {
    var hp: Int = 300
    var row: Int = 0
    var lawnX: Int = 0
    var lawnY: Int = 0
    val collisionBox = Rectangle(x, y, 60f, 70f)
    private var deathListener: PlantDeathListener? = null

    private val shapeRenderer = ShapeRenderer()

    override fun act(delta: Float) {
        super.act(delta)
        collisionBox.x = x
        collisionBox.y = y
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // 绘制椭圆形阴影
        batch.end()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.transformMatrix = batch.transformMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        // 半透明黑色阴影
        shapeRenderer.color = Color(0f, 0f, 0f, 0.3f)
        // 在植物底部绘制椭圆，椭圆中心在植物底部中央
        shapeRenderer.ellipse(x + 20f, y , 20f, 14f)

        shapeRenderer.end()
        batch.begin()

        super.draw(batch, parentAlpha)
    }

    fun setDeathListener(listener: PlantDeathListener) {
        deathListener = listener
    }

    fun receiveDamage(damage: Int) {
        hp -= damage
        if (hp <= 0) {
            deathListener?.onPlantDied(this)
            remove()
        }
    }
}
