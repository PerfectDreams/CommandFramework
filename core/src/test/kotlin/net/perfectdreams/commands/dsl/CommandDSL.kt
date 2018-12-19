package net.perfectdreams.commands.dsl

/* fun <T : BaseDSLCommand> command(vararg labels: String, block: CommandBuilder<T>.() -> Unit) = CommandBuilder<T>(*labels)
		.apply(block)
		.build()

inline fun <COMMAND : BaseDSLCommand, reified SENDER> simpleCommand(vararg labels: String, noinline block: (SENDER, Array<String>) -> Unit) = CommandBuilder<COMMAND>(*labels)
		.apply {
			whenever(block)
		}.build() */

fun command(vararg labels: String, block: DreamCommandBuilder.() -> Unit) = DreamCommandBuilder(*labels)
		.apply(block)
		.build()

inline fun <reified SENDER> simpleCommand(vararg labels: String, noinline block: (SENDER, Array<String>) -> Unit) = DreamCommandBuilder(*labels)
		.apply {
			whenever(block)
		}.build()