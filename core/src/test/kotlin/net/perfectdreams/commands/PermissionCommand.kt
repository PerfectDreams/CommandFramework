package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.annotation.SubcommandPermission
import net.perfectdreams.commands.console.Sender

class PermissionCommand : DreamCommand("permission") {
	@Subcommand
	@SubcommandPermission("loritta.savetheworld", "Sem permissão!")
	fun saveTheWorld(sender: Sender) {
		sender.sendMessage("Save the World!")
	}
}