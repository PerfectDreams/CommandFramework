package net.perfectdreams.commands.console

class TextDumperSender : Sender {
	val result = mutableListOf<String>()

	override fun getName(): String {
		return "Text Dumper"
	}

	override fun sendMessage(message: String) {
		println(message)
		result.add(message)
	}
}