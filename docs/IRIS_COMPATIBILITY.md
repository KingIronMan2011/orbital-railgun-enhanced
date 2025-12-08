# Iris Shader Pack Compatibility

## Overview

Orbital Railgun Enhanced now includes full compatibility with Iris Shaders. The mod automatically detects when Iris shader packs are active and adjusts its rendering pipeline to prevent conflicts.

## How It Works

### Automatic Detection

The mod uses the `ModDetector` utility to check if:
1. Iris mod is loaded
2. An Iris shader pack is currently active

### Rendering Behavior

#### When Iris Shader Packs Are Active

- **Item Rendering**: Switches to standard Minecraft `RenderLayer.getCutout()` for maximum compatibility
- **Post-Processing Effects**: Disables custom Satin post-processing shaders to prevent conflicts
- **Visual Quality**: Maintains visual quality while ensuring stability with shader packs

#### When Iris Shader Packs Are NOT Active

- **Item Rendering**: Uses custom GeckoLib rendering with advanced effects
- **Post-Processing Effects**: Enables full Satin shader effects for orbital strikes
- **Visual Quality**: Maximum visual fidelity with custom shader effects

## Technical Details

### Modified Components

1. **OrbitalRailgunRenderer**
   - Overrides `getRenderType()` method
   - Returns `RenderLayer.getCutout()` when Iris is active
   - Returns default GeckoLib render layer otherwise

2. **AbstractOrbitalRailgunShader**
   - Checks `ModDetector.isShaderPackActive()` in `onWorldRendered()`
   - Skips Satin shader rendering early when Iris is active
   - Prevents conflicts between Satin and Iris shader pipelines

### Code Example

```java
@Override
public RenderLayer getRenderType(OrbitalRailgunItem animatable, Identifier texture) {
    // Check if Iris shader pack is active
    if (ModDetector.isShaderPackActive()) {
        // Use standard cutout layer for shader pack compatibility
        return RenderLayer.getCutout();
    }
    
    // Use default GeckoLib rendering when no shader pack is active
    return super.getRenderType(animatable, texture);
}
```

## For Mod Developers

If you're extending this mod or using similar patterns:

1. **Always check shader pack status** before applying custom rendering
2. **Use standard RenderLayers** as fallbacks for compatibility
3. **Disable conflicting post-processing** when third-party shaders are active
4. **Test with popular shader packs** to ensure compatibility

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
