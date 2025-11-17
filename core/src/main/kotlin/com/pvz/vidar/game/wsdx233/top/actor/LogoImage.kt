package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.utils.Align

class LogoImage : Image(Texture(Gdx.files.internal("logo.png"))) {
    init {
        setSize(160f, 240f)
        setPosition(200f,180f, Align.center)
        // 设置锚点为中心
        setOrigin(Align.center)
        // 添加动作
        addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.1f, 1.1f, 0.9f),
                    Actions.rotateTo(5f, 0.6f),
                    Actions.rotateTo(-5f, 1.4f),
                    Actions.rotateTo(0f,0.6f),
                    Actions.scaleTo(1.0f, 1.0f, 0.9f)
                )
            )
        )
    }

}
