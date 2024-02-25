package com.github.Ringoame196.Commands

import com.github.Ringoame196.CryptoSystem
import com.github.Ringoame196.SenderPlayer
import com.github.Ringoame196.WrittenBook
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class Lock(private val plugin: Plugin) : CommandExecutor, TabExecutor {
    private val cryptoSystem = CryptoSystem(plugin)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        return true
    }
    private fun setLock(sender: SenderPlayer, writtenBook: WrittenBook, passWord: String) {
        if (writtenBook.checkLock()) {
            sender.errorMessage("${ChatColor.RED}既にロックがかかっています")
            return
        }
        val cipherValue = cryptoSystem.encryption(passWord)
        writtenBook.setLock(cipherValue)
        writtenBook.sentenceCipher()
        sender.player.sendMessage("${ChatColor.YELLOW}ロックをかけました")
        sender.player.sendMessage("${ChatColor.DARK_RED}コマンドを実行せずに閉じてください")
        sender.player.playSound(sender.player, Sound.BLOCK_ANVIL_USE, 1f, 1f)
    }
    private fun open(sender: SenderPlayer, writtenBook: WrittenBook, passWord: String) {
        val bookPassWorld = writtenBook.acquisitionLockPass() ?: return
        if (!cryptoSystem.certification(passWord, bookPassWorld)) {
            sender.errorMessage("${ChatColor.RED}パスワードが一致しませんでした")
            return
        }
        val sourceSentenceBook = writtenBook.sentenceDecryption()
        sender.player.openBook(sourceSentenceBook)
        sender.player.playSound(sender.player, Sound.BLOCK_CHEST_OPEN, 1f, 1f)
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): MutableList<String>? {
        if (sender !is Player) { return mutableListOf() }
        val senderPlayer = SenderPlayer(sender)
        val book = sender.inventory.itemInMainHand
        if (book.type != Material.WRITTEN_BOOK) { return mutableListOf() }
        val writtenBook = WrittenBook(plugin, book)
        if (args.size < 3) {
            return guide(args)
        }
        val subCommand = args[0]
        val passWord = args[1]
        val exit = args[2]
        val subCommandMap = mapOf(
            "set" to { setLock(senderPlayer, writtenBook, passWord) },
            "open" to { open(senderPlayer, writtenBook, passWord) }
        )
        if (exit != "exit") {
            return guide(args)
        }
        senderPlayer.player.sendMessage("${ChatColor.YELLOW}準備中… 少しお待ち下さい")
        subCommandMap[subCommand]?.invoke()
        return mutableListOf()
    }
    private fun guide(args: Array<out String>): MutableList<String> {
        return when (args.size) {
            1 -> mutableListOf("set", "open")
            2 -> mutableListOf("[パスワード]", "※個人情報、重要情報は入力しないてください")
            3 -> mutableListOf("exit", "※コマンドを実行せずに 閉じてください")
            else -> mutableListOf()
        }
    }
}
