package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class AsyncCoroutineCommand : DreamCommand("asynccontext") {
	@Subcommand
	suspend fun root(sender: Sender) {
		sender.sendMessage("Passe um sender!")
	}

	@Subcommand
	suspend fun asyncFriend(sender: Sender, friend: Sender) {
		sender.sendMessage("Seu amigo é ${friend.getName()}! Legal, né? Sabia que ele foi pego em uma coroutine? :O")
	}
}