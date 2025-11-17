package com.pvz.vidar.game.wsdx233.top

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeType
import com.badlogic.gdx.scenes.scene2d.ui.Skin

class Assets {
    companion object {
        val skin = Skin(Gdx.files.internal("skins/craftacular/skin/craftacular-ui.json"))
        val skin_dark = Skin(Gdx.files.internal("ui/uiskin.json"))

        val day = Texture(Gdx.files.internal("game/day.png"))

        val dayTopBg = TextureRegion(day,451,2,256,192)

        val introMusic: Music = Gdx.audio.newMusic(Gdx.files.internal("music/intro.mp3"))
        val grassMusic: Music = Gdx.audio.newMusic(Gdx.files.internal("music/grass.mp3"))
        val chooseYourSeedMusic : Music = Gdx.audio.newMusic(Gdx.files.internal("music/choose_your_seed.mp3"))

        val zombieAtlas = TextureAtlas(Gdx.files.internal("game/actor/zombie/zombie.atlas"))

        val sun_frame_1 = Texture(Gdx.files.internal("game/actor/sun/sun_0.png"))
        val sun_frame_2 = Texture(Gdx.files.internal("game/actor/sun/sun_1.png"))
        val sunIcon = Texture(Gdx.files.internal("ui/sun_icon.png"))

        val font = BitmapFont()

        val sunflowerTexture = TextureAtlas("game/actor/sunflower/sunflower.atlas")
        val peashooterTexture = TextureAtlas("game/actor/peashooter/peashooter.atlas")
        val potatoMineTexture = TextureAtlas("game/actor/potato_mine/potato_mine.atlas")
        val wallNutTexture = TextureAtlas("game/actor/wall_nut/wall_nut.atlas")

        val daveAtlas = TextureAtlas(Gdx.files.internal("game/actor/dave/dave.atlas"))

        val soil = TextureAtlas(Gdx.files.internal("game/actor/soil/soil.atlas"))

        val progressBarAtlas = TextureAtlas(Gdx.files.internal("game/actor/progress_bar/progress_bar.atlas"))

        val shovelAtlas = TextureAtlas(Gdx.files.internal("game/actor/shovel/shovel.atlas"))

        val plantSound = Gdx.audio.newSound(Gdx.files.internal("fx/plant.mp3"))
        val groanSound = Gdx.audio.newSound(Gdx.files.internal("fx/groan.mp3"))
        val readysetplantSound = Gdx.audio.newSound(Gdx.files.internal("fx/readysetplant.mp3"))
        val seedliftSound = Gdx.audio.newSound(Gdx.files.internal("fx/seedlift.mp3"))
        val splatSound = Gdx.audio.newSound(Gdx.files.internal("fx/splat.mp3"))
        val throwSound = Gdx.audio.newSound(Gdx.files.internal("fx/throw.mp3"))
        val awoogaSound = Gdx.audio.newSound(Gdx.files.internal("fx/awooga.mp3"))
        val evillaughSound = Gdx.audio.newSound(Gdx.files.internal("fx/evillaugh.mp3"))
        val gravebuttonSound = Gdx.audio.newSound(Gdx.files.internal("fx/gravebutton.mp3"))
        val potatoMineSound = Gdx.audio.newSound(Gdx.files.internal("fx/potato_mine.mp3"))
        val finalwaveSound = Gdx.audio.newSound(Gdx.files.internal("fx/finalwave.mp3"))
        val sirenSound = Gdx.audio.newSound(Gdx.files.internal("fx/siren.mp3"))
        val screamSound = Gdx.audio.newSound(Gdx.files.internal("fx/scream.mp3"))
        val popSound = Gdx.audio.newSound(Gdx.files.internal("fx/pop.mp3"))
        val fallingSound = Gdx.audio.newSound(Gdx.files.internal("fx/falling.mp3"))
        val losemusicSound = Gdx.audio.newMusic(Gdx.files.internal("fx/losemusic.mp3"))


    }

}
