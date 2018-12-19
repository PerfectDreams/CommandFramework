package net.perfectdreams.commands.manager

import kotlin.reflect.KClass

class ContextWrapper(
		val condition: (KClass<*>) -> Boolean,
		val executor: (Any, Any, Any) -> Any?
)