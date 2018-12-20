package net.perfectdreams.commands.manager

import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand

abstract class DispatchableCommandManager<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE: BaseDSLCommand> : CommandManager<SENDER, COMMAND_TYPE, DSL_COMMAND_TYPE>(), CommandDispatcher<SENDER, COMMAND_TYPE, DSL_COMMAND_TYPE> {
	fun dispatchBlocking(sender: SENDER, input: String): Boolean {
		val split = input.split(" ")
		return dispatchBlocking(sender, split[0], split.drop(1).toTypedArray())
	}

	fun dispatchBlocking(sender: SENDER, label: String, arguments: Array<String>): Boolean {
		for (command in getRegisteredCommands()) {
			if (dispatchBlocking(sender, command, label, arguments))
				return true
		}
		return false
	}

	suspend fun dispatch(sender: SENDER, input: String): Boolean {
		val split = input.split(" ")
		return dispatch(sender, split[0], split.drop(1).toTypedArray())
	}

	suspend fun dispatch(sender: SENDER, label: String, arguments: Array<String>): Boolean {
		for (command in getRegisteredCommands()) {
			if (dispatch(sender, command, label, arguments))
				return true
		}
		return false
	}
}