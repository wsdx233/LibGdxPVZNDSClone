package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Array
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen
import kotlin.random.Random

enum class PeashooterState {
    IDLE,
    SHOOTING
}

class Peashooter(gameScreen: GameScreen) : Plant(gameScreen) {
    private val textureAtlas = TextureAtlas("game/actor/peashooter/peashooter.atlas")
    private var currentAnimation: Animation<TextureAtlas.AtlasRegion>
    private var stateTime = 0f
    private var shootTime = 0f
    private var shootCooldown = Random.nextFloat() * 0.3f + 2f // Random between 2.0 and 2.3

    var state = PeashooterState.IDLE

    private val idleAnimation: Animation<TextureAtlas.AtlasRegion>
    private val shootAnimation: Animation<TextureAtlas.AtlasRegion>

    init {
        val idleFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..7) {
            idleFrames.add(textureAtlas.findRegion("idle", i))
        }
//        for (i in 6 downTo 1) {
//            idleFrames.add(textureAtlas.findRegion("idle", i))
//        }
        idleAnimation = Animation(0.25f, idleFrames, Animation.PlayMode.LOOP)

        val shootFrames = Array<TextureAtlas.AtlasRegion>()
        shootFrames.add(textureAtlas.findRegion("shoot", 0))
        shootFrames.add(textureAtlas.findRegion("shoot", 1))
        shootFrames.add(textureAtlas.findRegion("shoot", 2))
        shootAnimation = Animation(0.25f, shootFrames, Animation.PlayMode.NORMAL)

        currentAnimation = idleAnimation
        width = 28f
        height = 32f
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta
        shootTime += delta


        val hasZombieInLane = gameScreen.zombies.any { (it.row == this.row && it.isAlive() && it.x >= this.x && it.x < gameScreen.stage.width) }

        if (hasZombieInLane && shootTime > shootCooldown) {
            state = PeashooterState.SHOOTING
            stateTime = 0f
            shootTime = 0f
        }

        when (state) {
            PeashooterState.IDLE -> {
                currentAnimation = idleAnimation
            }
            PeashooterState.SHOOTING -> {
                currentAnimation = shootAnimation
                if (shootAnimation.isAnimationFinished(stateTime)) {
                    Assets.throwSound.play()
                    val pea = Pea(gameScreen)
                    pea.setPosition(x + 35, y + 10)
                    pea.row = this.row
                    gameScreen.addPea(pea)
                    state = PeashooterState.IDLE
                    stateTime = 0f
                    shootCooldown = Random.nextFloat() * 0.3f + 2f // Generate new random cooldown
                }
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        batch.color = color

        val index = currentAnimation.getKeyFrameIndex(stateTime)
        var drawX = x + 10f
        val offsets = listOf(0f,2f,5f,2f,1f,-1f,-2f,-2f,1f,2f)
        val offsetsFire = listOf(0f,3f,2f)
        drawX += if (currentAnimation == idleAnimation){
            offsets[index]
        } else {
            offsetsFire[index]
        }


        val frame = currentAnimation.getKeyFrame(stateTime, true)
        batch.draw(frame, drawX, y, frame.regionWidth.toFloat()*1.5f, frame.regionHeight.toFloat()*1.5f)
    }
}
