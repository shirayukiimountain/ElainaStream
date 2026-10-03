package dev.shira.anime.data.remote

import java.nio.ByteBuffer
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    fun decrypt(cipherText: String?, secretKeyHex: String?): String {
        if (cipherText.isNullOrBlank()) return ""
        if (cipherText.startsWith("http://") || cipherText.startsWith("https://")) {
            return cipherText
        }
        if (secretKeyHex.isNullOrBlank() || secretKeyHex.length != 64) {
            return cipherText
        }

        return runCatching {
            val keyBytes = ByteArray(32) { i ->
                val index = i * 2
                ((Character.digit(secretKeyHex[index], 16) shl 4) +
                        Character.digit(secretKeyHex[index + 1], 16)).toByte()
            }

            val decodedBytes = decodeBase64(cipherText)
            if (decodedBytes.size <= 28) return cipherText

            val byteBuffer = ByteBuffer.wrap(decodedBytes)
            val tag = ByteArray(16)
            val iv = ByteArray(12)
            val cipherLen = byteBuffer.remaining() - 28
            val cipherBytes = ByteArray(cipherLen)

            byteBuffer.get(tag)
            byteBuffer.get(iv)
            byteBuffer.get(cipherBytes)

            val gcmCipherWithTag = ByteArray(cipherLen + 16)
            System.arraycopy(cipherBytes, 0, gcmCipherWithTag, 0, cipherLen)
            System.arraycopy(tag, 0, gcmCipherWithTag, cipherLen, 16)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

            val plainBytes = cipher.doFinal(gcmCipherWithTag)
            String(plainBytes, Charsets.UTF_8)
        }.getOrDefault(cipherText)
    }

    private fun decodeBase64(input: String): ByteArray {
        return runCatching {
            android.util.Base64.decode(input, android.util.Base64.DEFAULT)
        }.getOrElse {
            java.util.Base64.getDecoder().decode(input)
        }
    }
}
