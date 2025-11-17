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

class FlagScreen(private val game: Main) : KtxScreen {

    private val camera = OrthographicCamera()
    private val stage = Stage(ExtendViewport(640f, 360f, camera))

    // Multi-layer encrypted flag storage
    // Layer 1: XOR obfuscation with multiple keys (outermost layer)
    private val encryptedChunk1 = byteArrayOf(
        0x56.toByte(), 0x91.toByte(), 0xd0.toByte(), 0x78.toByte(),
        0x13.toByte(), 0x82.toByte(), 0x40.toByte(), 0xeb.toByte(),
        0x31.toByte(), 0xbd.toByte(), 0x1a.toByte(), 0xda.toByte(),
        0x1a.toByte()
    )

    private val encryptedChunk2 = byteArrayOf(
        0x98.toByte(), 0x27.toByte(), 0x82.toByte(), 0x65.toByte(),
        0xc7.toByte(), 0xed.toByte(), 0x4d.toByte(), 0x12.toByte(),
        0x89.toByte(), 0x54.toByte(), 0xe4.toByte(), 0x23.toByte(),
        0xa3.toByte()
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
            // Step 1: XOR decrypt both chunks with different keys (outermost layer)
            val decrypted1 = xorDecrypt(encryptedChunk1, xorKey1)
            val decrypted2 = xorDecrypt(encryptedChunk2, xorKey2)

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
