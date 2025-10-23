#!/usr/bin/env python3
"""
Generate a basic Mekanism-like GUI texture for Adapter
Usage: tools/regenerate_adapter_texture.py <out_path>
"""
import sys
from PIL import Image, ImageDraw
import os

OUT = sys.argv[1] if len(sys.argv) > 1 else 'src/main/resources/assets/bonepipe/textures/gui/adapter_new.png'

os.makedirs(os.path.dirname(OUT), exist_ok=True)

img = Image.new('RGBA', (256,256), (0,0,0,0))
d = ImageDraw.Draw(img)

# Main panel - darker gray like Mekanism
d.rectangle([(0,0),(176,166)], fill=(198,198,198,255), outline=(85,85,85,255))

# Title bar area - lighter
d.rectangle([(4,4),(172,16)], fill=(220,220,220,255))

# Draw black rectangle for frequency input field
d.rectangle([(30,22),(130,38)], fill=(0,0,0,255), outline=(100,100,100,255))

# Inventory area background - darker
d.rectangle([(7,84),(169,166)], fill=(139,139,139,255))

# Draw inventory slots 9x3 - much darker with proper shading
for row in range(3):
    for col in range(9):
        x = 8 + col*18
        y = 84 + row*18
        # Dark slot
        d.rectangle([(x,y),(x+17,y+17)], fill=(55,55,55,255))
        # Light border top-left
        d.line([(x,y),(x+16,y)], fill=(99,99,99,255))
        d.line([(x,y),(x,y+16)], fill=(99,99,99,255))
        # Dark border bottom-right
        d.line([(x+17,y),(x+17,y+17)], fill=(0,0,0,255))
        d.line([(x,y+17),(x+17,y+17)], fill=(0,0,0,255))

# Hotbar - same style
for col in range(9):
    x = 8 + col*18
    y = 142
    d.rectangle([(x,y),(x+17,y+17)], fill=(55,55,55,255))
    d.line([(x,y),(x+16,y)], fill=(99,99,99,255))
    d.line([(x,y),(x,y+16)], fill=(99,99,99,255))
    d.line([(x+17,y),(x+17,y+17)], fill=(0,0,0,255))
    d.line([(x,y+17),(x+17,y+17)], fill=(0,0,0,255))

# Side config buttons - smaller, 3x2 grid layout with direction arrows
directions = ['UP', 'DOWN', 'NORTH', 'SOUTH', 'WEST', 'EAST']
arrows = ['↑', '↓', '↑', '↓', '←', '→']  # Visual hint in code
for i in range(6):
    col = i % 3
    row = i // 3
    bx = 8 + col*20
    by = 40 + row*20
    # Button background
    d.rectangle([(bx,by),(bx+18,by+18)], fill=(139,139,139,255), outline=(85,85,85,255))
    
    # Draw arrow/direction indicator
    cx, cy = bx + 9, by + 9  # Center of button
    if i == 0:  # UP - arrow up
        d.line([(cx, cy-4), (cx, cy+4)], fill=(255,255,255,255), width=2)
        d.line([(cx-3, cy-1), (cx, cy-4), (cx+3, cy-1)], fill=(255,255,255,255), width=1)
    elif i == 1:  # DOWN - arrow down
        d.line([(cx, cy-4), (cx, cy+4)], fill=(255,255,255,255), width=2)
        d.line([(cx-3, cy+1), (cx, cy+4), (cx+3, cy+1)], fill=(255,255,255,255), width=1)
    elif i == 2:  # NORTH - N letter
        d.text((cx-3, cy-5), 'N', fill=(255,255,255,255))
    elif i == 3:  # SOUTH - S letter  
        d.text((cx-3, cy-5), 'S', fill=(255,255,255,255))
    elif i == 4:  # WEST - arrow left
        d.line([(cx-4, cy), (cx+4, cy)], fill=(255,255,255,255), width=2)
        d.line([(cx-1, cy-3), (cx-4, cy), (cx-1, cy+3)], fill=(255,255,255,255), width=1)
    elif i == 5:  # EAST - arrow right
        d.line([(cx-4, cy), (cx+4, cy)], fill=(255,255,255,255), width=2)
        d.line([(cx+1, cy-3), (cx+4, cy), (cx+1, cy+3)], fill=(255,255,255,255), width=1)

# Save
img.save(OUT)
print('Saved', OUT)
