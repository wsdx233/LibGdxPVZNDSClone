package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

/**
 * 植物卡片
 * @author vidar
 */
class Card(private val gameScreen: GameScreen, val seed: Seed) : Group() {
    var isCoolingDown = false
        private set
    private var coolDownCounter = 0f

    private val cardImage: Image
    private val progressTexture: Texture

    init {
        setSize(seed.cardTexture.originalWidth.toFloat() * 2f, seed.cardTexture.originalHeight.toFloat() * 2f)

        cardImage = Image(seed.cardTexture)
        cardImage.setSize(width, height)
        addActor(cardImage)

        val gradientPixmap = Pixmap(100, 1, Pixmap.Format.RGBA8888)
        val startColor = Color.valueOf("33FF336A")
        val endColor = Color.valueOf("00AA006A")
        for (i in 0 until 100) {
            gradientPixmap.setColor(startColor.cpy().lerp(endColor, i / 99f))
            gradientPixmap.drawPixel(i, 0)
        }
        progressTexture = Texture(gradientPixmap)
        gradientPixmap.dispose()

        addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                if (!isCoolingDown && gameScreen.sun >= seed.sunCost) {
                    Assets.seedliftSound.play()
                    gameScreen.selectCard(this@Card)
                }
            }
        })
    }

    fun startCooldown() {
        isCoolingDown = true
        coolDownCounter = seed.coolDownTime
    }

    override fun act(delta: Float) {
        super.act(delta)
        if (isCoolingDown) {
            coolDownCounter -= delta
            if (coolDownCounter <= 0) {
                isCoolingDown = false
            }
        }
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        // 检查是否被选中
        val isSelected = gameScreen.selectedCard == this

        if(gameScreen.sun < this.seed.sunCost){
            cardImage.color = Color(.7f, .7f, .7f, 1f)
        } else if (isSelected) {
            // 被选中时设置透明度为0.8
            cardImage.color = Color(1f, 1f, 1f, .8f)
        } else {
            cardImage.color = Color(1f, 1f, 1f, 1f)
        }

        if (isCoolingDown) {
            cardImage.color = Color(.5f, .5f, .5f, 1f)
            val progress = 1f - coolDownCounter / seed.coolDownTime
            val barHeight = 8f

            batch.draw(progressTexture, x+15, y+7, (width-30) * progress, barHeight)
        }
    }
}
