package com.pvz.vidar.game.wsdx233.top.actor

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.pvz.vidar.game.wsdx233.top.Assets
import kotlin.reflect.KClass

data class Seed(
    val plantClass: KClass<out Plant>,
    val sunCost: Int,
    val coolDownTime: Float,
    val cardTexture: TextureAtlas.AtlasRegion,
    val plantTexture: TextureAtlas.AtlasRegion
)

object SeedManager {
    val allSeeds = listOf(
        Seed(
            plantClass = Sunflower::class,
            sunCost = 50,
            coolDownTime = 7.5f,
            cardTexture = Assets.sunflowerTexture.findRegion("seeds",0),

            plantTexture = Assets.sunflowerTexture.findRegion("idle",1)
        ),
        Seed(
            plantClass = Peashooter::class,
            sunCost = 100,
            coolDownTime = 4.5f,
            cardTexture = Assets.peashooterTexture.findRegion("seed",0),
            plantTexture = Assets.peashooterTexture.findRegion("idle",1)
        ),
        Seed(
            plantClass = PotatoMine::class,
            sunCost = 25,
            coolDownTime = 30f,
            cardTexture = Assets.potatoMineTexture.findRegion("seed",0),
            plantTexture = Assets.potatoMineTexture.findRegion("idle",1)
        ),
        Seed(
            plantClass = WallNut::class,
            sunCost = 50,
            coolDownTime = 30f,
            cardTexture = Assets.wallNutTexture.findRegion("seed",0),
            plantTexture = Assets.wallNutTexture.findRegion("idle",0)
        )
    )
}
