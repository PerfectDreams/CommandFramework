package net.perfectdreams.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.commands.console.Sender
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext

class SuspendableMagicallyExceptionallyCommand : DreamCommand("magically") {
	@ExperimentalContracts
	@Subcommand
	suspend fun magically(sender: Sender) {
		withContext(Dispatchers.IO) {
			notNull(null, "oh no!")
		}
	}
}