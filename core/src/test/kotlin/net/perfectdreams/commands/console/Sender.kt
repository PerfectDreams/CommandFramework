package net.perfectdreams.commands.console

interface Sender {
	fun getName(): String

	fun sendMessage(message: String)
}