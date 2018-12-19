package net.perfectdreams.commands.dsl

/* fun <T : BaseDSLCommand> command(vararg labels: String, block: CommandBuilder<T>.() -> Unit) = CommandBuilder<T>(*labels)
		.apply(block)
		.build()

inline fun <COMMAND : BaseDSLCommand, reified SENDER> simpleCommand(vararg labels: String, noinline block: (SENDER, Array<String>) -> Unit) = CommandBuilder<COMMAND>(*labels)
		.apply {
			whenever(block)
		}.build() */

/* fun command(vararg labels: String, permission: String, block: DreamCommandBuilder.() -> Unit) = DreamCommandBuilder(*labels, permission = permission)
		.apply(block)
		.build()

inline fun <reified SENDER> simpleCommand(vararg labels: String, permission: String, noinline block: (SENDER, Array<String>) -> Unit) = DreamCommandBuilder(*labels, permission = permission)
		.apply {
			whenever(block)
		}.build() */