package net.perfectdreams.commands.console

class ConsoleSender(val senderName: String) : Sender {
	override fun getName(): String {
		return senderName
	}

	override fun sendMessage(message: String) {
		println(message)
	}
}