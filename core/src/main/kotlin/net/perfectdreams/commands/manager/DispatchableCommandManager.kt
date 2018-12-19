package net.perfectdreams.commands.manager

import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import kotlin.coroutines.CoroutineContext

abstract class DispatchableCommandManager<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE: BaseDSLCommand> : CommandManager<SENDER, COMMAND_TYPE, DSL_COMMAND_TYPE>(), CommandDispatcher<SENDER, COMMAND_TYPE, DSL_COMMAND_TYPE> {
	fun dispatch(sender: SENDER, input: String, coroutineContext: CoroutineContext? = null): Boolean {
		val split = input.split(" ")
		return dispatch(sender, split[0], split.drop(1).toTypedArray(), coroutineContext)
	}

	fun dispatch(sender: SENDER, label: String, arguments: Array<String>, coroutineContext: CoroutineContext? = null): Boolean {
		for (command in getRegisteredCommands()) {
			if (dispatch(sender, command, label, arguments, coroutineContext))
				return true
		}
		return false
	}
}