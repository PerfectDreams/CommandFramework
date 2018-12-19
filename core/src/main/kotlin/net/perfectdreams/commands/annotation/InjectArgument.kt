package net.perfectdreams.commands.annotation

import net.perfectdreams.commands.ArgumentType

@Retention(AnnotationRetention.RUNTIME)
annotation class InjectArgument(
		val argumentType: ArgumentType
)