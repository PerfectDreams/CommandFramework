package net.perfectdreams.commands

import net.perfectdreams.commands.annotation.InjectArgument
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender

class RoleplayCommand : DreamCommand("undertale") {
	@Subcommand
	fun root(sender: Sender) {
		sender.sendMessage("Para começar o roleplay, use \"undertale roleplay personagem\"")
	}

	inner class RoleplaySubCommand : DreamCommand("roleplay") {
		@Subcommand
		fun showCharacters(sender: Sender) {
			Character.values().forEach { sender.sendMessage(it.name) }
		}

		@Subcommand
		fun chooseCharacter(sender: Sender, @InjectArgument(ArgumentType.PEEK_STRING) characterName: String, character: Character?) {
			if (character == null) {
				sender.sendMessage("O personagem $characterName não existe, bobinho!")
				return
			}

			sender.sendMessage("Você escolheu $character!")
		}
	}

	enum class Character {
		ASRIEL,
		TORIEL,
		FRISK,
		CHARA
	}
}