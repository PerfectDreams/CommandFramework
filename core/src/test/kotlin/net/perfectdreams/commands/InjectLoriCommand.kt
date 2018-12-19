package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.CustomArgumentType
import net.perfectdreams.commands.annotation.CustomInjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class InjectLoriCommand : DreamCommand("lori") {
	@Subcommand
	fun lori(sender: Sender, @CustomInjectArgument(CustomArgumentType.LORITTA_MORENITTA) lori: String) {
		sender.sendMessage(lori)
	}
}