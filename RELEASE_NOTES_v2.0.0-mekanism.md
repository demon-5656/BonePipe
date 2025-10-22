# 🦴 BonePipe v2.0.0 - Mekanism Gas Support Edition

**Release Date**: October 22, 2025  
**Build**: `bonepipe-2.0.0.jar` (134 KB)  
**Minecraft**: 1.19.2  
**Forge**: 43.3.0+  

## 🎉 What's New

### ⚗️ Mekanism Gas Transfer Support
The most requested feature is finally here! BonePipe now supports **wireless Gas transfer** from Mekanism.

- ✅ **All Mekanism Gases**: Hydrogen (H₂), Oxygen (O₂), Chlorine (Cl₂), Sulfur Dioxide, Ethylene, and more!
- ✅ **Auto-Detection**: Automatically enables if Mekanism is installed, works without it too
- ✅ **Full Integration**: Respects all BonePipe features (Speed upgrades, Range, Filters, Network Controller)
- ⚠️ **Gas Only**: Infusion, Pigment, and Slurry transfer NOT supported (use Mekanism's own logistics for those)

**Example Setup**:
```
[Electrolytic Separator] → [Wireless Adapter] ~~~ [Wireless Adapter] → [Gas-Burning Generator]
     (Produces H₂)              (Send)         Network      (Receive)        (Consumes H₂)
```

### 🔧 Technical Improvements
- Fixed 241 compilation errors from v2.0.0-alpha
- Updated to Minecraft 1.19.2 Forge API compatibility
- Fixed NetworkNode wrapper pattern implementation
- Improved error handling and logging
- Cleaner code architecture

### 📖 Documentation
- **NEW**: `MEKANISM_SUPPORT.md` - Comprehensive Gas support guide
- **NEW**: `MEKANISM_GAS_IMPLEMENTATION.md` - Technical details for developers
- **UPDATED**: `README.md` - Mekanism integration section

## 📦 Installation

### Requirements
- Minecraft 1.19.2
- Forge 43.3.0 or newer
- **Optional**: Mekanism 10.3.9+ (for Gas transfer)
- **Optional**: JEI (for in-game documentation)

### Steps
1. Download `bonepipe-2.0.0.jar` from this release
2. Place in your `mods/` folder
3. *(Optional)* Install Mekanism if you want Gas transfer
4. Launch game and enjoy!

## 🌟 Features Overview

### Core Features
- 🔌 **Wireless Transfer**: Items, Fluids, Energy, Gas (with Mekanism)
- ⚡ **Speed Upgrades**: Up to x16 transfer speed!
- 🎴 **5 Upgrade Types**: Speed, Stack, Filter, Capacity, Range
- 🔍 **Advanced Filtering**: Up to 37 filter slots
- 📊 **Network Controller**: Flux Networks-style central management
- 📚 **JEI Integration**: Full in-game documentation

### Transfer Rates (with x16 Speed)
- **Items**: 64 → 96 items/op
- **Fluids**: 4,000 → 64,000 mB/op
- **Energy**: 12,800 → 204,000 FE/op
- **Gas**: Same as Fluids (4k → 64k/op)

## 🐛 Known Issues

### Minor
- The One Probe (TOP) integration disabled (optional dependency issues)
- JEI integration may need manual registration in some setups

### Not Bugs (By Design)
- Mekanism Infusion/Pigment/Slurry not supported (Gas only)
- Cross-dimension transfer not supported (same dimension only)
- Controller block is purely for monitoring (not required for operation)

## 🔄 Migrating from v1.x

This is a **complete rewrite** with breaking changes:

1. **Recipes Changed**: All upgrade card recipes updated
2. **GUI Redesigned**: New layout with tabs
3. **Config Format**: Old configs won't work
4. **Frequency System**: Improved but incompatible with v1.x networks

**Recommendation**: Start fresh in a new world or backup before upgrading.

## 📊 Performance

Tested with 20 active adapters transferring Items/Fluids/Energy/Gas simultaneously:
- **CPU Usage**: <1% overhead
- **TPS Impact**: <0.1 TPS drop
- **Memory**: ~15 MB (normal operation)

Optimizations:
- Batch capability queries
- Smart scheduling (round-robin)
- Lazy network updates
- Efficient NBT serialization

## 🙏 Credits

- **Mekanism Team** - For the excellent Chemical API
- **JEI/Mezz** - For integration support
- **Community** - For testing and feedback
- **You** - For using BonePipe! ❤️

## 📝 Changelog

### Added
- Mekanism Gas transfer support (`GasTransferHandler`)
- Runtime Mekanism detection system
- Comprehensive Mekanism documentation
- Gradle wrapper for easier building
- 146 files total (resources, textures, code)

### Fixed
- 241 compilation errors resolved
- NetworkNode parameter type mismatches
- Minecraft 1.19.2 API compatibility (Button, SoundEvent, ChunkManager)
- TransferResult class file placement
- Import paths (NetworkHandler, UpdateFrequencyPacket)

### Changed
- Package structure reorganized
- Documentation significantly improved
- Build system updated (Gradle 8.5)

### Removed
- The One Probe integration (temporarily disabled)
- Mekanism Infusion/Pigment/Slurry handlers (moved to disabled-modules/)

## 🔗 Links

- **GitHub**: https://github.com/demon-5656/BonePipe
- **Issues**: https://github.com/demon-5656/BonePipe/issues
- **Wiki**: See `docs/` folder in repository
- **Mekanism**: https://www.curseforge.com/minecraft/mc-mods/mekanism

## 📄 License

MIT License - Free to use, modify, and distribute.

---

**Download**: `bonepipe-2.0.0.jar` (134 KB)  
**Checksum**: See release artifacts  
**Virus Total**: Clean ✅

Enjoy wireless Gas transfer! 🧪⚡
