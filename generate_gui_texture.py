#!/usr/bin/env python3
"""
Generate adapter.png GUI texture for BonePipe mod
256x256 texture with Minecraft-style GUI elements
"""

from PIL import Image, ImageDraw, ImageFont
import os

# Color palette (Minecraft GUI colors)
BG_GRAY = (198, 198, 198)
BORDER_DARK = (55, 55, 55)
BORDER_LIGHT = (255, 255, 255)
SHADOW = (85, 85, 85)
TAB_INACTIVE = (139, 139, 139)
TAB_ACTIVE = (160, 160, 160)

def draw_panel_border(draw, x, y, width, height):
    """Draw Minecraft-style 3D panel border"""
    # Dark border (bottom-right)
    draw.rectangle([x+1, y+height-1, x+width-1, y+height-1], fill=BORDER_DARK)
    draw.rectangle([x+width-1, y+1, x+width-1, y+height-1], fill=BORDER_DARK)
    
    # Light border (top-left)
    draw.rectangle([x, y, x+width-2, y], fill=BORDER_LIGHT)
    draw.rectangle([x, y, x, y+height-2], fill=BORDER_LIGHT)
    
    # Shadow
    draw.rectangle([x+1, y+1, x+width-2, y+1], fill=SHADOW)
    draw.rectangle([x+1, y+1, x+1, y+height-2], fill=SHADOW)

def create_gui_texture():
    """Create the main GUI texture"""
    img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Main background (0, 0, 176, 166)
    x, y, w, h = 0, 0, 176, 166
    draw.rectangle([x, y, x+w, y+h], fill=BG_GRAY)
    draw_panel_border(draw, x, y, w, h)
    
    # Title area
    draw.rectangle([8, 6, 168, 16], fill=(120, 120, 120))
    
    # Tab inactive (176, 198, 28, 32)
    tx, ty = 176, 198
    draw.rectangle([tx, ty, tx+28, ty+32], fill=TAB_INACTIVE)
    draw_panel_border(draw, tx, ty, 28, 32)
    
    # Tab active (176, 166, 28, 32)
    tx, ty = 176, 166
    draw.rectangle([tx, ty, tx+28, ty+32], fill=TAB_ACTIVE)
    draw_panel_border(draw, tx, ty, 28, 32)
    # Highlight active tab
    draw.rectangle([tx+2, ty+2, tx+26, ty+30], fill=(180, 180, 180))
    
    # Icons (16x16 each)
    # Item icon (chest) - (206, 166)
    ix, iy = 206, 166
    draw.rectangle([ix, iy, ix+16, iy+16], fill=(139, 90, 43))  # Brown
    draw.rectangle([ix+4, iy+4, ix+12, iy+12], fill=(85, 55, 26))  # Dark brown
    draw.line([ix+8, iy+6, ix+8, iy+10], fill=(200, 200, 200), width=2)  # Lock
    
    # Fluid icon (bucket) - (206, 182)
    ix, iy = 206, 182
    draw.polygon([ix+3, iy+14, ix+13, iy+14, ix+11, iy+2, ix+5, iy+2], fill=(128, 128, 128))
    draw.ellipse([ix+5, iy+12, ix+11, iy+16], fill=(64, 128, 255))  # Water
    
    # Energy icon (lightning) - (222, 166)
    ix, iy = 222, 166
    draw.polygon([ix+8, iy+2, ix+6, iy+8, ix+9, iy+8, ix+7, iy+14], fill=(255, 215, 0))
    draw.polygon([ix+9, iy+3, ix+7, iy+8, ix+10, iy+8, ix+8, iy+13], fill=(255, 255, 100))
    
    # Network icon (antenna) - (222, 182)
    ix, iy = 222, 182
    draw.ellipse([ix+6, iy+6, ix+10, iy+10], fill=(200, 50, 50))  # Center dot
    draw.arc([ix+3, iy+3, ix+13, iy+13], 0, 360, fill=(200, 50, 50), width=2)  # Ring 1
    draw.arc([ix+1, iy+1, ix+15, iy+15], 0, 360, fill=(200, 50, 50), width=1)  # Ring 2
    
    # Button normal (176, 230, 20, 20)
    bx, by = 176, 230
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
