package net.perfectdreams.commands.console

import net.perfectdreams.commands.DreamCommand

open class WithPermissionCommand(val permission: String, vararg labels: String) : DreamCommand(*labels)