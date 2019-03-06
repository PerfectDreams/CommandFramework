package net.perfectdreams.commands.manager

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.commands.ArgumentType
import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.HandlerValueWrapper
import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import java.lang.reflect.InvocationTargetException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

// Um generic type, por padrão, é nullable.
// Para não ser nullable (afinal, o COMAMND_TYPE e o SENDER jamais devem ser um tipo nullable) nós precisamos usar <T : Any>
abstract class CommandManager<SENDER : Any, COMMAND_TYPE : BaseCommand, DSL_COMMAND_TYPE : BaseDSLCommand> {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	val contextManager = ContextManager<SENDER>()
	val commandListeners = CommandListeners<SENDER, COMMAND_TYPE, DSL_COMMAND_TYPE>()

	init {
		contextManager.registerDefaultContexts()

		commandListeners.addParameterListener { sender, command, parameter, stack ->
			val injectArgumentAnnotation = parameter.findAnnotation<InjectArgument>()

			if (injectArgumentAnnotation != null) {
				return@addParameterListener when {
					injectArgumentAnnotation.argumentType == ArgumentType.PEEK_STRING -> stack.peek()
					injectArgumentAnnotation.argumentType == ArgumentType.ALL_ARGUMENTS -> stack.reversed().joinToString(" ")
					else -> throw UnsupportedOperationException("I don't know how to handle this!")
				}
			}

			return@addParameterListener null
		}
	}

	abstract fun registerCommand(command: COMMAND_TYPE)

	fun registerCommands(vararg commands: COMMAND_TYPE) {
		commands.forEach {
			registerCommand(it)
		}
	}

	abstract fun unregisterCommand(command: COMMAND_TYPE)

	fun unregisterCommands(vararg commands: COMMAND_TYPE) {
		commands.forEach {
			unregisterCommand(it)
		}
	}

	fun unregisterAllCommands() {
		// Cópia para não causar um ConcurrentModificationException
		getRegisteredCommands().toMutableList().forEach {
			unregisterCommand(it)
		}
	}

	abstract fun getRegisteredCommands(): List<COMMAND_TYPE>

	fun executeBlocking(sender: SENDER, command: COMMAND_TYPE, arguments: Array<String>) = runBlocking { execute(sender, command, arguments) }

	suspend fun execute(sender: SENDER, command: COMMAND_TYPE, arguments: Array<String>): Boolean {
		try {
			if (command is BaseDSLCommand) {
				val senderClazz = sender::class

				forMembers@ for (executorWrapper in command.executors.sortedByDescending { it.args.size }) {
					val (executorSenderClazz, isMarkedNullable) = executorWrapper.args[0]

					if (senderClazz.isSubclassOf(executorSenderClazz) || senderClazz == executorSenderClazz) {
						val stack = Stack<String>()
						arguments.reversed().forEach {
							stack.push(it)
						}

						val parameters = mutableListOf<Any?>()

						for ((clazz, nestedClazzMarkedNullable) in executorWrapper.args.drop(1)) {
							try {
								logger.debug { "Processing parameter ${stack.peek()} - ${clazz}" }
								val result = unwrap(contextManager.getResult(sender, clazz, stack))

								if (result != null) {
									parameters.add(result)
								} else {
									if (!nestedClazzMarkedNullable) {
										logger.debug { "Non nullable type found, ignoring..." }
										continue@forMembers
									}
									parameters.add(null)
								}
							} catch (e: EmptyStackException) {
								if (!nestedClazzMarkedNullable) {
									logger.debug { "Stack is empty, ignoring..." }
									continue@forMembers
								}
								parameters.add(null)
							}
						}

						logger.debug { "Stack size: ${stack.size}" }

						parameters.forEachIndexed { index, any ->
							logger.debug { "$index - $any" }
						}

						val executor = executorWrapper.executor

						logger.debug { "Executing $executor!" }

						for (callback in commandListeners.dslCommandProcessors) {
							val continuationType2 = callback.invoke(sender, command as DSL_COMMAND_TYPE)
							if (continuationType2 == CommandContinuationType.CANCEL)
								return true
							if (continuationType2 == CommandContinuationType.SKIP)
								continue@forMembers
						}

						for (callback in commandListeners.commandProcessors) {
							val continuationType2 = callback.invoke(sender, command)
							if (continuationType2 == CommandContinuationType.CANCEL)
								return true
							if (continuationType2 == CommandContinuationType.SKIP)
								continue@forMembers
						}

						when (executorWrapper.args.size) {
							1 -> (executor as (Any) -> Unit).invoke(sender)
							2 -> (executor as (Any, Any?) -> Unit).invoke(sender, parameters[0])
							3 -> (executor as (Any, Any?, Any?) -> Unit).invoke(sender, parameters[0], parameters[1])
							4 -> (executor as (Any, Any?, Any?, Any?) -> Unit).invoke(sender, parameters[0], parameters[1], parameters[2])
							5 -> (executor as (Any, Any?, Any?, Any?, Any?) -> Unit).invoke(sender, parameters[0], parameters[1], parameters[2], parameters[3])
							6 -> (executor as (Any, Any?, Any?, Any?, Any?, Any?) -> Unit).invoke(sender, parameters[0], parameters[1], parameters[2], parameters[3], parameters[4])
							else -> throw UnsupportedOperationException("You are using too many arguments for the callback! (${executorWrapper.args.size} arguments)")
						}
						return true
					}
				}

				return false
			} else {
				val commandClass = command::class

				val declaredMembers = commandClass.members

				// Estamos usando "compareBy" em vez de "sortedBy" pois é possível organizar com dois parametros diferentes, ou seja...
				// @Subcommand(["abc"]) fun(a)
				// @Subcommand fun(a, b)
				// @Subcommand(["def"]) fun(a, b, c)
				// Será...
				// @Subcommand(["def"]) fun(a, b, c)
				// @Subcommand(["abc"]) fun(a)
				// @Subcommand fun(a, b)
				logger.debug { "SORTED BOIS" }
				val sortedMembers = declaredMembers.sortedWith(compareBy({ it.findAnnotation<Subcommand>()?.labels?.isNotEmpty() }, { it.parameters.size })).reversed()
				sortedMembers.forEach {
					logger.debug { it }
				}

				forMembers@ for (member in sortedMembers) {
					val subCommandAnnotation = member.findAnnotation<Subcommand>()
					if (subCommandAnnotation != null) {
						val senderParameter = member.parameters[1]
						val senderParameterClazz = senderParameter.type.jvmErasure
						
						logger.debug { "Sender class: $senderParameterClazz" }
						logger.debug { "Is ${sender::class} not $senderParameterClazz? = ${senderParameterClazz != sender::class}" }
						logger.debug { "Is $sender a subclass of $senderParameterClazz? ${sender::class.isSubclassOf(senderParameterClazz)}" }

						if (senderParameterClazz != sender::class && !sender::class.isSubclassOf(senderParameterClazz))
							continue

						val parameters = mutableMapOf<KParameter, Any?>(
								member.instanceParameter!! to command,
								member.parameters[1] to sender
						)

						logger.debug { "Processing ${subCommandAnnotation.labels.joinToString(", ")} with $arguments - ${member}" }

						val stack = Stack<String>()
						arguments.reversed().forEach {
							stack.push(it)
						}

						// Label existe, mas o stack está vazio, ou seja: Não existe sublabel para ser processado!
						if (subCommandAnnotation.labels.isNotEmpty() && stack.empty())
							continue

						if (subCommandAnnotation.labels.isNotEmpty()) {
							logger.debug { stack.peek() }
							val matches = subCommandAnnotation.labels.contains(stack.peek())
							if (!matches)
								continue

							stack.pop()
						}

						// O primeiro sempre será o sender, então vamos ignorar
						params@ for (parameter in member.parameters.filterNot { it.kind == KParameter.Kind.INSTANCE }.drop(1)) {
							val classifier = parameter.type.classifier!!
							val clazz = classifier as KClass<*>

							logger.debug { clazz }

							try {
								val result = unwrap(getFirstNonNullValueFromParameterProcessor(sender, command, parameter, stack) ?: contextManager.getResult(sender, clazz, stack))

								if (result != null) {
									parameters[parameter] = result
								} else {
									if (!parameter.type.isMarkedNullable) {
										logger.debug { "Non nullable type found, ignoring..." }
										continue@forMembers
									}
									parameters[parameter] = null
								}
							} catch (e: EmptyStackException) {
								if (!parameter.isOptional) {
									logger.debug(e) { "Stack is empty, ignoring..." }
									continue@forMembers
								}
							}
						}

						logger.debug { "Stack size: ${stack.size}" }

						for (callback in commandListeners.methodProcessors) {
							val continuationType = callback.invoke(sender, command, member)
							if (continuationType == CommandContinuationType.CANCEL)
								return true
							if (continuationType == CommandContinuationType.SKIP)
								continue@forMembers
						}

						for (callback in commandListeners.commandProcessors) {
							val continuationType = callback.invoke(sender, command)
							if (continuationType == CommandContinuationType.CANCEL)
								return true
							if (continuationType == CommandContinuationType.SKIP)
								continue@forMembers
						}

						if (member.isSuspend) {
							member.callSuspendBy(parameters)
						} else {
							member.callBy(parameters)
						}
						return true
					}
				}
				return false
			}
		} catch (e: Throwable) {
			if (e is InvocationTargetException) {
				logger.debug { "Throwable $e, sending it to ${commandListeners.throwableProcessors.size} exception processors..." }

				for (callback in commandListeners.throwableProcessors) {
					val continuationType = callback.invoke(sender, command, e.targetException)
					logger.debug { "$callback said to $continuationType" }
					if (continuationType == CommandContinuationType.CANCEL)
						return true
				}
			}
			throw e
		}
	}

	// "Mas não dá para fazer isto só com um sequence?"
	// Não, pois é suspend, e dentro de uma sequence não pode usar métodos que utilizen suspend
	private suspend fun getFirstNonNullValueFromParameterProcessor(sender: SENDER, command: COMMAND_TYPE, parameter: KParameter, stack: Stack<String>): Any? {
		for (listener in commandListeners.parameterProcessors) {
			val result = listener.invoke(sender, command, parameter, stack)
			logger.debug { "Result is $result" }
			if (result != null)
				return result
		}
		return null
	}

	private fun unwrap(maybeWrappedValue: Any?): Any? {
		if (maybeWrappedValue !is HandlerValueWrapper)
			return maybeWrappedValue
		return maybeWrappedValue.value
	}
}