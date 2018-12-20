package net.perfectdreams.commands

import kotlin.reflect.full.isSubclassOf

interface BaseCommand {
	val labels: Array<out String>
	val subcommands: MutableList<BaseCommand>

	fun registerSubcommands() {
		// Registrar todos os sub comandos desta classe

		// A gente "poderia" verificar se a classe é "inner" antes de tudo, mas é claro que sempre vai ter aquela hora que não vai ter um "inner"
		// marcado e a gente vai gastar muuuito tempo tentando descobrir porque não funciona
		//
		// É melhor apenas verificar se a classe extende o DreamCommand
		this::class.nestedClasses.filter { it.isSubclassOf(BaseCommand::class) }.forEach {
			val constructor = it.constructors.firstOrNull() ?: return@forEach

			val newInstance = constructor.call(this)
			subcommands.add(newInstance as BaseCommand)
		}
	}
}