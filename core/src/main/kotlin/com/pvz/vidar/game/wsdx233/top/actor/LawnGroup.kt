package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.utils.Array

class LawnGroup : Group() {

    companion object {
        const val LAWN_START_X = 145f
        const val LAWN_WIDTH = 440f
        const val LAWN_START_Y = 20f
        const val LAWN_HEIGHT = 300f
        const val LAWN_END_X = LAWN_START_X + LAWN_WIDTH
        const val LAWN_END_Y = LAWN_START_Y + LAWN_HEIGHT
        const val CELL_WIDTH = LAWN_WIDTH / 9
        const val CELL_HEIGHT = LAWN_HEIGHT / 5
    }

    private val shapeRenderer = ShapeRenderer()
    var highlightX: Int? = null
    var highlightY: Int? = null
    var showHighlight = false
    var shovelMode = false  // 是否为铲子模式

    val zombies = Array<Zombie>()
    val plants = Array<Plant>()
    private var zombieDeathListener: ZombieDeathListener? = null

    fun setZombieDeathListener(listener: ZombieDeathListener) {
        zombieDeathListener = listener
    }

    override fun addActor(actor: Actor?) {
        super.addActor(actor)
        when (actor) {
            is Zombie -> zombies.add(actor)
            is Plant -> plants.add(actor)
        }
    }

    override fun removeActor(actor: Actor?): Boolean {
        val removed = super.removeActor(actor)
        if (removed) {
            when (actor) {
                is Zombie -> zombies.removeValue(actor, true)
                is Plant -> plants.removeValue(actor, true)
            }
        }
        return removed
    }


    override fun act(delta: Float) {
        super.act(delta)

        for (i in 0 until zombies.size) {
            val zombie = zombies[i]
            var isAttacking = false
            for (j in 0 until plants.size) {
                val plant = plants[j]
                if (zombie.row == plant.row && zombie.collisionBox.overlaps(plant.collisionBox)) {
                    zombie.attack(plant)
                    isAttacking = true
                    break
                }
            }
            if (!isAttacking && zombie.state == ZombieState.ATTACKING) {
                zombie.state = ZombieState.MOVING
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        // 深度排序：按y坐标（小的在前）和x坐标（小的在前）排序
        children.sort { a, b ->
            val yCompare = a.y.compareTo(b.y)
            if (yCompare != 0) yCompare else a.x.compareTo(b.x)
        }

        batch.end()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.transformMatrix = batch.transformMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        if (showHighlight && highlightX != null && highlightY != null) {
            // 植物模式：只在没有植物时显示高亮
            // 铲子模式：只在有植物时显示高亮
            val shouldShowHighlight = if (shovelMode) {
                hasPlantAt(highlightX!!, highlightY!!)
            } else {
                !hasPlantAt(highlightX!!, highlightY!!)
            }

            if (shouldShowHighlight) {
                shapeRenderer.color = Color(1f, 1f, 1f, 0.3f)
                shapeRenderer.rect(LAWN_START_X + highlightX!! * CELL_WIDTH, LAWN_START_Y, CELL_WIDTH, LAWN_HEIGHT)
                shapeRenderer.rect(LAWN_START_X, LAWN_START_Y + highlightY!! * CELL_HEIGHT, LAWN_WIDTH, CELL_HEIGHT)
            }
        }
        shapeRenderer.end()
        batch.begin()
        super.draw(batch, parentAlpha)
    }

    fun setHighlight(lawnX: Int, lawnY: Int) {
        highlightX = lawnX
        highlightY = lawnY
    }

    fun isWithin(x: Float, y: Float): Boolean {
        return x in LAWN_START_X..LAWN_END_X && y in LAWN_START_Y..LAWN_END_Y
    }

    fun getLawnX(x: Float): Int {
        return ((x - LAWN_START_X) / CELL_WIDTH).toInt()
    }

    fun getLawnY(y: Float): Int {
        return ((y - LAWN_START_Y) / CELL_HEIGHT).toInt()
    }

    fun hasPlantAt(lawnX: Int, lawnY: Int): Boolean {
        return plants.any { it.lawnX == lawnX && it.lawnY == lawnY }
    }

    fun getPlantAt(lawnX: Int, lawnY: Int): Plant? {
        return plants.firstOrNull { it.lawnX == lawnX && it.lawnY == lawnY }
    }
}
