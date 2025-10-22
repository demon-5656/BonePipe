# Mekanism Gas Support for BonePipe v2.0.0

## Overview
BonePipe v2.0.0 includes **optional support for Mekanism Gas transfer**. This allows wireless transfer of gases (Hydrogen, Oxygen, Chlorine, etc.) between machines.

## Supported Mekanism Features
✅ **Gas Transfer** - Full support for all Mekanism gases
- Hydrogen, Oxygen, Chlorine, Sulfur Dioxide, etc.
- Automatic detection via `IGasHandler` capability
- Round-robin distribution across network nodes

❌ **Not Supported** (excluded to reduce complexity):
- Infusion transfer (Carbon, Diamond, Tin, etc.)
- Pigment transfer (16 colors)
- Slurry transfer (Dirty/Clean ore slurries)

## Requirements
1. **Minecraft 1.19.2**
2. **Forge 43.3.0+**
3. **Mekanism 10.3.9+** (optional - Gas support auto-disabled if not present)
4. **BonePipe 2.0.0**

## Installation
1. Install Mekanism mod in your mods folder
2. Install BonePipe mod in your mods folder
3. Gas transfer will be automatically enabled if Mekanism is detected

## How It Works
### Automatic Detection
```java
// BonePipe checks for Mekanism at runtime
if (isMekanismLoaded()) {
    registerHandler(new GasTransferHandler());
}
```

### Gas Transfer Channel
- **Channel ID**: `MEK_GAS`
- **Priority**: Same as Forge Energy (medium)
- **Transfer Rate**: Configurable via `maxTransfersPerTick`

### Example Setup
```
[Electrolytic Separator] --> [Wireless Adapter] ~~~ [Wireless Adapter] --> [Gas-Burning Generator]
         (Produces H₂)              (Sender)      (Network)   (Receiver)        (Consumes H₂)
```

## Configuration
Gas transfer respects BonePipe's standard configuration:
- `maxTransfersPerTick` - Maximum transfers per game tick (default: 64)
- `maxPairsPerChannel` - Maximum sender-receiver pairs (default: 256)
- Upgrade cards (Speed, Range, Filter) work with gases

## Compilation Notes
### For Developers
Mekanism is included as `compileOnly` dependency from local JAR:
```gradle
compileOnly fg.deobf(files('libs/Mekanism-1.19.2-10.3.9.13.jar'))
```

The `GasTransferHandler` is conditionally loaded at runtime, so BonePipe works with or without Mekanism installed.

### Build Dependencies
- **libs/Mekanism-1.19.2-10.3.9.13.jar** - Required for compilation only
- Not included in final JAR (users must install Mekanism separately)

## Troubleshooting

### Gas transfer not working?
1. **Check Mekanism is installed**: Look for "Mekanism Gas handler registered" in logs
2. **Verify network frequency**: Both adapters must use same frequency
3. **Check machine sides**: Ensure gas handler is exposed on the connected side
4. **Confirm gas availability**: Source machine must have gas to extract

### Log Messages
```
[INFO] Mekanism Gas handler registered successfully  ← Mekanism detected
[WARN] Mekanism not found - Gas transfer disabled    ← Mekanism missing
```

## Performance
Gas transfer is optimized using:
- **Batch operations**: Multiple tanks processed per tick
- **Capability caching**: Handlers cached per machine
- **Smart scheduling**: Round-robin prevents starvation

Typical overhead: **<1% CPU** with 10-20 active gas transfers

## Future Plans
- ❓ Potential Infusion support (if requested by users)
- ❓ Pigment support for decoration mods
- ❓ Chemical mixer integration

## Credits
- **Mekanism API** - Chemical transfer system
- **BonePipe** - Wireless adapter framework
- **Community feedback** - Requested Mekanism support

---
**Version**: 2.0.0  
**Last Updated**: October 22, 2025  
**Mekanism Version**: 10.3.9.13
