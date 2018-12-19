package net.perfectdreams.commands

open class DreamCommand(override vararg val labels: String) : BaseCommand {
	override val subcommands: MutableList<BaseCommand> = mutableListOf()

	init {
		registerSubcommands()
	}
}