package net.perfectdreams.commands.console

class Friend(val senderName: String) : Sender {
	override fun getName(): String {
		return senderName
	}

	override fun sendMessage(message: String) {}
}