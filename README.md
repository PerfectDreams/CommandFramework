<h1 align="center">🔖 Framework de Comandos para o PerfectDreams 🔖</h1>
<p align="center">
<a href="https://github.com/PerfectDreams/CommandFramework/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-AGPL%20v3-lightgray.svg"></a>
</p>
<p align="center">
<a href="https://github.com/PerfectDreams/CommandFramework/stargazers"><img src="https://img.shields.io/github/stars/PerfectDreams/CommandFramework.svg?style=social&label=Stars"></a>
<a href="https://github.com/PerfectDreams/CommandFramework/watchers"><img src="https://img.shields.io/github/watchers/PerfectDreams/CommandFramework.svg?style=social&label=Watch"></a>
</p>
<p align="center">
	<sup><i>Desenho feito por <code>💀Hiro🐦#3360</code></i></sup>
</p>
<img height="350" src="https://i.imgur.com/vEK6bA9.png" align="right">

Apenas mais outra framework bacana e incrível para facilitar a criação e despacho de comandos! Genericamente utilizável em múltiplas plataformas diferentes, mas ao mesmo tempo incrivelmente customizável para implementar funcionalidades específicas para cada plataforma.

Ela foi criada para a [Loritta](https://loritta.website/) e para o [SparklyPower](https://sparklypower.net), e foi planejada com as seguintes ideias:
* Comandos devem ser criados de uma forma simples e intuitiva.
* Mesmo sendo simples, precisa permitir customizar como os comandos para implementar funcionalidades específicas para cada plataforma.
* Precisa suportar as funcionalidades que o Kotlin oferece. (Não precisar ficar colocando `@Optional` em parâmetros, suportar argumentos padrões, suportar criação de comandos usando DSL, etc)
* Precisa ser genérica, para que seja possível implementar a framework em plataformas diversas para ter um único sistema unificado para comandos.
* Precisa suportar sub sub sub ... argumentos.
* Suportar contextos personalizados para comandos, como, por exemplo, retornar a instância de um `Player` ao digitar `Loritta`!
* Suportar Kotlin Coroutines.
* Não ser "mágico", como registrar comandos magicamente ao declarar eles.

Ela foi inspirada em várias frameworks diferentes: [Annotation Command Framework](https://github.com/aikar/commands), [Plugin Annotations](https://www.spigotmc.org/resources/api-plugin-annotations.20446/) e [KotlinBukkitAPI](https://github.com/DevSrSouza/KotlinBukkitAPI), essas frameworks são excelentes, mas cada uma tem algum problema diferente que não se "encaixa" no que a gente precisa, por exemplo:
1. Não são feitas em Kotlin, ou seja, existem coisas redudantes que não seriam necessárias (como `@Optional`) (*Annotation Command Framework*, *Plugin Annotations*)
2. Dependem de plataformas para funcionar, em vez de serem "genéricas" para usar em qualquer tipo de sistema (*Plugin Annotations*, *KotlinBukkitAPI*)
3. Apenas suporta DSL (*KotlinBukkitAPI*)

## 🤔 Tá fera, mas cadê os exemplos?

Nestes exemplos, estamos usando a [nossa implementação que fizemos para unit tests](https://github.com/PerfectDreams/CommandFramework/tree/master/core/src/test/kotlin/net/perfectdreams/commands), como você terá que implementar algumas coisas você mesmo, talvez existam algumas diferenças. (Mas nada tããããão diferente, não se preocupe!)

```kotlin
class HelloWorldCommand : DreamCommand("hello", "olá") {
	@Subcommand
	fun helloWorld(sender: Sender) {
		// Isto será executado se o usuário escrever
		// "hello"
		// "olá"
		// "hello algo_aqui_blah"
		// "olá qdhd qdqgdyqwd dhqudquhd"
		sender.sendMessage("Olá, mundo!")
	}
}
```

```kotlin
fun main() {
	val commandManager = ConsoleCommandManager() // Criar o nosso CommandManager

	commandManager.registerCommand(HelloWorldCommand()) // Registrar o nosso lindo comando

	while (true) {
		val line = readLine()!!
		commandManager.dispatch(
			ConsoleSender(),
			line
		)
	}
}
```

E é isto!

*"Que nada, cadê as implementações que você falou??"*

Verdade, elas estão aqui:
<details>
 <summary>Implementações Marotas</summary>

Não se preocupe, tem coisas aqui que você não precisará implementar (como o `Sender`), ele só está aqui para demonstrar o exemplo acima!

**Senders:**
```kotlin
interface Sender {    
   fun sendMessage(message: String)  
}
```
```kotlin
class ConsoleSender(val senderName: String) : Sender {  
   override fun sendMessage(message: String) {  
      println(message)  
   }  
}
```
**Declarações de Comandos:**
```kotlin
open class DreamCommand(override vararg val labels: String) : BaseCommand {  
   override val subcommands: MutableList<BaseCommand> = mutableListOf()  
  
   init {  
      registerSubcommands()  
   }  
}
```
```kotlin
open class DreamDSLCommand(vararg labels: String, override val executors: List<DreamDSLExecutorWrapper>, dslSubcommands: List<BaseDSLCommand>) : DreamCommand(*labels), BaseDSLCommand {  
   init {  
      // lol nope, vamos ignorar todos os subcomandos registrados pela classe principal, elas são chatas!  
  subcommands.clear()  
  
      // E colocar todos os subcomandos de DSL após iniciar  
  subcommands.addAll(dslSubcommands)  
  
      // Deste jeito ainda é possível usar o "subcommands" para adicionar subcomandos de outras classes! Yay!  
  }  
}
```
**Command Manager:**
 ```kotlin
 class ConsoleCommandManager : DispatchableCommandManager<Sender, DreamCommand, DreamDSLCommand>() {  
	private val commands = mutableListOf<DreamCommand>()  
  
	override fun registerCommand(command: DreamCommand) {  
		commands.add(command)  
	}  
  
	override fun unregisterCommand(command: DreamCommand) {  
		commands.remove(command)  
	}  
  
	override fun getRegisteredCommands(): List<DreamCommand> {  
		return commands  
	}  
  
   override fun dispatch(sender: Sender, command: DreamCommand, label: String, arguments: Array<String>, coroutineContext: CoroutineContext?): Boolean {  
      if (!command.labels.contains(label))  
         return false  
  
		for (subCommand in command.subcommands) {    
			if (dispatch(sender, subCommand as DreamCommand, arguments.drop(0).firstOrNull() ?: "", arguments.drop(1).toTypedArray(), coroutineContext))  
				return true  
		}  
		return execute(sender, command, arguments, coroutineContext)  
   }  
}
```
</details>

E é claro, é possível criar exemplos mais complexos, por exemplo:

```kotlin
class RoleplayCommand : DreamCommand("undertale") {
	@Subcommand
	fun root(sender: Sender) {
		sender.sendMessage("Para começar o roleplay, use \"undertale roleplay personagem\"")
	}

	inner class RoleplaySubCommand : DreamCommand("roleplay") {
		@Subcommand
		fun showCharacters(sender: Sender) {
			Character.values().forEach { sender.sendMessage(it.name) }
		}

		@Subcommand
		fun chooseCharacter(sender: Sender, @InjectArgument(ArgumentType.PEEK_STRING) characterName: String, character: Character?) {
			if (character == null) {
				sender.sendMessage("O personagem $characterName não existe, bobinho!")
				return
			}

			sender.sendMessage("Você escolheu $character!")
		}
	}

	enum class Character {
		ASRIEL,
		TORIEL,
		FRISK,
		CHARA
	}
}
```

Você pode até usar DSLs para criar comandos!
```kotlin
class DomainSpecificLanguageCommand {
	fun generateCommand() = command("dslexample") {
		command("easter") {
			whenever<Sender, String?> { sender, input ->
				if (input == null) {
					sender.sendMessage("Apenas os fortes que possuem a senha poderão ver o easter egg.")
					return@whenever
				}

				if (input != "lorotajubinha") {
					sender.sendMessage("Errou! $input não é a senha, tente novamente, mas agora com mais confiança!")
					return@whenever
				}

				sender.sendMessage("Parabéns, você encotrou o easter egg! Guarde ele com muito carinho ^-^ https://bit.ly/segredolori")
			}
		}

		whenever<Sender> {
			it.sendMessage("i love u :3")
		}
	}
}
```

E é isto, have fun!

## 💫 Agradecimentos especiais para...

![YourKit-Logo](https://www.yourkit.com/images/yklogo.png)

[YourKit](http://www.yourkit.com/), criadores do YourKit Java Profiler, suportam projetos open source de todos os tipos com o excelente profiler de aplicações [Java](https://www.yourkit.com/java/profiler/index.jsp) e [.NET](https://www.yourkit.com/.net/profiler/index.jsp). Nós agradecemos por darem uma licença open source para conseguir deixar nossos projetos mais incríveis e maravilhosos para todos os nossos usuários!

## 📄 Licença

O código-fonte da Loritta está licenciado sob a [GNU Affero General Public License v3.0](https://github.com/LorittaBot/Loritta/blob/master/LICENSE)

<hr>

<p align="center">
<a href="https://perfectdreams.net/open-source">
<img src="https://perfectdreams.net/assets/img/perfectdreams_opensource_iniciative_rounded.png">
</a>
</p>
