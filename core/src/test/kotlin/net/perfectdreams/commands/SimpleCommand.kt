package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class SimpleCommand : DreamCommand("simple") {
	@Subcommand
	fun simple(sender: Sender) {
		sender.sendMessage("Olá, mundo!")
	}

	@Subcommand(["goodbye"])
	fun test(sender: Sender) {
		sender.sendMessage("Tchau, mundo!")
	}
}