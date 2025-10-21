#!/usr/bin/env python3
"""
Generate block textures for BonePipe adapter
Creates 16x16 textures for top, bottom, side, and front faces
"""

from PIL import Image, ImageDraw
import os

# Color palette
DARK_GRAY = (55, 55, 55)
MID_GRAY = (100, 100, 100)
LIGHT_GRAY = (160, 160, 160)
METAL = (140, 140, 150)
REDSTONE = (200, 0, 0)
CYAN = (0, 200, 200)
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)

def create_top_texture():
    """Create top face texture (antenna view)"""
    img = Image.new('RGBA', (16, 16), DARK_GRAY)
    draw = ImageDraw.Draw(img)
    
    # Border
    draw.rectangle([0, 0, 15, 15], outline=BLACK)
    draw.rectangle([1, 1, 14, 14], outline=LIGHT_GRAY)
    
    # Center antenna circle
    for r in range(3, 0, -1):
        color = CYAN if r == 3 else LIGHT_GRAY if r == 2 else WHITE
        draw.ellipse([8-r, 8-r, 8+r, 8+r], fill=color)
    
    # Corner details
    for x, y in [(2, 2), (2, 13), (13, 2), (13, 13)]:
        draw.rectangle([x, y, x+1, y+1], fill=METAL)
    
    return img

def create_bottom_texture():
    """Create bottom face texture (simple metal)"""
    img = Image.new('RGBA', (16, 16), DARK_GRAY)
    draw = ImageDraw.Draw(img)
    
    # Border
    draw.rectangle([0, 0, 15, 15], outline=BLACK)
    
    # Grid pattern
    for i in range(4, 13, 4):
        draw.line([i, 1, i, 14], fill=MID_GRAY)
        draw.line([1, i, 14, i], fill=MID_GRAY)
    
    return img

def create_side_texture():
    """Create side face texture"""
    img = Image.new('RGBA', (16, 16), MID_GRAY)
    draw = ImageDraw.Draw(img)
    
    # Border
    draw.rectangle([0, 0, 15, 15], outline=BLACK)
    
    # Horizontal bands (half-slab appearance)
    draw.line([1, 8, 14, 8], fill=LIGHT_GRAY, width=2)
    
    # Vertical accent lines
    for x in [4, 8, 12]:
        draw.line([x, 1, x, 14], fill=DARK_GRAY)
    
    # Corner rivets
    for x in [2, 13]:
        for y in [2, 6, 13]:
            draw.rectangle([x, y, x+1, y+1], fill=METAL)
    
    return img

def create_front_texture():
    """Create front face texture (connection indicator)"""
    img = Image.new('RGBA', (16, 16), MID_GRAY)
    draw = ImageDraw.Draw(img)
    
    # Border
    draw.rectangle([0, 0, 15, 15], outline=BLACK)
    
    # Horizontal band
    draw.line([1, 8, 14, 8], fill=LIGHT_GRAY, width=2)
    
    # Connection port (center)
    draw.rectangle([6, 6, 9, 9], fill=DARK_GRAY, outline=BLACK)
    
    # LED indicator (default red/off)
    draw.ellipse([7, 7, 8, 8], fill=REDSTONE)
    
    # Side panels
    draw.rectangle([2, 3, 4, 12], fill=DARK_GRAY)
    draw.rectangle([11, 3, 13, 12], fill=DARK_GRAY)
    
    # Accent lines
    for y in [3, 6, 9, 12]:
        draw.line([2, y, 4, y], fill=METAL)
        draw.line([11, y, 13, y], fill=METAL)
    
    return img

def create_front_active_texture():
    """Create active front texture (green LED)"""
    img = create_front_texture()
    draw = ImageDraw.Draw(img)
    
    # Active LED (green)
    draw.ellipse([7, 7, 8, 8], fill=(0, 255, 0))
    
    return img

def main():
    print("Generating block textures for adapter...")
    
    # Create output directory
    output_dir = "src/main/resources/assets/bonepipe/textures/block"
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate textures
    textures = {
        "adapter_top.png": create_top_texture(),
        "adapter_bottom.png": create_bottom_texture(),
        "adapter_side.png": create_side_texture(),
        "adapter_front.png": create_front_texture(),
        "adapter_front_active.png": create_front_active_texture()
    }
    
    # Save all textures
    for filename, img in textures.items():
        path = os.path.join(output_dir, filename)
        img.save(path)
        print(f"✓ Created {path}")
        
        # Also save 4x preview
        preview = img.resize((64, 64), Image.NEAREST)
        preview_path = path.replace(".png", "_preview.png")
        preview.save(preview_path)
    
    print(f"\n✓ Generated {len(textures)} textures")
    print("Note: Use adapter_front_active.png for blockstate variants")

if __name__ == "__main__":
    main()
