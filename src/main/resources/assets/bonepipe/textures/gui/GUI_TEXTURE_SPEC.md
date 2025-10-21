# GUI Texture Specification

## File: adapter.png
**Location**: `src/main/resources/assets/bonepipe/textures/gui/adapter.png`

### Dimensions: 256x256 pixels

### Layout:

#### Main Background (0, 0, 176, 166)
- Standard Minecraft GUI background
- Width: 176 pixels
- Height: 166 pixels

#### Tab Background - Normal State (176, 198, 28, 32)
- X: 176, Y: 198
- Width: 28 pixels
- Height: 32 pixels
- Used for inactive tabs

#### Tab Background - Selected State (176, 166, 28, 32)
- X: 176, Y: 166  
- Width: 28 pixels
- Height: 32 pixels
- Used for active tab (slightly different shade/border)

### Tab Icons (16x16 each):
- Item Icon (206, 166): Chest icon
- Fluid Icon (206, 182): Bucket icon
- Energy Icon (222, 166): Redstone icon
- Network Icon (222, 182): Antenna/frequency icon

## Color Palette:
- Background: #C6C6C6 (Minecraft GUI gray)
- Border: #373737 (Dark gray)
- Highlight: #FFFFFF (White)
- Shadow: #555555 (Medium gray)

## Notes:
- This is a placeholder specification
- Actual texture needs to be created using image editor
- Follow Minecraft's GUI style guide
- Use 9-slice scaling for background if needed
