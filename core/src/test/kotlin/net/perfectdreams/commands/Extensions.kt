package net.perfectdreams.commands

import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
fun <T> DreamCommand.notNull(value: T?, message: String): T { // Contracts precisam ser fora de uma classe, então...
	kotlin.contracts.contract {
		returns() implies (value != null)
	}
	if(value == null) throw DreamCommandException(message)
	return value
}