package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.utils.Array
import com.pvz.vidar.game.wsdx233.top.Assets

enum class ZombieState {
    IDLE,
    MOVING,
    ATTACKING,
    DYING,
    DYING_EAT,
    DEAD
}

interface ZombieDeathListener {
    fun onZombieDied(zombie: Zombie)
}

class Zombie(var row: Int) : Actor() {
    private val textureAtlas = TextureAtlas("game/actor/zombie/zombie.atlas")
    private var currentAnimation: Animation<TextureAtlas.AtlasRegion>
    private var stateTime = 0f
    private var isHalfHealth = false

    var hp = 250 // 僵尸的生命值
    var state = ZombieState.MOVING // 僵尸的初始状态
    private var targetPlant: Plant? = null
    private var attackCooldown = 0f

    private val idleAnimation: Animation<TextureAtlas.AtlasRegion>
    private val walkAnimation: Animation<TextureAtlas.AtlasRegion>
    private val walkHalfAnimation: Animation<TextureAtlas.AtlasRegion>
    private val eatAnimation: Animation<TextureAtlas.AtlasRegion>
    private val eatHalfAnimation: Animation<TextureAtlas.AtlasRegion>
    private val dieAnimation: Animation<TextureAtlas.AtlasRegion>
    private val walkDieAnimation: Animation<TextureAtlas.AtlasRegion>
    private val dieEatAnimation: Animation<TextureAtlas.AtlasRegion>

    val collisionBox = Rectangle(x + 25, y, 55f, 75f)
    private val attackBox = Rectangle(x, y, 10f, 75f)

    private val chompSound1: Sound = Gdx.audio.newSound(Gdx.files.internal("fx/chomp.mp3"))
    private val chompSound2: Sound = Gdx.audio.newSound(Gdx.files.internal("fx/chomp.mp3"))
    private val gulpSound: Sound = Gdx.audio.newSound(Gdx.files.internal("fx/gulp.mp3"))
    private var chompCounter = 0

    private var deathListener: ZombieDeathListener? = null

    var flashTime = 0f
    private var fallingSoundPlayed = false

    private val shapeRenderer = ShapeRenderer()



    init {
        val idleFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..4) {
            idleFrames.add(textureAtlas.findRegion("idle$i"))
        }
        idleAnimation = Animation(.25f, idleFrames, Animation.PlayMode.LOOP)

        val walkFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..6) {
            walkFrames.add(textureAtlas.findRegion("walk$i"))
        }
        walkAnimation = Animation(.25f, walkFrames, Animation.PlayMode.LOOP)

        val walkHalfFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..6) {
            walkHalfFrames.add(textureAtlas.findRegion("walk_half$i"))
        }
        walkHalfAnimation = Animation(.25f, walkHalfFrames, Animation.PlayMode.LOOP)

        val eatFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..6) {
            eatFrames.add(textureAtlas.findRegion("eat$i"))
        }
        eatAnimation = Animation(.25f, eatFrames, Animation.PlayMode.LOOP)

        val eatHalfFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..6) {
            eatHalfFrames.add(textureAtlas.findRegion("eat_half$i"))
        }
        eatHalfAnimation = Animation(.25f, eatHalfFrames, Animation.PlayMode.LOOP)


        val walkDieFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..6) {
            walkDieFrames.add(textureAtlas.findRegion("walk_die$i"))
        }
        walkDieAnimation = Animation(.25f, walkDieFrames, Animation.PlayMode.NORMAL)

        val dieEatFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..4) {
            dieEatFrames.add(textureAtlas.findRegion("die_eat$i"))
        }
        dieEatAnimation = Animation(.25f, dieEatFrames, Animation.PlayMode.NORMAL)

        val dieFrames = Array<TextureAtlas.AtlasRegion>()
        for (i in 0..8) {
            dieFrames.add(textureAtlas.findRegion("die",i))
        }
        dieAnimation = Animation(.25f, dieFrames, Animation.PlayMode.NORMAL)

        currentAnimation = walkAnimation
    }

    fun setDeathListener(listener: ZombieDeathListener) {
        deathListener = listener
    }

    override fun act(delta: Float) {
        super.act(delta)
        stateTime += delta
        attackCooldown -= delta

        if (flashTime > 0){
            flashTime -= delta
            if (flashTime < 0) flashTime = 0f
        }


        if (hp < 100 && state != ZombieState.DEAD && state != ZombieState.DYING && state != ZombieState.DYING_EAT) {

            val headTexture = textureAtlas.findRegion("head0")
            val drop = ZombieDrop(headTexture,x+this.width/2+10f,y+this.height/2+40f,Math.random().toFloat() * 50f,100f,-100f,y-5f)
            drop.width *= 1.5f
            drop.height *= 1.5f
            parent.addActor(drop)
            Assets.popSound.play()

            if (state == ZombieState.ATTACKING) {
                state = ZombieState.DYING_EAT
                currentAnimation = dieEatAnimation
                stateTime = 0f
            } else if (state == ZombieState.MOVING) {
                state = ZombieState.DYING
                currentAnimation = walkDieAnimation
                stateTime = 0f
            }
        }

        when (state) {
            ZombieState.IDLE -> {
                currentAnimation = idleAnimation
            }
            ZombieState.MOVING -> {
                x -= 13 * delta
                currentAnimation = if (isHalfHealth) walkHalfAnimation else walkAnimation
            }
            ZombieState.ATTACKING -> {
                currentAnimation = if (isHalfHealth) eatHalfAnimation else eatAnimation
                if (targetPlant != null && targetPlant!!.hp > 0) {
                    if (attackCooldown <= 0) {
                        if (chompCounter % 2 == 0) {
                            chompSound1.play()
                        } else {
                            chompSound2.play()
                        }
                        chompCounter++
                        targetPlant!!.receiveDamage(40) // Damage per attack
                        attackCooldown = 1f // Attack every 1 second
                    }

                    if(targetPlant!!.hp <= 0) {
                        chompSound1.stop()
                        chompSound2.stop()
                        gulpSound.play()
                        state = ZombieState.MOVING
                        targetPlant = null
                    }
                }
            }
            ZombieState.DYING_EAT -> {
                if (dieEatAnimation.isAnimationFinished(stateTime)) {
                    die()
                }
            }
            ZombieState.DYING -> {
                x -= 10 * delta
                if (walkDieAnimation.isAnimationFinished(stateTime)) {
                    die()
                }
            }
            ZombieState.DEAD -> {
                if (dieAnimation.isAnimationFinished(stateTime)) {
                    if (!fallingSoundPlayed) {
                        Assets.fallingSound.play()
                        fallingSoundPlayed = true
                    }
                    addAction(Actions.sequence(Actions.fadeOut(0.5f), Actions.run {
                        deathListener?.onZombieDied(this)
                        remove()
                    }))
                }
            }
        }

        collisionBox.x = x + 25
        collisionBox.y = y
        attackBox.x = x
        attackBox.y = y
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // 绘制椭圆形阴影
        batch.end()

        if (state != ZombieState.DEAD){
            shapeRenderer.projectionMatrix = batch.projectionMatrix
            shapeRenderer.transformMatrix = batch.transformMatrix
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

            // 半透明黑色阴影
            shapeRenderer.color = Color(0f, 0f, 0f, 0.3f)
            // 在僵尸底部绘制椭圆
            shapeRenderer.ellipse(x + 30f, y , 20f, 12f)

            shapeRenderer.end()
        }
        batch.begin()

        var looping = true
        if (currentAnimation == dieAnimation) {
            looping = false
        }
        val frame = currentAnimation.getKeyFrame(stateTime, looping)

        var drawX = x
        drawX += if (currentAnimation == dieAnimation){
            -11f
        } else if(currentAnimation == walkDieAnimation){
            11f
        } else {
            0f
        }

        val actorColor = this.color

        if (flashTime > 0){
            // 当闪白时，我们使用固定的0.8 alpha，但仍然乘以父actor的alpha
            batch.setColor(1f, 1f, 1f, 0.8f * parentAlpha)
        } else {
            // 正常情况下，使用actor自身的颜色，并乘以父actor的alpha
            batch.setColor(actorColor.r, actorColor.g, actorColor.b, actorColor.a * parentAlpha)
        }

        batch.draw(frame, drawX, y, frame.regionWidth.toFloat() * 1.5f, frame.regionHeight.toFloat() * 1.5f)

    }

    fun attack(plant: Plant) {
        if (state == ZombieState.MOVING) {
            state = ZombieState.ATTACKING
            targetPlant = plant
        }
    }

    fun receiveDamage(damage: Int) {
        hp -= damage
        if (hp <= 0) {
            die()
        } else if (hp < 175) {
            setHalfHealth()
        }

        Assets.splatSound.play()

        flashTime = 0.2f
    }

    private fun setHalfHealth() {
        if (!isHalfHealth) {
            isHalfHealth = true

            val handTexture = textureAtlas.findRegion("hand0")
            val drop = ZombieDrop(handTexture,x+this.width/2+20f,y+this.height/2+30f,Math.random().toFloat() * 70f - 35f,20f,-500f,y-5f)
            drop.width *= 1.5f
            drop.height *= 1.5f
            parent.addActor(drop)
            Assets.popSound.play()


            if (state == ZombieState.MOVING) {
                currentAnimation = walkHalfAnimation
            } else if (state == ZombieState.ATTACKING) {
                currentAnimation = eatHalfAnimation
            }
        }
    }

    private fun die() {
        if (state != ZombieState.DEAD) {



            state = ZombieState.DEAD
            currentAnimation = dieAnimation
            stateTime = 0f
        }
    }

    fun isAlive(): Boolean {
        return state != ZombieState.DEAD
    }
}
