package net.perfectdreams.commands.dsl

class DreamDSLExecutorWrapper(
		val args: Array<OptionallyNullableType>,
		val executor: Any // Esse tal de "Any" é na verdade um callback (..., ...?, ...?) -> Unit, o primeiro sempre será non-null (sender!)
)