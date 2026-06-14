package de.lenox.client

import de.lenox.HalfbrightConfig
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component

object HalfbrightHudRenderer {
    private var showUntil: Long = 0L

    fun show() {
        showUntil = System.currentTimeMillis() + 3000L
    }

    fun render(graphics: GuiGraphicsExtractor) {
        val remaining = showUntil - System.currentTimeMillis()
        if (remaining <= 0) return

        val mc = Minecraft.getInstance()
        // Do not render if the debug screen (F3) is open
        if (mc.debugOverlay.showDebugScreen()) return

        val font = mc.font
        val enabled = HalfbrightConfig.enabled
        val level = HalfbrightConfig.minLightLevel

        // Custom colors:
        // Desaturated green: 0x88D49E
        // Desaturated red: 0xD58B8B
        // Desaturated yellow: 0xF0D370
        // Soft label color: 0xC8C8C8
        // Divider color: 0x666666
        val component = Component.empty()
            .append(Component.literal("Halfbright: ").withStyle { it.withColor(0xC8C8C8) })
            .append(Component.literal(if (enabled) "Enabled" else "Disabled").withStyle { it.withColor(if (enabled) 0x88D49E else 0xD58B8B) })
            .append(Component.literal(" | ").withStyle { it.withColor(0x666666) })
            .append(Component.literal("Level: ").withStyle { it.withColor(0xC8C8C8) })
            .append(Component.literal(String.format(java.util.Locale.ROOT, "%.1f", level)).withStyle { it.withColor(0xF0D370) })

        val width = font.width(component)
        val x = (graphics.guiWidth() - width) / 2
        val y = graphics.guiHeight() - 80

        graphics.text(font, component, x, y, 0xFFFFFFFF.toInt(), true)
    }
}
