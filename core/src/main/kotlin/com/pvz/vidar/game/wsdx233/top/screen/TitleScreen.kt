package com.pvz.vidar.game.wsdx233.top.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.Main
import com.pvz.vidar.game.wsdx233.top.actor.LogoImage
import ktx.actors.setScrollFocus
import ktx.app.KtxScreen

class TitleScreen(private val game: Main) : KtxScreen {
    val stage = Stage(ExtendViewport(640f,360f))
    val dayBgImage = Image(Assets.dayTopBg)

    var rollSpeedX = .05f
    var rollSpeedY = .02f
    var bgX = .0f
    var bgY = .0f

    private var isTransitioning = false
    private var transitionTimer = 0f
    private lateinit var btnStart: TextButton
    private lateinit var btnHelp: TextButton
    private lateinit var btnQuit: TextButton

    override fun show() {
        // 清空stage，避免重复添加actors
        stage.clear()

        // 重置过渡状态
        isTransitioning = false
        transitionTimer = 0f

        Assets.introMusic.isLooping = true
        Assets.introMusic.play()

        dayBgImage.apply {
            setSize(960f,640f)
            setPosition(0f,0f)
        }
        stage.addActor(dayBgImage)


        val logoImage = LogoImage()
        stage.addActor(logoImage)

        btnQuit = TextButton("Quit",(Assets.skin))
        btnQuit.apply {
            setSize(240f,60f)
            setPosition(340f,75f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (!isTransitioning) Gdx.app.exit()
                }

            })
        }


        btnHelp = TextButton("Help",(Assets.skin))
        btnHelp.apply {
            setSize(240f,60f)
            setPosition(340f,150f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (isTransitioning) return
                    val helpDialog = object : Dialog("Help", Assets.skin) {
                        override fun result(obj: Any?) {
                            // This method is called when a button is clicked
                            // For a simple close button, we don't need to do anything here
                        }
                    }

                    val helpContent = """Help for Plants and Zombies Game
When the Zombies show up, just sit there and don't do anything.
You win the game when the Zombies get to your houze.""".trimIndent()

                    val contentLabel = Label(helpContent, Assets.skin)
                    contentLabel.setWrap(true) // Enable text wrapping

                    val scrollPane = ScrollPane(contentLabel, Assets.skin)
                    scrollPane.setFadeScrollBars(false)
                    scrollPane.setScrollingDisabled(true, false) // Disable horizontal scrolling

                    helpDialog.text("").padTop(20f) // Clear default text and add padding
                    helpDialog.getContentTable().add(scrollPane).width(400f).height(200f).pad(10f).row()
                    helpDialog.button("Close", true) // Add a close button with a return value of true
                    helpDialog.key(com.badlogic.gdx.Input.Keys.ESCAPE, false) // Close on escape key

                    helpDialog.show(stage)
                    contentLabel.setScrollFocus(true)
                }

            })
        }


        btnStart = TextButton("Start",(Assets.skin))
        btnStart.apply {
            setSize(240f,60f)
            setPosition(340f,230f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    if (isTransitioning) return
                    isTransitioning = true
                    transitionTimer = 0f
                    Assets.evillaughSound.play()
                }

            })
        }


        stage.addActor(btnStart)
        stage.addActor(btnHelp)
        stage.addActor(btnQuit)


        Gdx.input.inputProcessor = stage

        val label = Label("a simple pvz nds clone game for hgame final by @wsdx233 2025",Assets.skin)
        label.apply {
            setFontScale(0.5f)
            setSize(640f,50f)
            setAlignment(Align.center)
            setColor(1f,1f,1f,.7f)
        }
        stage.addActor(label)


        super.show()
    }

    override fun render(delta: Float) {
        if (isTransitioning) {
            transitionTimer += delta

            val t = transitionTimer * 3f
            btnStart.color.a = kotlin.math.sin(t * t) * 0.5f + 0.5f

            if (transitionTimer >= 3f) {
                Assets.introMusic.stop()
                game.setScreen<GameScreen>()
                return
            }
        }

        stage.apply {
            act(delta)
            draw()
        }

        bgX += rollSpeedX
        bgY += rollSpeedY

        if (bgX !in 0.0..160.0) {
            rollSpeedX *= -1
        }

        if (bgY !in 0.0..140.0) {
            rollSpeedY *= -1
        }

        dayBgImage.setPosition(-bgX,-bgY)

    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, false)
        super.resize(width, height)
    }
}
