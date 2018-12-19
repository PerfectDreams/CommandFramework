package net.perfectdreams.commands.dsl

interface Buildable<DSL_COMMAND_TYPE : BaseDSLCommand> {
	fun build(): DSL_COMMAND_TYPE
}