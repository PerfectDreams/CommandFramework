package net.perfectdreams.commands.console

import net.perfectdreams.commands.DreamCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import net.perfectdreams.commands.dsl.DreamDSLCommand
import net.perfectdreams.commands.manager.CommandContinuationType
import net.perfectdreams.commands.manager.DispatchableCommandManager
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.full.findAnnotation

@Suppress("UNCHECKED_CAST")
class ConsoleCommandManager : DispatchableCommandManager<Sender, DreamCommand, DreamDSLCommand>() {
	private val commands = mutableListOf<DreamCommand>()

	init {
		/* commandListeners.onMethodCommand { sender, dreamCommand, kCallable ->
			val annotation = kCallable.findAnnotation<SubcommandPermission>()
			if (annotation != null) {
				println("Permissão: ${annotation.permission}")
				sender.sendMessage("Sem permissão meu fera")
				return@onMethodCommand CommandContinuationType.CANCEL
			} else {
				println("Não possui annotation @SubcommandPermission!")
			}
			return@onMethodCommand CommandContinuationType.CONTINUE
		}
		
		commandListeners.onDSLCommand { sender, dreamCommand ->
			sender.sendMessage("DreamCommand precisa de permissão: ${dreamCommand.permission}")
			return@onDSLCommand CommandContinuationType.CONTINUE
		} */
	}

	override fun registerCommand(command: DreamCommand) {
		commands.add(command)
	}

	override fun unregisterCommand(command: DreamCommand) {
		commands.remove(command)
	}

	override fun getRegisteredCommands(): List<DreamCommand> {
		return commands
	}

	override fun dispatch(sender: Sender, command: DreamCommand, label: String, arguments: Array<String>, coroutineContext: CoroutineContext?): Boolean {
		println("pre-command: $command and $arguments - command has ${command.subcommands.size} subcommands")
		if (!command.labels.contains(label))
			return false

		println("COMMAND: $command")

		for (subCommand in command.subcommands) {
			println("Processando subcomando $subCommand")
			println("label: ${arguments.drop(0).firstOrNull()}")
			println("args: ${arguments.drop(1).toTypedArray().joinToString(" ")}")

			if (dispatch(sender, subCommand as DreamCommand, arguments.drop(0).firstOrNull() ?: "", arguments.drop(1).toTypedArray(), coroutineContext))
				return true
		}

		println("Executing $command with $label $arguments")

		if (command is BaseDSLCommand) {
			println("Executors: " + command.executors)
		}

		println("Coroutine Context: $coroutineContext")
		return execute(sender, command, arguments, coroutineContext)
	}
}