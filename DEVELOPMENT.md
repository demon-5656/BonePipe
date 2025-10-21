# BonePipe - Wireless Resource Transfer Mod

Minecraft 1.19.2 Forge mod providing wireless resource transfer via frequency-based adapters.

## 🎯 Features Implemented

### ✅ Core System
- **Wireless Adapter Block** - Place next to any machine for wireless connection
- **Frequency-based Networks** - Connect adapters via shared frequency (UUID + name + color)
- **Multi-Channel Support** - Items, Fluids, Energy (Mekanism chemicals planned)
- **Access Control** - Public, Private, Trusted modes
- **Side Configuration** - Per-face I/O settings

### ✅ Machine Detection
- `MachineDetector` utility for capability-based machine discovery
- Supports Forge standard capabilities:
  - `ITEM_HANDLER` (chests, furnaces, etc.)
  - `FLUID_HANDLER` (tanks, fluid machines)
  - `ENERGY` (FE/RF energy systems)
- Automatic connection/disconnection tracking

### ✅ Transfer System
- **3 Transfer Handlers** with simulate→real pattern:
  - `ItemTransferHandler` - Item stacks with overflow handling
  - `FluidTransferHandler` - Fluid transfer with tank compatibility
  - `EnergyTransferHandler` - RF/FE energy transfer
- Thread-safe concurrent collections
- Performance monitoring built-in
- Integrated with `TransferScheduler` (tick-based execution)

### ✅ GUI System
- **AdapterScreen** with 4 tabs:
  - Items - Transfer configuration, filters
  - Fluids - Fluid whitelist settings
  - Energy - Transfer rate control
  - Network - Frequency, access mode, statistics
- **Custom Widgets**:
  - `ToggleButton` - Boolean setting toggles
  - `FrequencyTextField` - Validated frequency input
  - `StatusIndicator` - Connection/activity visual feedback
- **Generated Texture** (`adapter.png` 256x256)
  - Minecraft-style GUI with tabs and icons
  - Python script for regeneration

### ✅ Network Synchronization
- `NetworkHandler` with SimpleChannel
- **Client→Server packets**:
  - `UpdateFrequencyPacket` - Change adapter frequency
  - `UpdateAccessModePacket` - Change access permissions
- **Server→Client packets**:
  - `SyncAdapterDataPacket` - Sync adapter state to GUI
- Permission validation (owner-only for sensitive operations)

### ✅ Upgrade Cards
- **4 Upgrade Types**:
  - **Speed Upgrade** - 1.5x transfer rate
  - **Filter Upgrade** - Item/fluid filtering capability
  - **Range Upgrade** - 2x wireless range
  - **Stack Upgrade** - +4 stack size per transfer
- Tooltips with stat display
- Crafting recipes included

### ✅ Crafting Recipes
- **Adapter** - Iron + Redstone + Ender Pearl
- **Speed Upgrade** - Redstone + Gold
- **Filter Upgrade** - Iron + Paper + Gold
- **Range Upgrade** - Ender Pearls + Gold
- **Stack Upgrade** - Chests + Gold

## 📦 Project Structure

```
src/main/java/com/bonepipe/
├── BonePipe.java                 # Main mod class
├── blocks/
│   ├── AdapterBlock.java         # Wireless adapter block
│   └── AdapterBlockEntity.java   # BlockEntity with tick logic
├── core/
│   └── Registration.java         # Deferred registries
├── client/
│   └── ClientSetup.java          # Client-side initialization
├── gui/
│   ├── AdapterMenu.java          # Server-side container
│   ├── AdapterScreen.java        # Client-side GUI
│   └── widgets/
│       ├── ToggleButton.java
│       ├── FrequencyTextField.java
│       └── StatusIndicator.java
├── items/
│   └── UpgradeCardItem.java      # Upgrade card system
├── network/
│   ├── FrequencyKey.java         # Network identifier
│   ├── NetworkNode.java          # Network participant
│   ├── WirelessNetwork.java      # Network container
│   ├── NetworkManager.java       # Singleton network manager
│   └── packets/
│       ├── NetworkHandler.java
│       ├── UpdateFrequencyPacket.java
│       ├── UpdateAccessModePacket.java
│       └── SyncAdapterDataPacket.java
├── transfer/
│   ├── TransferChannel.java      # Channel enum
│   ├── ITransferHandler.java     # Handler interface
│   ├── TransferScheduler.java    # Execution scheduler
│   ├── ItemTransferHandler.java
│   ├── FluidTransferHandler.java
│   └── EnergyTransferHandler.java
└── util/
    └── MachineDetector.java      # Machine capability detection

src/main/resources/
├── assets/bonepipe/
│   ├── blockstates/adapter.json
│   ├── models/
│   │   ├── block/adapter.json
│   │   └── item/adapter.json
│   ├── textures/gui/
│   │   ├── adapter.png
│   │   └── GUI_TEXTURE_SPEC.md
│   └── lang/
│       ├── en_us.json
│       ├── en_us_gui.json
│       ├── ru_ru.json
│       └── ru_ru_gui.json
└── data/bonepipe/
    └── recipes/
        ├── adapter.json
        ├── speed_upgrade.json
        ├── filter_upgrade.json
        ├── range_upgrade.json
        └── stack_upgrade.json
```

## 🚀 Technical Details

### Network Architecture
- **Singleton NetworkManager** subscribed to Forge ServerTickEvent
- **Concurrent collections** for thread safety
- **Frequency-based isolation** (UUID + string + color)
- **Performance monitoring** every 100 ticks

### Transfer Pattern
```java
// Phase 1: Simulate
ItemStack simulated = sourceHandler.extractItem(slot, amount, true);
if (simulated.isEmpty()) return false;

ItemStack remaining = destHandler.insertItem(targetSlot, simulated, true);
if (!remaining.isEmpty()) return false;

// Phase 2: Real execution
ItemStack extracted = sourceHandler.extractItem(slot, amount, false);
ItemStack leftover = destHandler.insertItem(targetSlot, extracted, false);
```

### Capability Detection
```java
BlockEntity be = level.getBlockEntity(pos);
boolean hasItems = be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).isPresent();
boolean hasFluids = be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction).isPresent();
boolean hasEnergy = be.getCapability(ForgeCapabilities.ENERGY, direction).isPresent();
```

## 📊 Statistics

- **Total Files**: 35 Java files + 15 resource files
- **Total Lines**: ~3000 lines of code
- **Commits**: 6 major commits
- **Translation Keys**: 52 (EN/RU)
- **Supported Capabilities**: 3 (Items/Fluids/Energy)

## 🔮 Future Plans (v0.2+)

- [ ] Mekanism chemical support (Gas/Infuse/Pigment/Slurry)
- [ ] Redstone signal transfer
- [ ] Upgrade slot implementation in GUI
- [ ] Item/fluid filter GUI
- [ ] Network statistics display
- [ ] Multi-dimensional support
- [ ] Load balancing algorithms
- [ ] Priority system
- [ ] Network encryption/security

## 🛠️ Development

### Requirements
- Minecraft 1.19.2
- Forge 43.3.0+
- Java 17
- Gradle 7.6

### Building
```bash
./gradlew build
```

### Testing
```bash
./gradlew runClient
./gradlew runServer
```

### GUI Texture Regeneration
```bash
python3 generate_gui_texture.py
```

## 📝 License

All rights reserved. See LICENSE file for details.

## 🤝 Credits

- Developed for Minecraft 1.19.2 Forge
- Compatible with Mekanism (planned)
- Uses Forge Capabilities API
- Inspired by EnderIO conduits and XNet

---

**Version**: 0.1.0  
**Status**: Core functionality complete, GUI basic implementation ready  
**Last Updated**: 2025-10-21
