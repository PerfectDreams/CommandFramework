package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender
import net.perfectdreams.commands.dsl.simpleCommand

class ClassAndDSLCommand : DreamCommand("classdsl") {
	init {
		subcommands.add(
				simpleCommand<Sender>("dsl") { sender, args ->
					sender.sendMessage("Você escreveu ${args.joinToString(" ")}")
				}
		)
	}

	@Subcommand
	fun root(sender: Sender) {
		sender.sendMessage("Olá, mundo! Use \"classdsl dsl\" para testar a parte em DSL!")
	}
}