package net.perfectdreams.commands.manager

import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import kotlin.coroutines.CoroutineContext

interface CommandDispatcher<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE: BaseDSLCommand> {
	fun dispatch(sender: SENDER, command: COMMAND_TYPE, input: String, coroutineContext: CoroutineContext? = null): Boolean {
		val split = input.split(" ")
		return dispatch(sender, command, split[0], split.drop(1).toTypedArray(), coroutineContext)
	}

	fun dispatch(sender: SENDER, command: COMMAND_TYPE, label: String, arguments: Array<String>, coroutineContext: CoroutineContext? = null): Boolean
}