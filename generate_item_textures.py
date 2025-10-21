#!/usr/bin/env python3
"""
Generate item textures for upgrade cards
Creates 16x16 textures for each upgrade type
"""

from PIL import Image, ImageDraw
import os

# Color palette
GOLD = (255, 215, 0)
GOLD_DARK = (200, 170, 0)
GOLD_LIGHT = (255, 240, 100)
REDSTONE = (200, 0, 0)
CYAN = (0, 200, 200)
GREEN = (0, 200, 0)
PURPLE = (150, 0, 200)
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
GRAY = (100, 100, 100)

def create_base_card():
    """Create base card template"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Card background (gold)
    draw.rectangle([2, 1, 13, 14], fill=GOLD, outline=GOLD_DARK)
    draw.rectangle([3, 2, 12, 13], outline=GOLD_LIGHT)
    
    # Corner details
    for x, y in [(2, 1), (2, 14), (13, 1), (13, 14)]:
        draw.point((x, y), fill=GOLD_LIGHT)
    
    return img, draw

def create_speed_upgrade():
    """Speed upgrade - lightning bolt"""
    img, draw = create_base_card()
    
    # Lightning bolt (red/yellow)
    points = [(8, 3), (7, 7), (9, 7), (7, 12)]
    for i in range(len(points)-1):
        draw.line([points[i], points[i+1]], fill=REDSTONE, width=2)
    
    # Highlights
    draw.line([(8, 4), (8, 6)], fill=(255, 100, 0))
    
    return img

def create_filter_upgrade():
    """Filter upgrade - filter/funnel icon"""
    img, draw = create_base_card()
    
    # Funnel shape (cyan)
    draw.polygon([(5, 4), (10, 4), (8, 8), (7, 8)], fill=CYAN, outline=BLACK)
    draw.rectangle([7, 8, 8, 12], fill=CYAN, outline=BLACK)
    
    # Grid pattern
    for y in [5, 6]:
        draw.line([(6, y), (9, y)], fill=WHITE)
    
    return img

def create_range_upgrade():
    """Range upgrade - radar/signal waves"""
    img, draw = create_base_card()
    
    # Center dot
    draw.ellipse([7, 7, 8, 8], fill=PURPLE)
    
    # Signal waves (purple/magenta)
    for r in [2, 4, 6]:
        # Top-left arc
        draw.arc([7-r, 7-r, 8+r, 8+r], 180, 270, fill=PURPLE, width=1)
        # Bottom-right arc
        draw.arc([7-r, 7-r, 8+r, 8+r], 0, 90, fill=PURPLE, width=1)
    
    return img

def create_stack_upgrade():
    """Stack upgrade - stacked boxes"""
    img, draw = create_base_card()
    
    # Three stacked boxes (green)
    boxes = [
        ([5, 9, 10, 12], GREEN),
        ([6, 6, 9, 9], GREEN),
        ([7, 3, 8, 6], GREEN)
    ]
    
    for box, color in boxes:
        draw.rectangle(box, fill=color, outline=BLACK)
        # Shine
        x1, y1, x2, y2 = box
        draw.line([(x1+1, y1+1), (x2-1, y1+1)], fill=WHITE)
    
    return img

def main():
    print("Generating upgrade card textures...")
    
    # Create output directory
    output_dir = "src/main/resources/assets/bonepipe/textures/item"
    os.makedirs(output_dir, exist_ok=True)
    
    # Generate textures
    textures = {
        "speed_upgrade.png": create_speed_upgrade(),
        "filter_upgrade.png": create_filter_upgrade(),
        "range_upgrade.png": create_range_upgrade(),
        "stack_upgrade.png": create_stack_upgrade()
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
    
    print(f"\n✓ Generated {len(textures)} upgrade card textures")

if __name__ == "__main__":
    main()
