#!/usr/bin/env python3#!/usr/bin/env python3

""""""

Generate proper Mekanism-style GUI texture for BonePipe Wireless AdapterGenerate adapter.png GUI texture for BonePipe mod

Creates 256x256 texture with all necessary elements INCLUDING inventory slots256x256 texture with Minecraft-style GUI elements

""""""



from PIL import Image, ImageDrawfrom PIL import Image, ImageDraw, ImageFont

import os

# Create 256x256 texture

img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))# Color palette (Minecraft GUI colors)

draw = ImageDraw.Draw(img)BG_GRAY = (198, 198, 198)

BORDER_DARK = (55, 55, 55)

# ColorsBORDER_LIGHT = (255, 255, 255)

BG_COLOR = (198, 198, 198, 255)SHADOW = (85, 85, 85)

DARK = (55, 55, 55, 255)TAB_INACTIVE = (139, 139, 139)

LIGHT = (255, 255, 255, 255)TAB_ACTIVE = (160, 160, 160)

SLOT_BG = (139, 139, 139, 255)

def draw_panel_border(draw, x, y, width, height):

def draw_slot(x, y):    """Draw Minecraft-style 3D panel border"""

    """18x18 inventory slot"""    # Dark border (bottom-right)

    draw.rectangle([x, y, x+17, y+17], outline=DARK)    draw.rectangle([x+1, y+height-1, x+width-1, y+height-1], fill=BORDER_DARK)

    draw.rectangle([x+1, y+1, x+16, y+16], fill=SLOT_BG)    draw.rectangle([x+width-1, y+1, x+width-1, y+height-1], fill=BORDER_DARK)

    draw.line([x+1, y+1, x+15, y+1], fill=LIGHT)    

    draw.line([x+1, y+1, x+1, y+15], fill=LIGHT)    # Light border (top-left)

    draw.rectangle([x, y, x+width-2, y], fill=BORDER_LIGHT)

def panel(x, y, w, h):    draw.rectangle([x, y, x, y+height-2], fill=BORDER_LIGHT)

    """3D panel"""    

    draw.rectangle([x, y, x+w-1, y+h-1], fill=BG_COLOR)    # Shadow

    draw.rectangle([x, y, x+w-1, y+h-1], outline=DARK)    draw.rectangle([x+1, y+1, x+width-2, y+1], fill=SHADOW)

    draw.line([x+1, y+1, x+w-2, y+1], fill=LIGHT)    draw.rectangle([x+1, y+1, x+1, y+height-2], fill=SHADOW)

    draw.line([x+1, y+1, x+1, y+h-2], fill=LIGHT)

def create_gui_texture():

# Main GUI (176x166)    """Create the main GUI texture"""

panel(0, 0, 176, 166)    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))

    draw = ImageDraw.Draw(img)

# Title area    

draw.rectangle([8, 6, 168, 16], fill=(180, 180, 180, 255))    # Main background (0, 0, 176, 166)

    x, y, w, h = 0, 0, 176, 166

# Frequency input    draw.rectangle([x, y, x+w, y+h], fill=BG_GRAY)

draw.rectangle([30, 22, 130, 38], fill=(0, 0, 0, 255))    draw_panel_border(draw, x, y, w, h)

draw.rectangle([30, 22, 130, 38], outline=DARK)    

    # Title area

# Status    draw.rectangle([8, 6, 168, 16], fill=(120, 120, 120))

draw.rectangle([140, 10, 168, 18], fill=(60, 60, 60, 255))    

    # Tab inactive (176, 198, 28, 32)

# Separators    tx, ty = 176, 198

draw.line([8, 18, 168, 18], fill=DARK)    draw.rectangle([tx, ty, tx+28, ty+32], fill=TAB_INACTIVE)

draw.line([8, 84, 168, 84], fill=DARK)    draw_panel_border(draw, tx, ty, 28, 32)

    

# PLAYER INVENTORY - THIS IS CRITICAL!    # Tab active (176, 166, 28, 32)

print("Drawing inventory slots...")    tx, ty = 176, 166

# Main inventory (3x9) at y=84    draw.rectangle([tx, ty, tx+28, ty+32], fill=TAB_ACTIVE)

for row in range(3):    draw_panel_border(draw, tx, ty, 28, 32)

    for col in range(9):    # Highlight active tab

        draw_slot(8 + col * 18, 84 + row * 18)    draw.rectangle([tx+2, ty+2, tx+26, ty+30], fill=(180, 180, 180))

    

# Hotbar (1x9) at y=142    # Icons (16x16 each)

for col in range(9):    # Item icon (chest) - (206, 166)

    draw_slot(8 + col * 18, 142)    ix, iy = 206, 166

    draw.rectangle([ix, iy, ix+16, iy+16], fill=(139, 90, 43))  # Brown

# Side buttons (6 buttons, 2x3 grid)    draw.rectangle([ix+4, iy+4, ix+12, iy+12], fill=(85, 55, 26))  # Dark brown

for i in range(6):    draw.line([ix+8, iy+6, ix+8, iy+10], fill=(200, 200, 200), width=2)  # Lock

    x = 8 + (i % 3) * 20    

    y = 40 + (i // 3) * 20    # Fluid icon (bucket) - (206, 182)

    panel(x, y, 18, 18)    ix, iy = 206, 182

    draw.polygon([ix+3, iy+14, ix+13, iy+14, ix+11, iy+2, ix+5, iy+2], fill=(128, 128, 128))

# Button states (widgets area)    draw.ellipse([ix+5, iy+12, ix+11, iy+16], fill=(64, 128, 255))  # Water

panel(176, 0, 18, 18)  # Normal    

draw.rectangle([176, 18, 193, 35], fill=(160, 160, 160, 255))  # Selected    # Energy icon (lightning) - (222, 166)

    ix, iy = 222, 166

# Direction icons    draw.polygon([ix+8, iy+2, ix+6, iy+8, ix+9, iy+8, ix+7, iy+14], fill=(255, 215, 0))

for i in range(6):    draw.polygon([ix+9, iy+3, ix+7, iy+8, ix+10, iy+8, ix+8, iy+13], fill=(255, 255, 100))

    x = 194 + i * 16    

    draw.rectangle([x, 0, x+15, 15], fill=(100, 100, 100, 255))    # Network icon (antenna) - (222, 182)

    draw.rectangle([x+5, 5, x+10, 10], fill=(255, 255, 255, 255))    ix, iy = 222, 182

    draw.ellipse([ix+6, iy+6, ix+10, iy+10], fill=(200, 50, 50))  # Center dot

# Save    draw.arc([ix+3, iy+3, ix+13, iy+13], 0, 360, fill=(200, 50, 50), width=2)  # Ring 1

output = 'src/main/resources/assets/bonepipe/textures/gui/adapter_new.png'    draw.arc([ix+1, iy+1, ix+15, iy+15], 0, 360, fill=(200, 50, 50), width=1)  # Ring 2

img.save(output)    

print(f"✅ Created: {output}")    # Button normal (176, 230, 20, 20)

print(f"   176x166 GUI with INVENTORY SLOTS at y=84 and y=142")    bx, by = 176, 230

    draw.rectangle([bx, by, bx+20, by+20], fill=BG_GRAY)
    draw_panel_border(draw, bx, by, 20, 20)
    
    # Button pressed (196, 230, 20, 20)
    bx, by = 196, 230
    draw.rectangle([bx, by, bx+20, by+20], fill=(150, 150, 150))
    draw.rectangle([bx+1, by+1, bx+19, by+19], fill=(120, 120, 120))
    
    return img

def main():
    print("Generating adapter.png GUI texture...")
    
    # Create texture
    texture = create_gui_texture()
    
    # Save to resources
    output_path = "src/main/resources/assets/bonepipe/textures/gui/adapter.png"
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    texture.save(output_path)
    
    print(f"✓ Created {output_path}")
    print(f"  Size: {texture.size[0]}x{texture.size[1]}")
    
    # Also save a preview
    preview = texture.resize((512, 512), Image.NEAREST)
    preview.save("adapter_preview.png")
    print("✓ Created adapter_preview.png (2x scale for preview)")

if __name__ == "__main__":
    main()
