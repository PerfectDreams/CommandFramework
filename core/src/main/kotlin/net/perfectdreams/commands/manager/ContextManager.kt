package net.perfectdreams.commands.manager

import net.perfectdreams.commands.HandlerValueWrapper
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
class ContextManager<SENDER> {
	val registeredContexts = mutableListOf<ContextWrapper>()

	suspend fun <SENDER> getResult(sender: SENDER, clazz: KClass<*>, stack: Stack<String>): Any? {
		// "Mas não dá para fazer isto só com um sequence?"
		// Não, pois é suspend, e dentro de uma sequence não pode usar métodos que utilizen suspend
		for (contextProcessor in registeredContexts) {
			if (!contextProcessor.condition.invoke(clazz))
				continue

			val result = contextProcessor.executor.invoke(sender as Any, clazz, stack)
			if (result != null)
				return (result as? HandlerValueWrapper)?.value ?: result
		}
		return null
	}

	fun registerDefaultContexts() {
		registerContext<SENDER>({ clazz: KClass<*> -> clazz.isSubclassOf(String::class) }) { sender, clazz, stack ->
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

	fun <T1> registerContext(condition: (KClass<*>) -> (Boolean), callback: suspend (SENDER, KClass<*>, Stack<String>) -> (Any?)) {
		registerContext(condition, callback as suspend (Any, Any, Any) -> (Any?))
	}

	fun registerContext(condition: (KClass<*>) -> Boolean, callback: suspend (Any, Any, Any) -> (Any?)) = registeredContexts.add(ContextWrapper(condition, callback))
}