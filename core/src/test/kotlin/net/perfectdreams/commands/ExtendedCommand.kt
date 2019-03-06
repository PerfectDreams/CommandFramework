package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class ExtendedCommand(label: String) : RootBaseCommand(label) {
    @Subcommand
    fun base(sender: Sender) {
        sender.sendMessage("Olá ${this.something()}!")
    }
}