package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen

class Shovel(private val gameScreen: GameScreen) : Actor() {

    private val emptySlotTexture: TextureRegion = Assets.shovelAtlas.findRegion("empty_slot")
    private val inSlotTexture: TextureRegion = Assets.shovelAtlas.findRegion("inslot")
    private val shovelTexture: TextureRegion = Assets.shovelAtlas.findRegion("shovel")

    var isSelected = false
        private set

    init {
        setSize(emptySlotTexture.regionWidth.toFloat() * 2f, emptySlotTexture.regionHeight.toFloat() * 2f)

        addListener(object : InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                if (!isSelected) {
                    selectShovel()
                }
                return true
            }
        })
    }

    private fun selectShovel() {
        isSelected = true
        gameScreen.selectShovel()

        // 设置铲子光标
        val texture = shovelTexture.texture
        if (!texture.textureData.isPrepared) {
            texture.textureData.prepare()
        }
        val pixmap = texture.textureData.consumePixmap()
        val cursorPixmap = Pixmap(128, 128, Pixmap.Format.RGBA8888)
        cursorPixmap.drawPixmap(
            pixmap,
            shovelTexture.regionX, shovelTexture.regionY,
            shovelTexture.regionWidth, shovelTexture.regionHeight,
            0, 0, 128, 128
        )
        val cursor = Gdx.graphics.newCursor(cursorPixmap, 32, 32)
        Gdx.graphics.setCursor(cursor)
        cursorPixmap.dispose()
        pixmap.dispose()
    }

    fun deselectShovel() {
        isSelected = false
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        super.draw(batch, parentAlpha)

        val texture = if (isSelected) emptySlotTexture else inSlotTexture
        batch.draw(texture, x, y, width, height)
    }
}
