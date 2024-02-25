package com.github.Ringoame196

import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class WrittenBook(private val plugin: Plugin, val book: ItemStack) {
    private val bookMeta = book.itemMeta as BookMeta
    private val passKey = NamespacedKey(plugin, "passwordKey")
    private val cryptoSystem = CryptoSystem(plugin)
    fun setLock(pass: String) {
        bookMeta.persistentDataContainer.set(passKey, PersistentDataType.STRING, pass)
        bookMeta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        book.itemMeta = bookMeta
    }
    fun checkLock(): Boolean {
        return acquisitionLockPass() != null
    }
    fun acquisitionLockPass(): String? {
        return bookMeta.persistentDataContainer.get(passKey, PersistentDataType.STRING)
    }
    fun sentenceCipher() {
        for (page in 1..bookMeta.pages.size) {
            val content = bookMeta.getPage(page) // 各ページの内容を取得する
            val cipher = cryptoSystem.encryption(content)
            bookMeta.setPage(page, cipher)
        }
        book.setItemMeta(bookMeta)
    }
    fun sentenceDecryption(): ItemStack {
        val newBook = book.clone()
        val newBookMeta = bookMeta.clone()
        for (page in 1..bookMeta.pageCount) {
            val content = bookMeta.getPage(page) // 各ページの内容を取得する
            val decryptedContent = cryptoSystem.restore(content)
            newBookMeta.setPage(page, decryptedContent)
        }
        newBook.setItemMeta(newBookMeta)
        return newBook
    }
}
