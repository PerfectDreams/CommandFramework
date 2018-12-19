package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Friend
import net.perfectdreams.commands.console.Sender

class CustomContextCommand : DreamCommand("context") {
	@Subcommand
	fun root(sender: Sender) {
		sender.sendMessage("Passe um sender!")
	}

	@Subcommand
	fun sender(sender: Sender, friend: Sender) {
		sender.sendMessage("Seu amigo é ${friend.getName()}! Legal, né?")
	}
}