package de.lenox.client

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.resources.Identifier
import org.lwjgl.glfw.GLFW

object HalfbrightKeybinds {
    val HALFBRIGHT_CATEGORY: KeyMapping.Category = KeyMapping.Category.register(
        Identifier.fromNamespaceAndPath("halfbright", "halfbright")
    )

    val toggleKey = KeyMapping(
        "key.halfbright.toggle",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        HALFBRIGHT_CATEGORY,
        1
    )

    val increaseKey = KeyMapping(
        "key.halfbright.increase",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_UP,
        HALFBRIGHT_CATEGORY,
        2
    )

    val decreaseKey = KeyMapping(
        "key.halfbright.decrease",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_DOWN,
        HALFBRIGHT_CATEGORY,
        3
    )
}
