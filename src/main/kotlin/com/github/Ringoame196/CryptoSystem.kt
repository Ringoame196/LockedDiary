package com.github.Ringoame196

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File
import java.security.Key
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class CryptoSystem(private val plugin: Plugin) {
    private fun acquisitionEncryptionKey(): String? {
        val configFile = File(plugin.dataFolder, "config.yml")
        val config = YamlConfiguration.loadConfiguration(configFile)
        return config.getString("encryptionKey")
    }
    fun settingCipherKey() {
        val config = plugin.config
        if (acquisitionEncryptionKey() != "") {
            return
        }
        val newEncryptionKey = generateRandomKey()
        config.set("encryptionKey", newEncryptionKey)
        plugin.saveConfig()
    }
    private fun generateRandomKey(): String {
        val bateSize = 16
        val random = SecureRandom()
        val keyBytes = ByteArray(bateSize) // 16バイトのキーを生成する
        random.nextBytes(keyBytes)
        return Base64.getEncoder().encodeToString(keyBytes)
    }
    fun encryption(value: String): String {
        val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val key: Key = SecretKeySpec(acquisitionEncryptionKey()?.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedBytes: ByteArray = cipher.doFinal(value.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
    fun certification(value: String, cipher: String): Boolean {
        val inputPassword = encryption(value)
        return inputPassword == cipher
    }
    fun restore(value: String): String {
        val cipher: Cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        val key: Key = SecretKeySpec(acquisitionEncryptionKey()?.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decryptedBytes: ByteArray = cipher.doFinal(Base64.getDecoder().decode(value))
        return String(decryptedBytes)
    }
}
