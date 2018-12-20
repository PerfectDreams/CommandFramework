package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class MagicallyExceptionallyCommand : DreamCommand("magically") {
	@Subcommand
	fun magically(sender: Sender) {
		throw DreamCommandException("https://bit.ly/segredolori")
	}
}