package de.lenox.neoforge

import de.lenox.HalfbrightConfig
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

@ConfigEntryPointForge("halfbright")
class HalfbrightSodiumConfig : ConfigEntryPoint {
    override fun registerConfigLate(builder: ConfigBuilder) {
        builder.registerOwnModOptions()
            .setNonTintedIcon(Identifier.fromNamespaceAndPath("halfbright", "icon.png"))
            .addPage(builder.createOptionPage()
                .setName(Component.literal("Halfbright"))
                .addOptionGroup(builder.createOptionGroup()
                    .addOption(builder.createBooleanOption(Identifier.fromNamespaceAndPath("halfbright", "enabled"))
                        .setName(Component.literal("Halfbright Enabled"))
                        .setTooltip(Component.literal("Toggle halfbright brightness boost."))
                        .setStorageHandler { HalfbrightConfig.save() }
                        .setBinding(
                            { value -> HalfbrightConfig.enabled = value },
                            { HalfbrightConfig.enabled }
                        )
                        .setDefaultValue(false)
                    )
                )
            )
    }
}
