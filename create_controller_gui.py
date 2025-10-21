#!/usr/bin/env python3
from PIL import Image, ImageDraw

# Create large GUI texture (256x256 for GUI 256x220)
img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Background panel (darker bone color)
draw.rectangle([0, 0, 255, 219], fill=(139, 119, 101, 255))

# Border (lighter bone)
draw.rectangle([0, 0, 255, 219], outline=(180, 160, 140, 255), width=2)

# Title bar area
draw.rectangle([8, 6, 247, 16], fill=(100, 85, 70, 255))

# Main content area (network info)
draw.rectangle([8, 20, 247, 100], fill=(160, 140, 120, 255))

# Node list area
draw.rectangle([8, 105, 247, 210], fill=(160, 140, 120, 255))

# Save
img.save('src/main/resources/assets/bonepipe/textures/gui/controller.png')
print("✅ Controller GUI texture created!")
