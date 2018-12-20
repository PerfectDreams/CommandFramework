package net.perfectdreams.commands.dsl

import net.perfectdreams.commands.DreamCommand

open class DreamDSLCommand(vararg labels: String, override val executors: List<DSLExecutorWrapper>, dslSubcommands: List<BaseDSLCommand>) : DreamCommand(*labels), BaseDSLCommand {
	init {
		// lol nope, vamos ignorar todos os subcomandos registrados pela classe principal, elas são chatas!
		subcommands.clear()

		// E colocar todos os subcomandos de DSL após iniciar
		subcommands.addAll(dslSubcommands)

		// Deste jeito ainda é possível usar o "subcommands" para adicionar subcomandos de outras classes! Yay!
	}
}