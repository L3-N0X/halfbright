package de.lenox.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import de.lenox.HalfbrightConfig
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

import com.mojang.brigadier.arguments.FloatArgumentType.floatArg
import com.mojang.brigadier.arguments.FloatArgumentType.getFloat

object HalfbrightCommand {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("halfbright")
                .executes { query(it) }
                .then(
                    Commands.literal("toggle")
                        .executes { toggle(it) }
                )
                .then(
                    Commands.literal("enable")
                        .executes { setEnabled(it, true) }
                )
                .then(
                    Commands.literal("disable")
                        .executes { setEnabled(it, false) }
                )
                .then(
                    Commands.literal("level")
                        .then(
                            Commands.argument("value", floatArg(0.0f, 15.0f))
                                .executes { setLevel(it) }
                        )
                )
        )
    }

    private fun query(ctx: CommandContext<CommandSourceStack>): Int {
        val state = HalfbrightConfig.enabled
        val level = HalfbrightConfig.minLightLevel
        ctx.source.sendSystemMessage(Component.literal("Halfbright is currently ${if (state) "enabled" else "disabled"} (minimum light level: $level)"))
        return 1
    }

    private fun toggle(ctx: CommandContext<CommandSourceStack>): Int {
        val newState = !HalfbrightConfig.enabled
        HalfbrightConfig.enabled = newState
        HalfbrightConfig.save()
        ctx.source.sendSystemMessage(Component.literal("Halfbright has been ${if (newState) "enabled" else "disabled"}"))
        return 1
    }

    private fun setEnabled(ctx: CommandContext<CommandSourceStack>, value: Boolean): Int {
        HalfbrightConfig.enabled = value
        HalfbrightConfig.save()
        ctx.source.sendSystemMessage(Component.literal("Halfbright has been ${if (value) "enabled" else "disabled"}"))
        return 1
    }

    private fun setLevel(ctx: CommandContext<CommandSourceStack>): Int {
        val value = getFloat(ctx, "value")
        HalfbrightConfig.minLightLevel = value
        HalfbrightConfig.save()
        ctx.source.sendSystemMessage(Component.literal("Halfbright minimum light level has been set to $value"))
        return 1
    }
}
