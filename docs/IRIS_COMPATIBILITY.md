# Iris Shader Pack Compatibility

## Overview

Orbital Railgun Enhanced now includes compatibility with Iris Shaders for its post-processing shader effects. The mod automatically detects when Iris shader packs are active and disables custom Satin shaders to prevent conflicts.

## How It Works

### Automatic Detection

The mod uses the `ModDetector` utility to check if:
1. Iris mod is loaded
2. An Iris shader pack is currently active

### Rendering Behavior

#### When Iris Shader Packs Are Active

- **Post-Processing Effects**: Disables custom Satin post-processing shaders to prevent conflicts
- **Visual Quality**: Maintains gameplay functionality while ensuring stability with shader packs
- **Compatibility**: Prevents rendering conflicts between Satin and Iris shader pipelines

#### When Iris Shader Packs Are NOT Active

- **Post-Processing Effects**: Enables full Satin shader effects for orbital strikes
- **Visual Quality**: Maximum visual fidelity with custom shader effects
- **Full Features**: Complete orbital strike visual effects with custom shaders

## Technical Details

### Modified Components

**AbstractOrbitalRailgunShader**
   - Checks `ModDetector.isShaderPackActive()` in `onWorldRendered()`
   - Skips Satin shader rendering early when Iris is active
   - Prevents conflicts between Satin and Iris shader pipelines
   - Affects both `OrbitalRailgunShader` and `OrbitalRailgunGuiShader`

### Code Example

```java
@Override
public void onWorldRendered(Camera camera, float tickDelta, long nanoTime) {
    // Skip Satin shader rendering if Iris shader packs are active
    // This prevents conflicts between Satin post-processing and Iris shaders
    if (ModDetector.isShaderPackActive()) {
        return;
    }
    
    if (shouldRender()) {
        // ... normal shader rendering logic
        SHADER.render(tickDelta);
    }
}
```

## For Mod Developers

If you're extending this mod or implementing similar patterns:

1. **Always check shader pack status** before applying custom post-processing shaders
2. **Use early returns** to prevent shader conflicts
3. **Disable conflicting post-processing** when third-party shaders are active
4. **Test with popular shader packs** to ensure compatibility

## Affected Features

When Iris shader packs are active, the following visual effects are disabled:
- Orbital strike post-processing effects (chromatic aberration, distortion)
- GUI shader effects when aiming the railgun
- Custom depth-based rendering effects

The core gameplay functionality remains fully operational:
- Orbital railgun item works normally
- Strike damage and mechanics unchanged
- Sound effects play correctly
- Particle effects render normally

## Compatibility

This implementation is compatible with:
- Iris 1.7.6+ on Minecraft 1.20.1/1.20.4
- All Iris-compatible shader packs
- Vanilla Minecraft rendering (when no shaders are active)

## Performance

The shader detection has minimal performance impact:
- Single boolean check per frame
- No additional rendering overhead
- Efficient early-return pattern

## Troubleshooting

If you experience rendering issues:

1. **Check Iris version**: Ensure you have Iris 1.7.6 or newer
2. **Verify shader pack**: Some shader packs may have specific requirements
3. **Try disabling the shader pack**: This will enable full custom rendering
4. **Check logs**: Look for any shader-related errors in the game log

## Future Enhancements

Potential future improvements:
- Selective shader effects based on shader pack capabilities
- Configuration options for shader compatibility mode
- Performance optimizations for specific shader packs
