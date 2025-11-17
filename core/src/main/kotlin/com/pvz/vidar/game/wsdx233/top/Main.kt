package com.pvz.vidar.game.wsdx233.top

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.viewport.FitViewport
import com.pvz.vidar.game.wsdx233.top.screen.FlagScreen
import com.pvz.vidar.game.wsdx233.top.screen.GameScreen
import com.pvz.vidar.game.wsdx233.top.screen.TitleScreen
import ktx.app.KtxGame
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile
import ktx.async.KtxAsync
import ktx.graphics.use

class Main : KtxGame<KtxScreen>() {
    override fun create() {
        KtxAsync.initiate()

        addScreen(TitleScreen(this))
        addScreen(GameScreen(this))
//        addScreen(FlagScreen(this))
        setScreen<TitleScreen>()

    }
}
