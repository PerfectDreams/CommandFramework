package net.perfectdreams.commands.dsl

class DreamCommandBuilder(vararg labels: String) : CommandBuilder<DreamDSLCommand>(*labels) {
	fun command(vararg labels: String, block: CommandBuilder<DreamDSLCommand>.() -> Unit): DreamDSLCommand {
		val dreamCommand = DreamCommandBuilder(
				*labels
		).apply(block).build()

		subcommands.add(dreamCommand)

		return dreamCommand
	}

	override fun build(): DreamDSLCommand = DreamDSLCommand(
			*labels,
			executors = executors,
			dslSubcommands = subcommands
	)
}