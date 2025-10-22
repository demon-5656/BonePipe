# New GUI Design - v3.0.0

**Date**: 22 Oct 2024  
**Status**: ✅ COMPLETE  
**Approach**: Complete rewrite based on Mekanism best practices

---

## 🎨 Design Philosophy

**OLD GUI** (v2.x):
- Complex tab system (5 tabs: Items/Fluids/Energy/Gas/Network)
- Many custom widgets (FrequencyTextField, StatusIndicator, ToggleButton, etc.)
- Over-engineered with ~400+ lines of code
- Multiple rendering methods per tab

**NEW GUI** (v3.0.0):
- **Single-screen design** - no tabs, everything visible at once
- **Mekanism-inspired** - clean, professional layout
- **Minimal code** - ~260 lines total
- **Direct controls** - frequency input + 6 side buttons

---

## 📐 Layout Specifications

### Dimensions (Mekanism-style)
```
GUI_WIDTH = 176px
GUI_HEIGHT = 166px
BASE_Y_OFFSET = 84px (player inventory Y position)
inventoryLabelY = 72px (GUI_HEIGHT - 94, Mekanism formula)
```

### Content Areas
```
┌─────────────────────────────────────────────────────┐
│ Wireless Adapter                      Status: Active │ ← Title (y=6)
│                                                      │
│ Frequency: [____________] 🔒                         │ ← Input (y=22)
│                                                      │
│ ┌──┐ ┌──┐ ┌──┐          Mode: OUTPUT               │
│ │↑ │ │↓ │ │N │                                     │ ← Side buttons (y=40)
│ └──┘ └──┘ └──┘                                     │
│ ┌──┐ ┌──┐ ┌──┐                                     │
│ │S │ │W │ │E │                                     │
│ └──┘ └──┘ └──┘                                     │
│                                                      │
│ ───────────────────────────────────────────────────│ ← Separator (y=84)
│ [Player Inventory 9x3]                              │ ← y=84 (BASE_Y_OFFSET)
│ [Player Hotbar   9x1]                               │ ← y=142
└─────────────────────────────────────────────────────┘
```

---

## 🎮 Interaction Model

### Frequency Input
- **Type**: Standard Minecraft `EditBox`
- **Position**: (30, 22), width=100
- **Max Length**: 32 characters
- **Network**: Updates server via `UpdateFrequencyPacket`

### Side Configuration Buttons (6 buttons)
```
Directions: UP, DOWN, NORTH, SOUTH, WEST, EAST
Layout: 2 rows × 3 columns
Size: 18×18 pixels each
Spacing: 2px between buttons

Interaction:
  - Left Click → Select side (visual highlight)
  - Right Click → Cycle mode (DISABLED → OUTPUT → INPUT → BOTH → DISABLED)
  
Mode Colors:
  - OUTPUT (Extract): 🟠 Orange (0xFFFF5500)
  - INPUT (Insert):   🔵 Blue (0xFF0055FF)
  - BOTH:             🟡 Yellow (0xFFFFFF00)
  - DISABLED:         ⚪ Gray (0xFF808080)
```

---

## 🎨 Rendering

### Background (Single Blit)
```java
// Main GUI background - Mekanism approach
this.blit(poseStack, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);
```

### Side Buttons (Dynamic Rendering)
```java
for each direction:
  - Draw button background (18×18)
  - Draw direction icon (16×16)
  - Draw mode indicator (3×3 colored square at bottom-right)
```

### Labels
```java
- Title: "Wireless Adapter" at (titleLabelX, 6)
- Inventory: at (inventoryLabelX, 72)
- Frequency label: "Frequency:" at (30, 12)
- Selected mode: "Mode: OUTPUT" at (side_buttons_x + 70, side_buttons_y + 10)
- Status: "Active/Inactive" with color at (140, 12)
```

---

## 🗑️ Removed Components

### Deleted from OLD GUI:
```java
❌ Tab system (5 tabs)
❌ FrequencyTextField widget (custom class)
❌ StatusIndicator widget
❌ ToggleButton widget
❌ SideConfigWidget (replaced with inline rendering)
❌ Per-tab rendering methods:
   - renderItemsTab()
   - renderFluidsTab()
   - renderEnergyTab()
   - renderGasTab()
   - renderNetworkTab()
❌ Tab click detection logic
❌ Widget re-initialization on tab change
❌ Complex widget management
```

### Simplified to:
```java
✅ Standard EditBox for frequency
✅ Direct side button rendering
✅ Single render method
✅ Minimal state (only selectedSide)
```

---

## 📦 File Changes

### New Files:
- `AdapterScreen.java` (260 lines) - Complete rewrite

### Modified Files:
- `ClientSetup.java` - Updated import to new `AdapterScreen`

### Deleted Files:
- Old `AdapterScreen.java` (400+ lines with tab system)
- `FrequencyTextField.java` (custom widget)
- `StatusIndicator.java` (custom widget)
- `ToggleButton.java` (custom widget)
- `SideConfigWidget.java` (complex custom widget)

### Assets:
- `adapter_new.png` - Texture for new GUI (copied from old, to be customized)

---

## 🎯 Advantages

### Code Quality
```
Old GUI: ~400 lines + 4 widget files (~600 lines total)
New GUI: ~260 lines total (56% reduction)
```

### Maintainability
- **Single source of truth** - no tab switching logic
- **Standard widgets** - uses Minecraft's EditBox
- **Simple rendering** - one method, no conditionals
- **Clear interaction** - left click select, right click cycle

### User Experience
- **Everything visible** - no hunting through tabs
- **Direct manipulation** - click sides, see mode immediately
- **Fast workflow** - configure all sides without navigation
- **Visual feedback** - colored indicators show mode at a glance

### Performance
- **No widget recreation** - static layout
- **Single render pass** - no per-tab logic
- **Minimal state** - only selected side tracked
- **Efficient updates** - only frequency + side configs

---

## 🚀 Implementation Highlights

### Mekanism-Style Constants
```java
private static final int GUI_WIDTH = 176;
private static final int GUI_HEIGHT = 166;
private static final int BASE_Y_OFFSET = 84;
```

### Clean Rendering
```java
@Override
protected void renderBg(PoseStack poseStack, ...) {
    // Single blit for entire background
    this.blit(poseStack, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);
    
    // Render side configuration area
    renderSideConfigArea(poseStack, guiLeft, guiTop);
}
```

### Direct Interaction
```java
@Override
public boolean mouseClicked(double mouseX, double mouseY, int button) {
    // Check side button clicks
    if (button == 0) { // Left - select
        selectedSide = clickedDir;
    } else if (button == 1) { // Right - cycle mode
        cycleSideMode(clickedDir);
    }
}
```

---

## 📝 TODO (Future)

### Texture Work
- [ ] Create proper texture for `adapter_new.png`
  - Side button backgrounds
  - Direction icons (↑↓←→⇑⇓)
  - Mode indicators
  - Background decoration

### Enhancements (Optional)
- [ ] Tooltip on side buttons (show current mode)
- [ ] Shift+click to quickly toggle side
- [ ] Keyboard shortcuts (Tab to cycle sides?)
- [ ] Visual connection indicator (show paired adapters)

---

## 🎓 Lessons Learned

### What Worked
✅ **Complete rewrite** > incremental refactor  
✅ **Study Mekanism** > reinvent the wheel  
✅ **Standard widgets** > custom components  
✅ **Single screen** > complex navigation  

### What Didn't Work (in OLD GUI)
❌ Over-abstraction (too many custom widgets)  
❌ Tab system for simple mod  
❌ Separate rendering per resource type  
❌ Trying to be "comprehensive" instead of focused  

---

**Bottom Line**: New GUI is simpler, cleaner, faster, and more maintainable. 260 lines of clear code beats 600 lines of complex abstraction. Mekanism-style FTW! 🎉
