package net.perfectdreams.commands.manager

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.commands.ArgumentType
import net.perfectdreams.commands.BaseCommand
import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.dsl.BaseDSLCommand
import java.util.*
import javax.xml.bind.JAXBElement
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.*

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

	fun execute(sender: SENDER, command: COMMAND_TYPE, arguments: Array<String>, coroutineContext: CoroutineContext? = null): Boolean {
		if (command is BaseDSLCommand) {
			val senderClazz = sender::class

			forMembers@for (executorWrapper in command.executors.sortedByDescending { it.args.size }) {
				val (executorSenderClazz, isMarkedNullable) = executorWrapper.args[0]

				if (senderClazz.isSubclassOf(executorSenderClazz) || senderClazz == executorSenderClazz) {
					val stack = Stack<String>()
					arguments.reversed().forEach {
						stack.push(it)
					}

					val parameters = mutableListOf<Any?>()


					for ((clazz, nestedClazzMarkedNullable) in executorWrapper.args.drop(1)) {
						try {
							val result = contextManager.getResult(sender, clazz, stack)

							if (result != null) {
								parameters.add(result)
							} else {
								if (!nestedClazzMarkedNullable) {
									logger.debug { "Non nullable type found, ignoring..." }
									continue@forMembers
								}
								parameters.add(null)
							}
						}
						catch (e: EmptyStackException) {
							if (!nestedClazzMarkedNullable) {
								logger.debug { "Stack is empty, ignoring..." }
								continue@forMembers
							}
							parameters.add(null)
						}
					}

					logger.debug { "Stack size: ${stack.size}" }

					parameters.forEachIndexed { index, any ->
						println("$index - $any")
					}

					val executor = executorWrapper.executor

					logger.debug { "Executing $executor!" }

					val continuationType1 = commandListeners.commandDslProcessor?.invoke(sender, command as DSL_COMMAND_TYPE)
					if (continuationType1 == CommandContinuationType.CANCEL)
						return true
					if (continuationType1 == CommandContinuationType.SKIP)
						continue@forMembers

					val continuationType2 = commandListeners.commandProcessor?.invoke(sender, command)
					if (continuationType2 == CommandContinuationType.CANCEL)
						return true
					if (continuationType2 == CommandContinuationType.SKIP)
						continue@forMembers

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

			val declaredMembers = commandClass.declaredMembers

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
						println(stack.peek())
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
							val injectArgumentAnnotation = parameter.findAnnotation<InjectArgument>()

							if (injectArgumentAnnotation != null) {
								parameters[parameter] = when {
									injectArgumentAnnotation.argumentType == ArgumentType.PEEK_STRING -> stack.peek()
									else -> throw UnsupportedOperationException("I don't know how to handle this!")
								}
							} else {
								val result = commandListeners.commandParameterProcessor?.invoke(sender, command, parameter) ?: contextManager.getResult(sender, clazz, stack)

								if (result != null) {
									parameters[parameter] = result
								} else {
									if (!parameter.type.isMarkedNullable) {
										logger.debug { "Non nullable type found, ignoring..." }
										continue@forMembers
									}
									parameters[parameter] = null
								}
							}
						} catch (e: EmptyStackException) {
							logger.debug { "Stack is empty, ignoring..." }
							continue@forMembers
						}
					}

					logger.debug { "Stack size: ${stack.size}" }

					val continuationType1 = commandListeners.commandMethodProcessor?.invoke(sender, command, member)
					if (continuationType1 == CommandContinuationType.CANCEL)
						return true
					if (continuationType1 == CommandContinuationType.SKIP)
						continue@forMembers

					val continuationType2 = commandListeners.commandProcessor?.invoke(sender, command)
					if (continuationType2 == CommandContinuationType.CANCEL)
						return true
					if (continuationType2 == CommandContinuationType.SKIP)
						continue@forMembers

					if (member.isSuspend) {
						if (coroutineContext == null)
							throw IllegalStateException("Member ${member.name} is marked with suspend, but we don't have a coroutine context!")
						GlobalScope.launch(coroutineContext) {
							member.callSuspendBy(parameters)
						}
					} else {
						member.callBy(parameters)
					}
					return true
				}
			}
			return false
		}
	}
}