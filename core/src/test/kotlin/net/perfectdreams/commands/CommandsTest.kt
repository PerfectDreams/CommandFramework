package net.perfectdreams.commands

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.perfectdreams.commands.annotation.CustomArgumentType
import net.perfectdreams.commands.annotation.CustomInjectArgument
import net.perfectdreams.commands.annotation.SubcommandPermission
import net.perfectdreams.commands.console.ConsoleCommandManager
import net.perfectdreams.commands.console.Friend
import net.perfectdreams.commands.console.Sender
import net.perfectdreams.commands.console.TextDumperSender
import net.perfectdreams.commands.manager.CommandContinuationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.contracts.ExperimentalContracts
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class CommandsTest {
	fun createCommandManager() = ConsoleCommandManager()

	@Test
	fun `command registering`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				SimpleCommand()
		)
		assertThat(commandManager.getRegisteredCommands().size).isEqualTo(1)

		commandManager.unregisterAllCommands()

		assertThat(commandManager.getRegisteredCommands().size).isEqualTo(0)
	}

	@Test
	fun `simple command execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				SimpleCommand()
		)
		val sender = TextDumperSender()

		val result1 = commandManager.dispatchBlocking(
				sender,
				"simple"
		)

		val result2 = commandManager.dispatchBlocking(
				sender,
				"simple goodbye"
		)

		val result3 = commandManager.dispatchBlocking(
				sender,
				"simple unknown_subcommand_must_display_ola_mundo"
		)

		val result4 = commandManager.dispatchBlocking(
				sender,
				"unknown_command"
		)

		val result = sender.result

		assertThat(result1).isTrue()
		assertThat(result2).isTrue()
		assertThat(result3).isTrue()
		assertThat(result4).isFalse()

		assertThat(result[0]).isEqualTo("Olá, mundo!")
		assertThat(result[1]).isEqualTo("Tchau, mundo!")
		assertThat(result[2]).isEqualTo("Olá, mundo!")
	}

	@Test
	fun `coroutine command execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				ApiRequestCommand()
		)

		val sender = TextDumperSender()

		val resultWithinCoroutine = runBlocking {
			commandManager.dispatch(
					sender,
					"coroutine drawn mask"
			)
		}

		val result = sender.result

		assertThat(resultWithinCoroutine).isTrue()
		assertThat(result[0]).isEqualTo("Você enviou: drawn mask")
		assertThat(result[1]).isEqualTo("Resposta da API: ksam nward")
	}

	@Test
	fun `roleplay command execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				RoleplayCommand()
		)

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"undertale"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"undertale roleplay"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"undertale roleplay frisk"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"undertale roleplay FRISK"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"undertale roleplay KRIS"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Para começar o roleplay, use \"undertale roleplay personagem\"")
		assertThat(result[1]).isEqualTo("ASRIEL")
		assertThat(result[2]).isEqualTo("TORIEL")
		assertThat(result[3]).isEqualTo("FRISK")
		assertThat(result[4]).isEqualTo("CHARA")
		assertThat(result[5]).isEqualTo("Você escolheu FRISK!")
		assertThat(result[6]).isEqualTo("Você escolheu FRISK!")
		assertThat(result[7]).isEqualTo("O personagem KRIS não existe, bobinho!")
	}

	@Test
	fun `class and dsl command execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				ClassAndDSLCommand()
		)

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"classdsl"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"classdsl dsl lori é fofis!"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Olá, mundo! Use \"classdsl dsl\" para testar a parte em DSL!")
		assertThat(result[1]).isEqualTo("Você escreveu lori é fofis!")
	}

	@Test
	fun `command with custom context execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				CustomContextCommand()
		)

		commandManager.contextManager.registerContext<Sender>(
				{ clazz: KClass<*> -> clazz == Sender::class },
				{ sender, klazz, stack ->
					val pop = stack.pop().toLowerCase()

					when (pop) {
						"loritta" -> Friend("Loritta Morenitta")
						"pantufa" -> Friend("Pantufa (Charlotte)")
						"mrpowergamerbr" -> Friend("MrPowerGamerBR")
						"luca" -> Friend("Drawn Mask")
						"toddy" -> Friend("_XxToDdYNho0Xx_")
						"jvgm45" -> Friend("JvGm45")
						"gabriel" -> Friend("MrGaabriel")
						"its_gabi" -> Friend("Its_Gabi")
						else -> null
					}
				}
		)

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"context"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"context nobody"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"context loritta"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"context pantufa"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Passe um sender!")
		assertThat(result[1]).isEqualTo("Passe um sender!")
		assertThat(result[2]).isEqualTo("Seu amigo é Loritta Morenitta! Legal, né?")
		assertThat(result[3]).isEqualTo("Seu amigo é Pantufa (Charlotte)! Legal, né?")
	}

	@Test
	fun `command made with DSL execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				DomainSpecificLanguageCommand().generateCommand()
		)

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"dslexample"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"dslexample easter"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"dslexample easter lorota jubinha"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"dslexample easter lorotajubinha"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("i love u :3")
		assertThat(result[1]).isEqualTo("Apenas os fortes que possuem a senha poderão ver o easter egg.")
		assertThat(result[2]).isEqualTo("Errou! lorota não é a senha, tente novamente, mas agora com mais confiança!")
		assertThat(result[3]).isEqualTo("Parabéns, você encotrou o easter egg! Guarde ele com muito carinho ^-^ https://bit.ly/segredolori")
	}

	@Test
	fun `annotation checking`() {
		val commandManager = createCommandManager()
		commandManager.registerCommands(
				SimpleCommand(),
				RoleplayCommand(),
				PermissionCommand()
		)

		commandManager.commandListeners.addMethodListener { sender, dreamCommand, kCallable ->
			val annotation = kCallable.findAnnotation<SubcommandPermission>()

			if (annotation == null) {
				CommandContinuationType.CONTINUE
			} else {
				sender.sendMessage(annotation.message)
				CommandContinuationType.CANCEL
			}
		}

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"permission"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Sem permissão!")
	}

	@Test
	fun `parameter checking`() {
		val commandManager = createCommandManager()
		commandManager.registerCommands(
				InjectLoriCommand()
		)

		commandManager.commandListeners.addParameterListener { sender, dreamCommand, kParameter, stack ->
			val annotation = kParameter.findAnnotation<CustomInjectArgument>()

			when (annotation?.type) {
				CustomArgumentType.LORITTA_MORENITTA -> "Loritta Morenitta"
				else -> null
			}
		}

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"lori"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Loritta Morenitta")
	}

	@Test
	fun `ayla subcommand joined string stack checking`() {
		val commandManager = createCommandManager()
		commandManager.registerCommands(
				InjectedAylaJoinedCommand()
		)

		commandManager.commandListeners.addParameterListener { sender, dreamCommand, kParameter, stack ->
			val annotation = kParameter.findAnnotation<CustomInjectArgument>()

			return@addParameterListener when (annotation?.type) {
				CustomArgumentType.ARGUMENT_LIST -> {
					println("Processing argument list!")
					if (stack.isEmpty()) {
						println("Oh no, it is empty! Return null :3")
						return@addParameterListener HandlerValueWrapper(null) // Já que queremos retornar null, temos que ser explícitos
					}

					println("Joining the stack...")
					stack.reversed().joinToString(" ")
				}
				else -> null
			}
		}

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"ayla all_arguments"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"ayla all_arguments lori é fofis! :3"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("null")
		assertThat(result[1]).isEqualTo("lori é fofis! :3")
	}

	@Test
	fun `exception checking`() {
		val commandManager = createCommandManager()
		commandManager.registerCommands(
				MagicallyExceptionallyCommand()
		)

		commandManager.commandListeners.addThrowableListener { sender, dreamCommand, exception ->
			if (exception is DreamCommandException) {
				sender.sendMessage(exception.message!!)
				return@addThrowableListener CommandContinuationType.CANCEL
			}

			return@addThrowableListener CommandContinuationType.CONTINUE
		}

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"magically"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("https://bit.ly/segredolori")
	}

	@Test
	fun `exception checking 2`() {
		val commandManager = createCommandManager()
		commandManager.registerCommands(
				SuspendableMagicallyExceptionallyCommand()
		)

		commandManager.commandListeners.addThrowableListener { sender, dreamCommand, exception ->
			if (exception is DreamCommandException) {
				sender.sendMessage(exception.message!!)
				return@addThrowableListener CommandContinuationType.CANCEL
			}

			return@addThrowableListener CommandContinuationType.CONTINUE
		}

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"magically"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("oh no!")
	}

	@Test
	fun `command with suspendable custom context execution`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				AsyncCoroutineCommand()
		)

		commandManager.contextManager.registerContext<Sender>(
				{ clazz: KClass<*> -> clazz == Sender::class },
				{ sender, klazz, stack ->
					val pop = stack.pop().toLowerCase()

					println("Processando utilizando coroutines!")
					delay(100) // API Request

					when (pop) {
						"loritta" -> Friend("Loritta Morenitta")
						"pantufa" -> Friend("Pantufa (Charlotte)")
						"mrpowergamerbr" -> Friend("MrPowerGamerBR")
						"luca" -> Friend("Drawn Mask")
						"toddy" -> Friend("_XxToDdYNho0Xx_")
						"jvgm45" -> Friend("JvGm45")
						"gabriel" -> Friend("MrGaabriel")
						"its_gabi" -> Friend("Its_Gabi")
						else -> null
					}
				}
		)

		val sender = TextDumperSender()

		runBlocking {
			assertThat(
					commandManager.dispatch(
							sender,
							"asynccontext"
					)
			).isTrue()

			assertThat(
					commandManager.dispatch(
							sender,
							"asynccontext loritta"
					)
			).isTrue()
		}

		val result = sender.result

		assertThat(result[0]).isEqualTo("Passe um sender!")
		assertThat(result[1]).isEqualTo("Seu amigo é Loritta Morenitta! Legal, né? Sabia que ele foi pego em uma coroutine? :O")
	}

	@Test
	fun `default value test`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				DefaultValuesCommand()
		)

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"default"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"default loritta"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"default loritta uwu"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Você escreveu hello_world! lori é fofa!")
		assertThat(result[1]).isEqualTo("Você escreveu loritta! lori é fofa!")
		assertThat(result[2]).isEqualTo("Você escreveu loritta! uwu")
	}

	@Test
	fun `extended base test`() {
		val commandManager = createCommandManager()
		commandManager.registerCommand(
				ExtendedCommand("extended")
		)

		val sender = TextDumperSender()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"extended"
				)
		).isTrue()

		assertThat(
				commandManager.dispatchBlocking(
						sender,
						"extended fofis"
				)
		).isTrue()

		val result = sender.result

		assertThat(result[0]).isEqualTo("Olá Teste!")
		assertThat(result[1]).isEqualTo(":3 https://twitter.com/Haaataoh/status/1103377726432571392")
	}
}