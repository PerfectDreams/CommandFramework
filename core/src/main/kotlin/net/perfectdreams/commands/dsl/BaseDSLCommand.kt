package net.perfectdreams.commands.dsl

import net.perfectdreams.commands.BaseCommand

interface BaseDSLCommand : BaseCommand {
	val executors: List<DreamDSLExecutorWrapper>
}