package net.perfectdreams.commands.manager

import kotlinx.coroutines.runBlocking
import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand

interface CommandDispatcher<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE: BaseDSLCommand> {
	fun dispatchBlocking(sender: SENDER, command: COMMAND_TYPE, input: String): Boolean {
		val split = input.split(" ")
		return dispatchBlocking(sender, command, split[0], split.drop(1).toTypedArray())
	}

	suspend fun dispatch(sender: SENDER, command: COMMAND_TYPE, input: String): Boolean {
		val split = input.split(" ")
		return dispatch(sender, command, split[0], split.drop(1).toTypedArray())
	}

	fun dispatchBlocking(sender: SENDER, command: COMMAND_TYPE, label: String, arguments: Array<String>): Boolean = runBlocking { dispatch(sender, command, label, arguments) }

	suspend fun dispatch(sender: SENDER, command: COMMAND_TYPE, label: String, arguments: Array<String>): Boolean
}