package net.perfectdreams.commands.manager

import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import java.util.*
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter

class CommandListeners<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE : BaseDSLCommand> {
	var commandProcessors = mutableListOf<suspend ((SENDER, COMMAND_TYPE) -> CommandContinuationType)>()
	var dslCommandProcessors = mutableListOf<suspend (SENDER, DSL_COMMAND_TYPE) -> CommandContinuationType>()
	var methodProcessors = mutableListOf<suspend (SENDER, COMMAND_TYPE, KCallable<*>) -> CommandContinuationType>()
	var parameterProcessors = mutableListOf<suspend (SENDER, COMMAND_TYPE, KParameter, Stack<String>) -> Any?>()
	var throwableProcessors = mutableListOf<suspend (SENDER, COMMAND_TYPE, Throwable) -> CommandContinuationType>()

	fun addCommandListener(callback: suspend ((SENDER, COMMAND_TYPE) -> CommandContinuationType)) {
		commandProcessors.add(callback)
	}

	fun addDSLCommandListener(callback: suspend ((SENDER, DSL_COMMAND_TYPE) -> CommandContinuationType)) {
		dslCommandProcessors.add(callback)
	}

	fun addMethodListener(callback: suspend ((SENDER, COMMAND_TYPE, KCallable<*>) -> CommandContinuationType)) {
		methodProcessors.add(callback)
	}

	fun addParameterListener(callback: suspend ((SENDER, COMMAND_TYPE, KParameter, Stack<String>) -> Any?)) {
		parameterProcessors.add(callback)
	}

	fun addThrowableListener(callback: suspend ((SENDER, COMMAND_TYPE, Throwable) -> CommandContinuationType)) {
		throwableProcessors.add(callback)
	}
}