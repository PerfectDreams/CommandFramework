package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.CustomArgumentType
import net.perfectdreams.commands.annotation.CustomInjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class InjectedAylaJoinedCommand : DreamCommand("ayla") {
	@Subcommand
	suspend fun root(sender: Sender) {
		sender.sendMessage("Isto JAMAIS deve acontecer!")
	}

	@Subcommand(["all_arguments"])
	suspend fun allArguments(sender: Sender, @CustomInjectArgument(CustomArgumentType.ARGUMENT_LIST) allArgs: String?) {
		sender.sendMessage("$allArgs")
	}
}