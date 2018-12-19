package net.perfectdreams.commands

open class Command(override vararg val labels: String) : BaseCommand {
	override val subcommands: MutableList<BaseCommand>
		get() = mutableListOf()

	init {
		registerSubcommands()
	}
}