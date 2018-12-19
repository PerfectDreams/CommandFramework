package net.perfectdreams.commands

import net.perfectdreams.commands.console.Sender
import net.perfectdreams.commands.dsl.command

class DomainSpecificLanguageCommand {
	fun generateCommand() = command("dslexample") {
		command("easter") {
			whenever<Sender, String?> { sender, input ->
				if (input == null) {
					sender.sendMessage("Apenas os fortes que possuem a senha poderão ver o easter egg.")
					return@whenever
				}

				if (input != "lorotajubinha") {
					sender.sendMessage("Errou! $input não é a senha, tente novamente, mas agora com mais confiança!")
					return@whenever
				}

				sender.sendMessage("Parabéns, você encotrou o easter egg! Guarde ele com muito carinho ^-^ https://bit.ly/segredolori")
			}
		}

		whenever<Sender> {
			it.sendMessage("i love u :3")
		}
	}
}