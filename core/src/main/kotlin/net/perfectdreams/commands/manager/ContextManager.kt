package net.perfectdreams.commands.manager

import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
class ContextManager<SENDER> {
	val registeredContexts = mutableListOf<ContextWrapper>()

	fun <SENDER> getResult(sender: SENDER, clazz: KClass<*>, stack: Stack<String>) = registeredContexts
			.asSequence()
			.filter { it.condition.invoke(clazz) }
			.map { it.executor.invoke(sender as Any, clazz, stack) }
			.firstOrNull()

	fun registerDefaultContexts() {
		registerContext<SENDER>({ clazz: KClass<*> -> clazz == String::class }) { sender, clazz, stack ->
			stack.pop()
		}
		registerContext<SENDER>({ clazz: KClass<*> -> clazz.isSubclassOf(Enum::class) }) { sender, clazz, stack ->
			try {
				clazz.members.first { it.name == "valueOf" }
						.call(stack.pop().toUpperCase())
			} catch (e: InvocationTargetException) {
				if (e.targetException is IllegalArgumentException)
					null
				else throw e
			}
		}
		registerContext<SENDER>({ clazz: KClass<*> -> clazz == Array<String>::class }) { sender, clazz, stack ->
			val array = stack.reversed().toTypedArray()
			stack.clear()
			array
		}
	}

	fun <T1> registerContext(condition: (KClass<*>) -> (Boolean), callback: (SENDER, KClass<*>, Stack<String>) -> (Any?)) {
		registerContext(condition, callback as (Any, Any, Any) -> (Any?))
	}

	fun registerContext(condition: (KClass<*>) -> Boolean, callback: (Any, Any, Any) -> (Any?)) = registeredContexts.add(ContextWrapper(condition, callback))
}