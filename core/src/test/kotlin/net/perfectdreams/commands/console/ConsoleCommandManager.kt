package net.perfectdreams.commands.console

import net.perfectdreams.commands.DreamCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.dsl.DreamDSLCommand
import net.perfectdreams.commands.manager.DispatchableCommandManager

@Suppress("UNCHECKED_CAST")
class ConsoleCommandManager : DispatchableCommandManager<Sender, DreamCommand, DreamDSLCommand>() {
	private val commands = mutableListOf<DreamCommand>()

	override fun registerCommand(command: DreamCommand) {
		commands.add(command)
	}

	override fun unregisterCommand(command: DreamCommand) {
		commands.remove(command)
	}

	override fun getRegisteredCommands(): List<DreamCommand> {
		return commands
	}

	override suspend fun dispatch(sender: Sender, command: DreamCommand, label: String, arguments: Array<String>): Boolean {
		println("pre-command: $command and $arguments - command has ${command.subcommands.size} subcommands")
		if (!command.labels.contains(label))
			return false

		println("COMMAND: $command")

		for (subCommand in command.subcommands) {
			println("Processando subcomando $subCommand")
			println("label: ${arguments.drop(0).firstOrNull()}")
			println("args: ${arguments.drop(1).toTypedArray().joinToString(" ")}")

			if (dispatch(sender, subCommand as DreamCommand, arguments.drop(0).firstOrNull() ?: "", arguments.drop(1).toTypedArray()))
				return true
		}

		println("Executing $command with $label $arguments")

		if (command is BaseDSLCommand) {
			println("Executors: " + command.executors)
		}

		return execute(sender, command, arguments)
	}
}