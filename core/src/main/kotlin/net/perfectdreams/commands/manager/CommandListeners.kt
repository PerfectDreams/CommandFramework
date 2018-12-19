package net.perfectdreams.commands.manager

import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter

class CommandListeners<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE : BaseDSLCommand> {
	var commandProcessor: ((SENDER, COMMAND_TYPE) -> CommandContinuationType)? = null
		private set
	var commandDslProcessor: ((SENDER, DSL_COMMAND_TYPE) -> CommandContinuationType)? = null
		private set
	var commandMethodProcessor: ((SENDER, COMMAND_TYPE, KCallable<*>) -> CommandContinuationType)? = null
		private set
	var commandParameterProcessor: ((SENDER, COMMAND_TYPE, KParameter) -> Any?)? = null
		private set

	fun onCommand(callback: ((SENDER, COMMAND_TYPE) -> CommandContinuationType)?) {
		commandProcessor = callback
	}

	fun onDSLCommand(callback: ((SENDER, DSL_COMMAND_TYPE) -> CommandContinuationType)?) {
		commandDslProcessor = callback
	}

	fun onMethodCommand(callback: ((SENDER, COMMAND_TYPE, KCallable<*>) -> CommandContinuationType)?) {
		commandMethodProcessor = callback
	}

	fun onParameter(callback: ((SENDER, COMMAND_TYPE, KParameter) -> Any?)?) {
		commandParameterProcessor = callback
	}
}