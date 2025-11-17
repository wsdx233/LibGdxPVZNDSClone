package com.pvz.vidar.game.wsdx233.top.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.Main
import com.pvz.vidar.game.wsdx233.top.actor.*
import ktx.app.KtxScreen
import kotlin.random.Random

class GameScreen(private val game: Main) : KtxScreen {

    companion object {
        private const val TOTAL_GAME_DURATION = 300f // seconds
        private const val INITIAL_SPAWN_INTERVAL = 10f // seconds
        private const val FINAL_SPAWN_INTERVAL = 0.00001f // seconds
        private const val PREVIEW_DURATION = 2f // seconds
        private const val CAMERA_PREVIEW_X = 515f
        private const val CAMERA_MOVE_SPEED = 80f // pixels per second
        private const val FLASH_TEXT_DURATION = 0.5f // seconds per text
    }

    val gameBg = TextureRegion(Assets.day, 2, 2, 447, 192)
    val grassTexture = TextureRegion(Assets.day, 248, 242, 246, 169)

    val camera = OrthographicCamera()
    val stage = Stage(ExtendViewport(640f, 360f, camera))

    var sun = 50
    private val sunScoreLabel: Label
    private val sunIconImage: Image
    private val progressBar: ProgressBar

    private val cards = mutableListOf<Card>()
    var selectedCard: Card? = null
    private val shovel: Shovel
    private var shovelMode = false
    private val lawnGroup = LawnGroup()
    val zombies: com.badlogic.gdx.utils.Array<Zombie>
        get() = lawnGroup.zombies

    private var gameTime = 0f
    private var spawnTimer = 0f
    private val zombieSpawnList = listOf(
        1.0 to Zombie::class.java
    )

    // 阳光掉落定时器
    private var sunDropTimer = 0f
    private var nextSunDropInterval = Random.nextFloat() * 5f + 5f // 初始随机5-10秒

    // 游戏开始状态
    private var gameStarted = false
    private var previewTimer = 0f
    private var cameraMovingToPreview = true
    private var cameraReturning = false
    private var showingReadySetPlant = false
    private var readySetPlantTimer = 0f
    private var readySetPlantIndex = 0
    private val readySetPlantTexts = listOf("Ready...", "Set...", "Plant!")
    private var currentFlashText: String? = null
    private var musicTransitioned = false

    // 屏幕震动相关
    private var isShaking = false
    private var shakeTimer = 0f
    private var shakeDuration = 0f
    private var shakeIntensity = 0f
    private val originalCameraX = 320f

    // 最后一波相关
    private var finalWaveTriggered = false
    private var finalWaveTextTimer = 0f
    private var showingFinalWaveText = false
    private var firstZombieSpawned = false
    private var firstZombieTimer = 0f

    // 游戏失败相关
    private var gameLost = false
    private var zombieSpawnCount = 0

    // 游戏胜利相关
    private var gameWon = false

    // 击杀统计
    var zombieKillCount = 0


    private val plantDeathListener = object : PlantDeathListener {
        override fun onPlantDied(plant: Plant) {
            lawnGroup.removeActor(plant)
        }
    }

    private val zombieDeathListener = object : ZombieDeathListener {
        override fun onZombieDied(zombie: Zombie) {
            lawnGroup.removeActor(zombie)
            // 增加击杀计数（排除预览僵尸）
            if (zombie.state != ZombieState.IDLE) {
                zombieKillCount++
            }
        }
    }

    init {
        val labelStyle = Label.LabelStyle(Assets.font, null)
        sunScoreLabel = Label(sun.toString(), labelStyle)
        sunScoreLabel.setPosition(17f, 290f)  // 往下移动，并向右移动为图标留空间
        sunScoreLabel.setAlignment(Align.center)

        sunIconImage = Image(Assets.sunIcon)
        sunIconImage.setSize(32f, 32f)
        sunIconImage.setPosition(10f, 310f)  // 在文字上方（实际是左侧）

        progressBar = ProgressBar()
        // 放在屏幕右下角，距离右边和下边各10px
        progressBar.setPosition(640f - progressBar.width - 10f, 10f)

        shovel = Shovel(this)
        shovel.setPosition(10f, 0f)

        lawnGroup.setZombieDeathListener(zombieDeathListener)
    }

    private fun reset() {
        // 清空LawnGroup中的所有植物和僵尸
        // 需要创建副本来避免在迭代时修改集合
        val zombiesToRemove = com.badlogic.gdx.utils.Array<Zombie>()
        zombies.forEach { zombiesToRemove.add(it) }
        zombiesToRemove.forEach { lawnGroup.removeActor(it) }

        val plantsToRemove = com.badlogic.gdx.utils.Array<Plant>()
        lawnGroup.plants.forEach { plantsToRemove.add(it) }
        plantsToRemove.forEach { lawnGroup.removeActor(it) }

        // 清空stage中的所有actors
        stage.clear()

        // 重置游戏状态变量
        sun = 50
        gameTime = 0f
        spawnTimer = 0f
        sunDropTimer = 0f
        nextSunDropInterval = Random.nextFloat() * 5f + 5f

        // 重置游戏开始状态
        gameStarted = false
        previewTimer = 0f
        cameraMovingToPreview = true
        cameraReturning = false
        showingReadySetPlant = false
        readySetPlantTimer = 0f
        readySetPlantIndex = 0
        currentFlashText = null
        musicTransitioned = false

        // 重置屏幕震动
        isShaking = false
        shakeTimer = 0f
        shakeDuration = 0f
        shakeIntensity = 0f
        camera.position.x = originalCameraX
        camera.position.y = 180f
        camera.update()

        // 重置最后一波
        finalWaveTriggered = false
        finalWaveTextTimer = 0f
        showingFinalWaveText = false
        firstZombieSpawned = false
        firstZombieTimer = 0f

        // 重置游戏失败状态
        gameLost = false
        zombieSpawnCount = 0

        // 重置游戏胜利状态
        gameWon = false

        // 重置击杀统计
        zombieKillCount = 0

        // 清空卡片列表
        cards.clear()
        selectedCard = null
        shovelMode = false

        // 重置LawnGroup的高亮状态
        lawnGroup.showHighlight = false
        lawnGroup.shovelMode = false
        lawnGroup.highlightX = null
        lawnGroup.highlightY = null

        // 重置UI元素可见性
        sunIconImage.isVisible = false
        sunScoreLabel.isVisible = false
        progressBar.isVisible = false
        shovel.isVisible = false
        progressBar.progress = 0f

        // 更新阳光显示
        sunScoreLabel.setText(sun.toString())

        // 重置光标
        Gdx.graphics.setSystemCursor(com.badlogic.gdx.graphics.Cursor.SystemCursor.Arrow)
    }

    override fun show() {
        // 重置所有游戏状态
        reset()

        val bg = Image(gameBg)
        bg.apply {
            setSize(838.125f, 360f)
            setPosition(0f, 0f)
        }
        stage.addActor(bg)

        val grass = Image(grassTexture)

        grass.apply {
            setSize(461.25f, 316.875f)
            setPosition(140.625f, 12f)
        }
        stage.addActor(grass)

        // 初始隐藏UI元素
        sunIconImage.isVisible = false
        sunScoreLabel.isVisible = false
        progressBar.isVisible = false
        shovel.isVisible = false

        stage.addActor(sunIconImage)
        stage.addActor(sunScoreLabel)
        stage.addActor(progressBar)
        stage.addActor(shovel)
        stage.addActor(lawnGroup)

        // Create and add cards from SeedManager
        SeedManager.allSeeds.forEach { seed ->
            val card = Card(this, seed)
            cards.add(card)
        }

        // Position cards at the top of the screen
        cards.forEachIndexed { index, card ->
            card.setPosition(60f , 300f - index * (card.height + 5f))
            card.isVisible = false // 初始隐藏卡片
            stage.addActor(card)
        }

        // 先播放选择种子音乐
        Assets.chooseYourSeedMusic.isLooping = true
        Assets.chooseYourSeedMusic.play()

        // 生成预览僵尸
        spawnPreviewZombies()

        Gdx.input.inputProcessor = stage
        spawnTimer = 0f // Start timer at 0, first spawn happens after initial interval
        super.show()
    }

    fun selectCard(card: Card) {
        // 取消铲子模式
        if (shovelMode) {
            shovelMode = false
            lawnGroup.shovelMode = false
            shovel.deselectShovel()
        }

        selectedCard = card
        val region = card.seed.plantTexture
        val texture = region.texture
        if (!texture.textureData.isPrepared) {
            texture.textureData.prepare()
        }
        val pixmap = texture.textureData.consumePixmap()
        val cursorPixmap = Pixmap(128, 128, Pixmap.Format.RGBA8888)
        cursorPixmap.drawPixmap(
            pixmap,
            region.regionX, region.regionY, region.regionWidth, region.regionHeight,
            0, 0, 128, 128
        )
        val cursor = Gdx.graphics.newCursor(cursorPixmap, 32, 32)
        Gdx.graphics.setCursor(cursor)
        cursorPixmap.dispose()
        pixmap.dispose()
    }

    fun selectShovel() {
        // 取消卡片选择
        if (selectedCard != null) {
            selectedCard = null
            lawnGroup.showHighlight = false
        }

        shovelMode = true
        lawnGroup.shovelMode = true
    }

    private fun placePlant(lawnX: Int, lawnY: Int) {
        if (lawnGroup.hasPlantAt(lawnX, lawnY)) {
            return
        }
        Assets.plantSound.play()
        selectedCard?.let { card ->
            if (sun >= card.seed.sunCost) {
                sun -= card.seed.sunCost
                val constructor = card.seed.plantClass.java.getConstructor(GameScreen::class.java)
                val soil = Soil()
                soil.setPosition(
                    LawnGroup.LAWN_START_X + lawnX * LawnGroup.CELL_WIDTH,
                    LawnGroup.LAWN_START_Y + lawnY * LawnGroup.CELL_HEIGHT - 12f
                )
                lawnGroup.addActor(soil)
                val plant = constructor.newInstance(this) as Plant
                plant.setDeathListener(plantDeathListener)
                plant.setPosition(
                    LawnGroup.LAWN_START_X + lawnX * LawnGroup.CELL_WIDTH,
                    LawnGroup.LAWN_START_Y + lawnY * LawnGroup.CELL_HEIGHT
                )
                plant.row = lawnY
                plant.lawnX = lawnX
                plant.lawnY = lawnY
                lawnGroup.addActor(plant)
                card.startCooldown()
                selectedCard = null
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
                lawnGroup.showHighlight = false
            }
        }
    }

    private fun removePlant(lawnX: Int, lawnY: Int) {
        // 查找指定位置的植物
        val plant = lawnGroup.getPlantAt(lawnX, lawnY)
        if (plant != null) {
            // 通过造成大量伤害来移除植物
            plant.receiveDamage(9999)
            // 播放铲除音效
            Assets.plantSound.play()

            shovelMode = false
            lawnGroup.shovelMode = false
            shovel.deselectShovel()
            lawnGroup.showHighlight = false
        }
    }

    fun addSun(sun: Sun) {
        stage.addActor(sun)
    }

    fun addPea(pea: Pea) {
        stage.addActor(pea)
    }

    private fun spawnZombie() {
        val randomValue = Random.nextDouble()
        var cumulativeProbability = 0.0
        var selectedZombieClass: Class<out Zombie>? = null

        for ((probability, zombieClass) in zombieSpawnList) {
            cumulativeProbability += probability
            if (randomValue <= cumulativeProbability) {
                selectedZombieClass = zombieClass
                break
            }
        }

        selectedZombieClass?.let {
            val constructor = it.getConstructor(Int::class.java)
            val randomRow = Random.nextInt(0, 5)
            val zombie = constructor.newInstance(randomRow)
            zombie.row = randomRow
            zombie.setPosition(
                LawnGroup.LAWN_START_X + LawnGroup.LAWN_WIDTH + 200f,
                LawnGroup.LAWN_START_Y + randomRow * LawnGroup.CELL_HEIGHT
            )
            zombie.setDeathListener(zombieDeathListener)
            lawnGroup.addActor(zombie)

            // 僵尸生成计数
            zombieSpawnCount++

            // 第一个僵尸出现时显示进度条并播放groan音效
            if (!firstZombieSpawned) {
                firstZombieSpawned = true
                progressBar.isVisible = true
                Assets.groanSound.play()
                firstZombieTimer = 0f
            } else {
                // 后续僵尸有5%概率播放groan音效
                if (Random.nextFloat() < 0.05f) {
                    Assets.groanSound.play()
                }
            }
        }
    }

    private fun dropSunFromSky() {
        // 计算屏幕中间70%区域的范围
        val screenWidth = 640f
        val centerStart = screenWidth * 0.15f  // 左边界：15%
        val centerEnd = screenWidth * 0.85f    // 右边界：85%

        // 在中间70%区域随机选择x坐标
        val randomX = centerStart + Random.nextFloat() * (centerEnd - centerStart)
        val randomY = Random.nextFloat() * 200f + 20f

        // 从屏幕顶部开始掉落
        val startY = 400f

        // 创建从天空掉落的阳光
        val sun = Sun(this, Sun.SunType.FROM_SKY, randomX, startY, targetY = randomY)
        addSun(sun)
    }

    private fun spawnPreviewZombies() {
        // 在场外区域随机放置僵尸用于预览
        // 每个种类至少一个最多5个，一共不超过10个
        val zombieCount = Random.nextInt(3, 6)

        for (i in 0 until zombieCount) {
            val randomRow = Random.nextInt(0, 5)
            val zombie = Zombie(randomRow)
            zombie.state = ZombieState.IDLE
            zombie.row = randomRow

            // 在场外区域随机放置（x > 640）
            val randomX = 650f + Random.nextFloat() * 80f
            zombie.setPosition(
                randomX,
                LawnGroup.LAWN_START_Y + randomRow * LawnGroup.CELL_HEIGHT
            )
            zombie.setDeathListener(zombieDeathListener)
            lawnGroup.addActor(zombie)
        }
    }

    private fun showFlashText(text: String) {
        currentFlashText = text
    }

    fun shakeScreen(duration: Float, intensity: Float) {
        isShaking = true
        shakeTimer = 0f
        shakeDuration = duration
        shakeIntensity = intensity
    }

    private fun showGameOverDialog() {
        // 停止所有音乐
        Assets.grassMusic.stop()
        Assets.chooseYourSeedMusic.stop()

        // 创建对话框
        val dialog = object : com.badlogic.gdx.scenes.scene2d.ui.Dialog("Game Over", Assets.skin_dark) {
            override fun result(obj: Any?) {
                when (obj) {
                    "retry" -> {
                        // 重新开始游戏
                        game.setScreen<TitleScreen>()
                    }
                    "menu" -> {
                        // 返回主菜单
                        game.setScreen<TitleScreen>()
                    }
                }
            }
        }

        dialog.text("The zombies ate your brains!")

        // 添加重试按钮
        dialog.button("Retry", "retry")

        // 添加退出按钮
        dialog.button("Main Menu", "menu")

        // 显示对话框
        dialog.show(stage)
    }

    private fun drawFlashText(batch: com.badlogic.gdx.graphics.g2d.Batch) {
        currentFlashText?.let { text ->
            val labelStyle = Label.LabelStyle(Assets.font, com.badlogic.gdx.graphics.Color.RED)
            val tempLabel = Label(text, labelStyle)
            tempLabel.setFontScale(3f)

            // 计算文字居中位置
            val textWidth = tempLabel.prefWidth
            val textHeight = tempLabel.prefHeight
            val x = (640f - textWidth) / 2
            val y = (360f + textHeight) / 2

            tempLabel.setPosition(x, y)
            tempLabel.draw(batch, 1f)
        }
    }

    override fun render(delta: Float) {
        // 处理屏幕震动
        if (isShaking) {
            shakeTimer += delta
            if (shakeTimer >= shakeDuration) {
                isShaking = false
                camera.position.x = originalCameraX
                camera.update()
            } else {
                // 使用随机偏移实现震动效果
                val offsetX = (Math.random().toFloat() - 0.5f) * 2f * shakeIntensity
                val offsetY = (Math.random().toFloat() - 0.5f) * 2f * shakeIntensity
                camera.position.x = originalCameraX + offsetX
                camera.position.y = 180f + offsetY
                camera.update()
            }
        }

        // 处理预览序列
        if (!gameStarted) {
            // 镜头移动到预览位置 (从320移动到640，速度20px/s)
            if (cameraMovingToPreview) {
                camera.position.x += CAMERA_MOVE_SPEED * delta
                if (camera.position.x >= CAMERA_PREVIEW_X) {
                    camera.position.x = CAMERA_PREVIEW_X
                    cameraMovingToPreview = false
                }
                camera.update()
            }
            // 预览计时
            else if (!cameraReturning && !showingReadySetPlant) {
                previewTimer += delta
                if (previewTimer >= PREVIEW_DURATION) {
                    cameraReturning = true
                }
            }
            // 镜头返回场内 (从640返回到320，速度20px/s)
            else if (cameraReturning) {
                camera.position.x -= CAMERA_MOVE_SPEED * delta
                if (camera.position.x <= 320f) {
                    camera.position.x = 320f
                    cameraReturning = false
                    showingReadySetPlant = true
                    readySetPlantTimer = 0f
                    readySetPlantIndex = 0
                    Assets.readysetplantSound.play()
                    showFlashText(readySetPlantTexts[0])
                }
                camera.update()
            }
            // 显示Ready/Set/Plant序列
            else if (showingReadySetPlant) {
                readySetPlantTimer += delta
                if (readySetPlantTimer >= FLASH_TEXT_DURATION) {
                    readySetPlantIndex++
                    if (readySetPlantIndex < readySetPlantTexts.size) {
                        showFlashText(readySetPlantTexts[readySetPlantIndex])
                        readySetPlantTimer = 0f
                    } else {
                        currentFlashText = null
                        showingReadySetPlant = false
                        gameStarted = true

                        // 清除预览僵尸
                        val zombiesToRemove = com.badlogic.gdx.utils.Array<Zombie>()
                        zombies.forEach { zombie ->
                            if (zombie.state == ZombieState.IDLE) {
                                zombiesToRemove.add(zombie)
                            }
                        }
                        zombiesToRemove.forEach { zombie ->
                            lawnGroup.removeActor(zombie)
                        }

                        // 显示UI元素
                        sunIconImage.isVisible = true
                        sunScoreLabel.isVisible = true
//                        progressBar.isVisible = true
                        shovel.isVisible = true
                        cards.forEach { card ->
                            card.isVisible = true
                        }

                        // 开始播放草地音乐
                        Assets.grassMusic.isLooping = true
                        Assets.grassMusic.play()
                        Assets.chooseYourSeedMusic.stop()
                    }
                }
            }
        }

        // 音乐过渡逻辑
        if (!musicTransitioned && !Assets.chooseYourSeedMusic.isPlaying) {
            musicTransitioned = true
        }

        // 游戏逻辑只在游戏开始后执行
        if (gameStarted && !gameLost && !gameWon) {
            // 检查游戏失败条件：任何僵尸的x坐标小于50f
            zombies.forEach { zombie ->
                if (zombie.x < 50f) {
                    gameLost = true
                    // 先播放losemusic，播放完后再播放scream
                    Assets.grassMusic.stop()
                    Assets.losemusicSound.play()
                    Assets.losemusicSound.setOnCompletionListener {
                        Assets.screamSound.play()
                        showGameOverDialog()
                    }
                    return@render
                }
            }

            gameTime += delta
            spawnTimer += delta
            sunDropTimer += delta

            // 检查游戏胜利条件：时间到达300秒且所有僵尸都被消灭
            if (gameTime >= TOTAL_GAME_DURATION && zombies.size == 0) {
                gameWon = true
                // 停止音乐
                Assets.grassMusic.stop()
                // 跳转到FlagScreen
                game.setScreen<FlagScreen>()
                return@render
            }

            if (firstZombieSpawned && firstZombieTimer < 2f) {
                firstZombieTimer += delta
                if (firstZombieTimer >= 2f) {
                    Assets.awoogaSound.play()
                }
            }

            val progress = (gameTime / TOTAL_GAME_DURATION).coerceAtMost(1f)
            val currentSpawnInterval = INITIAL_SPAWN_INTERVAL + (FINAL_SPAWN_INTERVAL - INITIAL_SPAWN_INTERVAL) * progress

            // 更新进度条
            progressBar.progress = progress

            // 检测是否到达最后30秒
            val timeRemaining = TOTAL_GAME_DURATION - gameTime
            if (!finalWaveTriggered && timeRemaining <= 30f) {
                finalWaveTriggered = true
                showingFinalWaveText = true
                finalWaveTextTimer = 0f
                // 播放最后一波音效
                Assets.finalwaveSound.play()
                Assets.sirenSound.play()
                // 显示红字提示
                showFlashText("FINAL WAVE!")
            }

            // 处理最后一波文字显示计时
            if (showingFinalWaveText) {
                finalWaveTextTimer += delta
                if (finalWaveTextTimer >= 1f) {
                    showingFinalWaveText = false
                    currentFlashText = null
                }
            }

            if (gameTime < TOTAL_GAME_DURATION && spawnTimer >= currentSpawnInterval) {
                spawnZombie()
                spawnTimer = 0f
            }

            // 阳光掉落逻辑
            if (sunDropTimer >= nextSunDropInterval) {
                dropSunFromSky()
                sunDropTimer = 0f
                // 重新随机下一次掉落的时间间隔（5-10秒）
                nextSunDropInterval = Random.nextFloat() * 5f + 5f
            }

            sunScoreLabel.setText(sun.toString())
        }

        stage.act(delta)
        stage.draw()

        // 绘制闪烁文字
        if (currentFlashText != null) {
            stage.batch.begin()
            drawFlashText(stage.batch)
            stage.batch.end()
        }

        // 只在游戏开始后处理输入
        if (gameStarted) {
            val touchX = Gdx.input.x
            val touchY = Gdx.input.y
            val worldCords = camera.unproject(com.badlogic.gdx.math.Vector3(touchX.toFloat(), touchY.toFloat(), 0f))

            if (selectedCard != null) {
                if (lawnGroup.isWithin(worldCords.x, worldCords.y)) {
                    lawnGroup.showHighlight = true
                    val lawnX = lawnGroup.getLawnX(worldCords.x)
                    val lawnY = lawnGroup.getLawnY(worldCords.y)
                    lawnGroup.setHighlight(lawnX, lawnY)
                } else {
                    lawnGroup.showHighlight = false
                }
            }

            if (selectedCard != null) {
                // 左键点击种植
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    if (lawnGroup.isWithin(worldCords.x, worldCords.y)) {
                        val lawnX = lawnGroup.getLawnX(worldCords.x)
                        val lawnY = lawnGroup.getLawnY(worldCords.y)
                        placePlant(lawnX, lawnY)
                    }
                }
                // 右键点击取消选择
                if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                    selectedCard = null
                    Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
                    lawnGroup.showHighlight = false
                }
            }

            // 铲子模式处理
            if (shovelMode) {
                // 显示铲子模式的高亮指示条
                if (lawnGroup.isWithin(worldCords.x, worldCords.y)) {
                    lawnGroup.showHighlight = true
                    val lawnX = lawnGroup.getLawnX(worldCords.x)
                    val lawnY = lawnGroup.getLawnY(worldCords.y)
                    lawnGroup.setHighlight(lawnX, lawnY)
                } else {
                    lawnGroup.showHighlight = false
                }

                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    if (lawnGroup.isWithin(worldCords.x, worldCords.y)) {
                        val lawnX = lawnGroup.getLawnX(worldCords.x)
                        val lawnY = lawnGroup.getLawnY(worldCords.y)
                        removePlant(lawnX, lawnY)
                    }
                }
                // 右键点击取消铲子模式
                if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
                    shovelMode = false
                    lawnGroup.shovelMode = false
                    shovel.deselectShovel()
                    lawnGroup.showHighlight = false
                }
            }
        }
        super.render(delta)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        super.resize(width, height)
    }
}
