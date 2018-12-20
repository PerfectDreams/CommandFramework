package net.perfectdreams.commands.dsl

@Suppress("UNCHECKED_CAST")
abstract class CommandBuilder<DSL_COMMAND_TYPE : BaseDSLCommand>(vararg val labels: String) : Buildable<DSL_COMMAND_TYPE> {
	val executors = mutableListOf<DSLExecutorWrapper>()
	val subcommands = mutableListOf<BaseDSLCommand>()

	// abstract fun command(vararg labels: String, block: CommandBuilder<DSL_COMMAND_TYPE>.() -> Unit): DSL_COMMAND_TYPE
	/* fun command(vararg labels: String, block: CommandBuilder<DSL_COMMAND_TYPE>.() -> Unit): BaseDSLCommand {
		val dreamCommand = CommandBuilder<DSL_COMMAND_TYPE>(
				*labels
		).apply(block).build()

		subcommands.add(dreamCommand)

		return dreamCommand
	} */

	// inline fun <reified T> simpleCommand(vararg labels: String, noinline block: (T, Array<String>) -> Unit) = command(*labels) { whenever(block) }

	inline fun <reified T1> whenever(noinline block: (T1) -> Unit) {
		executors.add(DSLExecutorWrapper(arrayOf(wrap<T1>()), block as (Any) -> Unit))
	}

	inline fun <reified T1, reified T2> whenever(noinline block: (T1, T2) -> Unit) {
		executors.add(DSLExecutorWrapper(arrayOf(wrap<T1>(), wrap<T2>()), block as (Any, Any?) -> Unit))
	}

	inline fun <reified T1, reified T2, reified T3> whenever(noinline block: (T1, T2, T3) -> Unit) {
		executors.add(DSLExecutorWrapper(arrayOf(wrap<T1>(), wrap<T2>(), wrap<T3>()), block as (Any, Any?, Any?) -> Unit))
	}

	inline fun <reified T1, reified T2, reified T3, reified T4> whenever(noinline block: (T1, T2, T3, T4) -> Unit) {
		executors.add(DSLExecutorWrapper(arrayOf(wrap<T1>(), wrap<T2>(), wrap<T3>(), wrap<T4>()), block as (Any, Any?, Any?, Any?) -> Unit))
	}

	inline fun <reified T1, reified T2, reified T3, reified T4, reified T5> whenever(noinline block: (T1, T2, T3, T4, T5) -> Unit) {
		executors.add(DSLExecutorWrapper(arrayOf(wrap<T1>(), wrap<T2>(), wrap<T3>(), wrap<T4>(), wrap<T5>()), block as (Any, Any?, Any?, Any?, Any?) -> Unit))
	}

	inline fun <reified T1, reified T2, reified T3, reified T4, reified T5, reified T6> whenever(noinline block: (T1, T2, T3, T4, T5, T6) -> Unit) {
		executors.add(DSLExecutorWrapper(arrayOf(wrap<T1>(), wrap<T2>(), wrap<T3>(), wrap<T4>(), wrap<T5>(), wrap<T6>()), block as (Any, Any?, Any?, Any?, Any?, Any?) -> Unit))
	}

	inline fun <reified T> wrap(): OptionallyNullableType {
		return OptionallyNullableType(T::class, null is T)
	}

	/* override fun build(): BaseDSLCommand = DSLCommand(
			*labels,
			executors = executors,
			dslSubcommands = subcommands
	) */
}