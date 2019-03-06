package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

abstract class RootBaseCommand(label: String) : DreamCommand(label) {
    fun something(): String {
        return "Teste"
    }

    @Subcommand(["fofis"])
    fun lorota(sender: Sender) {
        sender.sendMessage(":3 https://twitter.com/Haaataoh/status/1103377726432571392")
    }
}