package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class DefaultValuesCommand : DreamCommand("default") {
	@Subcommand
	fun kindaRoot(sender: Sender) {
		sender.sendMessage("Isto jamais deve aparecer!")
	}

	@Subcommand
	fun root(sender: Sender, value: String = "hello_world", value2: String = "lori é fofa!") {
		sender.sendMessage("Você escreveu $value! $value2")
	}
}