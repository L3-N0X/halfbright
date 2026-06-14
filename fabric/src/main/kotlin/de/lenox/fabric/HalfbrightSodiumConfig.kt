package de.lenox.fabric

import de.lenox.HalfbrightConfig
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier

class HalfbrightSodiumConfig : ConfigEntryPoint {
    override fun registerConfigLate(builder: ConfigBuilder) {
        builder.registerOwnModOptions()
            .setIcon(Identifier.fromNamespaceAndPath("halfbright", "textures/sodium_icon.png"))
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
                    .addOption(builder.createIntegerOption(Identifier.fromNamespaceAndPath("halfbright", "min_light_level"))
                        .setName(Component.literal("Minimum Light Level"))
                        .setTooltip(Component.literal("The minimum light level to scale from (0.0 to 15.0)."))
                        .setStorageHandler { HalfbrightConfig.save() }
                        .setBinding(
                            { value -> HalfbrightConfig.minLightLevel = value / 10.0f },
                            { (HalfbrightConfig.minLightLevel * 10).toInt() }
                        )
                        .setRange(0, 150, 5)
                        .setValueFormatter { value -> Component.literal((value / 10.0).toString()) }
                        .setDefaultValue(35)
                    )
                )
            )
    }
}
