package com.pvz.vidar.game.wsdx233.top.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.pvz.vidar.game.wsdx233.top.Assets
import com.pvz.vidar.game.wsdx233.top.Main
import ktx.app.KtxScreen
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.xor

// 僵尸击杀数 260

class FlagScreen(private val game: Main, private val zombieKillCount: Int = 0) : KtxScreen {

    private val camera = OrthographicCamera()
    private val stage = Stage(ExtendViewport(640f, 360f, camera))

    // Kill count encrypted flag (requires kill count to round to 260)
    // Only kill counts 255-264 will decrypt correctly
    // Uses complex multi-layer key derivation with position-dependent encryption
    // This contains both chunks encrypted together
    private val killCountEncryptedFlag = byteArrayOf(
        // Chunk 1 (13 bytes)
        0x00.toByte(), 0xf8.toByte(), 0xfa.toByte(), 0x06.toByte(),
        0x1f.toByte(), 0xd9.toByte(), 0x98.toByte(), 0x72.toByte(),
        0x56.toByte(), 0xe9.toByte(), 0xdd.toByte(), 0x1c.toByte(),
        0x86.toByte(),
        // Chunk 2 (13 bytes)
        0x38.toByte(), 0x1d.toByte(), 0x82.toByte(), 0xe3.toByte(),
        0x5e.toByte(), 0x17.toByte(), 0xe3.toByte(), 0x2e.toByte(),
        0x82.toByte(), 0xfc.toByte(), 0x2d.toByte(), 0x14.toByte(),
        0xc7.toByte()
    )

    // XOR keys stored separately
    private val xorKey1 = 0x66.toByte()
    private val xorKey2 = 0x77.toByte()

    // Layer 2: AES-like encryption key
    private val aesEncryptedKey = byteArrayOf(
        0x4a.toByte(), 0x91.toByte(), 0xc3.toByte(), 0x7f.toByte(),
        0x2e.toByte(), 0xb5.toByte(), 0x68.toByte(), 0xd4.toByte(),
        0x1c.toByte(), 0x89.toByte(), 0x3a.toByte(), 0xf2.toByte(),
        0x5d.toByte(), 0xa6.toByte(), 0x71.toByte(), 0xbe.toByte()
    )

    // Layer 3: Base64-like custom encoding table (shuffled)
    private val customBase64Table = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm0123456789+/"

    // Layer 4: Rotation cipher offset (derived from game state)
    private fun getRotationOffset(): Int {
        val seed = "PLANTS_VS_ZOMBIES_2025"
        return seed.sumOf { it.code } % 26
    }

    // Layer 5: Substitution cipher mapping
    private val substitutionMap = mapOf(
        'A' to 'Q', 'B' to 'W', 'C' to 'E', 'D' to 'R', 'E' to 'T',
        'F' to 'Y', 'G' to 'U', 'H' to 'I', 'I' to 'O', 'J' to 'P',
        'K' to 'A', 'L' to 'S', 'M' to 'D', 'N' to 'F', 'O' to 'G',
        'P' to 'H', 'Q' to 'J', 'R' to 'K', 'S' to 'L', 'T' to 'Z',
        'U' to 'X', 'V' to 'C', 'W' to 'V', 'X' to 'B', 'Y' to 'N',
        'Z' to 'M', '_' to '!', '{' to '[', '}' to ']'
    )

    // Reverse substitution for decryption
    private val reverseSubstitutionMap = substitutionMap.entries.associate { (k, v) -> v to k }

    // Complex key derivation using multiple mathematical transformations
    private fun deriveKeyFromKillCount(killCount: Int): ByteArray {
        // Layer 1: Apply rounding transformation
        val rounded = ((killCount + 5) / 10) * 10

        // Layer 2: Hash-like transformation using prime numbers
        val hash1 = (rounded * 31 + 17) % 997
        val hash2 = (rounded * 37 + 23) % 991
        val hash3 = (rounded * 41 + 29) % 983

        // Layer 3: Combine hashes with XOR and rotation
        val combined = (hash1 xor (hash2 shl 3) xor (hash3 shr 2)) % 256

        // Layer 4: Generate multi-byte key using seed
        val seed = (rounded * 7 + hash1 + hash2 + hash3) % 65536
        val key = ByteArray(16)
        var state = seed
        for (i in key.indices) {
            state = (state * 1103515245 + 12345) and 0x7fffffff
            key[i] = ((state shr 16) % 256).toByte()
        }

        return key
    }

    // Multi-layer XOR decryption with position-dependent keys
    private fun decryptWithKillCount(data: ByteArray, killCount: Int): ByteArray {
        val keyStream = deriveKeyFromKillCount(killCount)
        val result = ByteArray(data.size)

        for (i in data.indices) {
            // Use different key bytes for different positions
            val keyByte = keyStream[i % keyStream.size]
            // Add position-dependent transformation
            val positionKey = ((i * 13 + 7) % 256).toByte()
            result[i] = (data[i].toInt() xor keyByte.toInt() xor positionKey.toInt()).toByte()
        }

        return result
    }

    // Decryption function - Layer 1: XOR decryption
    private fun xorDecrypt(data: ByteArray, key: Byte): ByteArray {
        return data.map { (it xor key).toByte() }.toByteArray()
    }

    // Layer 2: Simple AES-like transformation (simplified for obfuscation)
    private fun simpleAesDecrypt(data: ByteArray, key: ByteArray): ByteArray {
        val result = ByteArray(data.size)
        for (i in data.indices) {
            result[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return result
    }

    // Layer 3: Rotation cipher decryption
    private fun rotateDecrypt(text: String, offset: Int): String {
        return text.map { char ->
            when {
                char.isUpperCase() -> {
                    val shifted = (char - 'A' - offset + 26) % 26
                    ('A' + shifted)
                }
                char.isLowerCase() -> {
                    val shifted = (char - 'a' - offset + 26) % 26
                    ('a' + shifted)
                }
                else -> char
            }
        }.joinToString("")
    }

    // Layer 4: Substitution cipher decryption
    private fun substitutionDecrypt(text: String): String {
        return text.map { reverseSubstitutionMap[it] ?: it }.joinToString("")
    }

    // Layer 5: Custom encoding with checksum
    private fun customDecode(encoded: String): String {
        val decoded = StringBuilder()
        var checksum = 0

        for (i in encoded.indices step 2) {
            if (i + 1 < encoded.length) {
                val byte1 = encoded[i].code
                val byte2 = encoded[i + 1].code
                val combined = ((byte1 xor 0x5A) shl 4) or (byte2 xor 0xA5)
                decoded.append(combined.toChar())
                checksum = (checksum + combined) % 256
            }
        }

        return decoded.toString()
    }

    // Master decryption function combining all layers
    private fun decryptFlag(): String {
        try {
            // Step 0: Decrypt using kill count (NEW LAYER - must round to 260)
            // This decrypts to get two chunks that need further processing
            val killCountDecrypted = decryptWithKillCount(killCountEncryptedFlag, zombieKillCount)

            // Split the decrypted data into two chunks (13 bytes each)
            val chunkSize = killCountDecrypted.size / 2
            val decryptedChunk1 = killCountDecrypted.sliceArray(0 until chunkSize)
            val decryptedChunk2 = killCountDecrypted.sliceArray(chunkSize until killCountDecrypted.size)

            // Step 1: XOR decrypt both chunks with different keys (outermost layer)
            val decrypted1 = xorDecrypt(decryptedChunk1, xorKey1)
            val decrypted2 = xorDecrypt(decryptedChunk2, xorKey2)

            // Step 2: Combine chunks
            val combined = decrypted1 + decrypted2

            // Step 3: Apply AES-like decryption (XOR is symmetric, so same operation)
            val aesDecrypted = simpleAesDecrypt(combined, aesEncryptedKey)

            // Step 4: Convert to string
            val intermediate = String(aesDecrypted, StandardCharsets.UTF_8)

            // Step 5: Apply rotation cipher decryption
            val rotated = rotateDecrypt(intermediate, getRotationOffset())

            // Step 6: Apply substitution cipher decryption
            val substituted = substitutionDecrypt(rotated)

            // Step 7: Return final decrypted flag
            return substituted

        } catch (e: Exception) {
            // Fallback - should never happen in normal execution
            return "ERROR_DECRYPTING"
        }
    }

    // Additional obfuscation: Split flag verification across multiple functions
    private fun verifyFlagIntegrity(flag: String): Boolean {
        val expectedLength = 26
        val expectedPrefix = "flag{"
        val expectedSuffix = "}"

        return flag.length == expectedLength &&
               flag.startsWith(expectedPrefix) &&
               flag.endsWith(expectedSuffix)
    }

    // Final flag getter with multiple verification layers
    private fun getFlag(): String {
        // Use the decrypted flag (XOR decryption)
        val flag = decryptFlag()

        // Verify integrity
        if (!verifyFlagIntegrity(flag)) {
            return "INTEGRITY_CHECK_FAILED"
        }

        // Even if hash doesn't match, return the flag (hash is just for obfuscation)
        return flag
    }

    private var titleLabel: Label
    private var flagLabel: Label
    private var instructionLabel: Label

    init {
        val labelStyle = Label.LabelStyle(Assets.font, Color.WHITE)

        titleLabel = Label("CONGRATULATIONS!", labelStyle)
        titleLabel.setFontScale(2.5f)
        titleLabel.setAlignment(Align.center)

        flagLabel = Label("", labelStyle)
        flagLabel.setFontScale(1.8f)
        flagLabel.setAlignment(Align.center)
        flagLabel.color = Color.GREEN

        instructionLabel = Label("Press SPACE to return to menu", labelStyle)
        instructionLabel.setFontScale(1.2f)
        instructionLabel.setAlignment(Align.center)
    }

    override fun show() {
        stage.clear()

        // Position labels
        titleLabel.setPosition(
            320f,
            250f,
            Align.center,
        )

        // Decrypt and display flag
        val flag = getFlag()
        flagLabel.setText(flag)
        flagLabel.setPosition(
            320f,
            180f,
            Align.center
        )

        instructionLabel.setPosition(
            320f,
            100f,
            Align.center,
        )

        stage.addActor(titleLabel)
        stage.addActor(flagLabel)
        stage.addActor(instructionLabel)

        Gdx.input.inputProcessor = stage

        // Play victory music if available
        if (Assets.grassMusic.isPlaying) {
            Assets.grassMusic.stop()
        }
    }

    override fun render(delta: Float) {
        stage.act(delta)
        stage.draw()

        // Handle input
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen<TitleScreen>()
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun dispose() {
        stage.dispose()
    }
}
