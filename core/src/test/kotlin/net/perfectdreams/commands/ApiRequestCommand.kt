package net.perfectdreams.commands

import kotlinx.coroutines.delay
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class ApiRequestCommand : DreamCommand("coroutine") {
	@Subcommand
	suspend fun root(sender: Sender, vararg text: String = arrayOf("lori", "é", "muito", "fofis")) {
		sender.sendMessage("Você enviou: ${text.joinToString(" ")}")
		sender.sendMessage("Resposta da API: " + superSlowApiRequest(text))
	}

	suspend fun superSlowApiRequest(arg: Array<out String>): String {
		// delay(5000)
		return arg.joinToString(" ").reversed()
	}
}