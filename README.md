# Halfbright

A fundamentally different approach to fullbright in modern Minecraft.

Traditional fullbright mods often attempt to raise the game's gamma levels to extreme values (e.g., 1200%). While this makes things visible, it introduces severe visual issues:
- **Poor Light Curves:** Smooth lighting is completely broken, resulting in flat, unnatural transitions.
- **Color Distortion:** Intermediate light levels look washed out or shift toward a harsh yellow tint.
- **Sodium Incompatibilities:** On modern Minecraft versions (like 1.21.x), performance mods like Sodium completely bypass the vanilla shader pipeline (`lightmap.fsh`), rendering shader-based fullbright mods inactive or broken.

## How Halfbright Works

Instead of raising the global gamma, **Halfbright scales the lightmap directly on the CPU**.

1. **Lightmap Scaling:** The mod captures the 16x16 lightmap texture on the CPU.
2. **Dynamic Range Remapping:** It remaps the lower bounds of the light levels to start at a configurable minimum value (defaults to `5.0`).
3. **Curve Preservation:** By keeping all 16 steps in the lightmap and scaling them smoothly, the mod preserves Minecraft's smooth lighting curve. Shadows and caves remain visually pleasing, and nights stay dark and atmospheric while ensuring you can still clearly see your surroundings.
4. **Universal Compatibility:** By writing directly to the lightmap texture before uploading to the GPU, it works seamlessly with both vanilla rendering and custom rendering engines like **Sodium** on both **Fabric** and **NeoForge**.

---

## Features

- **Sodium Options Menu Integration:** Configurable via Sodium's Options interface with a toggle button and a slider to adjust the minimum light level.
- **Command Support:** Use `/halfbright` (with subcommands `toggle`, `enable`, `disable`, and `level <value>`).
- **Default Keybindings:**
  - `H` - Toggle Halfbright on/off.
  - `Arrow Up` - Increase the minimum light level.
  - `Arrow Down` - Decrease the minimum light level.

---

## License

This mod is available under the MIT license.
