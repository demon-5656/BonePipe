#!/usr/bin/env python3
"""Generate GUI texture for adapter with channel configuration"""

from PIL import Image, ImageDraw

# GUI dimensions - MUST match exactly what's in AdapterScreen.java
WIDTH = 176
HEIGHT = 120

# Minecraft GUI texture dimensions are always 256x256 for compatibility
TEXTURE_SIZE = 256

# Java constants that we must match EXACTLY
CHANNELS_START_X = 8
CHANNELS_START_Y = 50
CHANNEL_ROW_HEIGHT = 16

# Colors (Minecraft GUI palette)
BG_COLOR = (198, 198, 198)  # Light gray background
DARK_BG = (139, 139, 139)   # Darker gray
BORDER_DARK = (55, 55, 55)  # Dark border
BORDER_LIGHT = (255, 255, 255)  # Light border/highlight
INPUT_BG = (0, 0, 0)  # Black input field
SLOT_BG = (139, 139, 139)  # Slot background

# Create 256x256 texture (standard Minecraft GUI size)
img = Image.new('RGBA', (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Draw only the actual GUI area (176x120) at top-left
# Main background
draw.rectangle([0, 0, WIDTH-1, HEIGHT-1], fill=BG_COLOR)

# Outer border (dark)
draw.rectangle([0, 0, WIDTH-1, HEIGHT-1], outline=BORDER_DARK, width=1)

# Inner highlight (light - top and left)
draw.line([(1, 1), (WIDTH-2, 1)], fill=BORDER_LIGHT, width=1)  # Top
draw.line([(1, 1), (1, HEIGHT-2)], fill=BORDER_LIGHT, width=1)  # Left

# Inner shadow (dark - bottom and right)
draw.line([(1, HEIGHT-2), (WIDTH-2, HEIGHT-2)], fill=BORDER_DARK, width=1)  # Bottom
draw.line([(WIDTH-2, 1), (WIDTH-2, HEIGHT-2)], fill=BORDER_DARK, width=1)  # Right

# Title bar area (darker panel) - y: 4-16 for title at y:6
draw.rectangle([4, 4, WIDTH-5, 17], fill=DARK_BG, outline=BORDER_DARK)

# Frequency section at y:20
# Label area "Frequency:" at x:10, y:22
draw.rectangle([8, 18, 57, 35], fill=DARK_BG, outline=BORDER_DARK)

# Input field at x:58
draw.rectangle([58, 18, 144, 35], fill=INPUT_BG, outline=BORDER_DARK)
draw.rectangle([59, 19, 143, 34], outline=BORDER_LIGHT)

# Connected machine info area - y:36-47 (text at y:38)
draw.rectangle([8, 36, WIDTH-9, 48], fill=DARK_BG, outline=BORDER_DARK)

# Channel configuration rows - starting at CHANNELS_START_Y=50
y = CHANNELS_START_Y
for i in range(4):
    # Channel row background (main area)
    draw.rectangle([CHANNELS_START_X, y, WIDTH-9, y+14], fill=BG_COLOR, outline=BORDER_DARK)
    
    # Icon area - text at x+3, y+3, so box around it
    # Icon box: x:[11-28], accommodates icon at x:11 (CHANNELS_START_X+3)
    draw.rectangle([11, y+1, 28, y+13], fill=DARK_BG, outline=BORDER_DARK)
    
    # Name area starts at x:28 (CHANNELS_START_X+20)
    # Mode area at x:103 (CHANNELS_START_X+95)
    draw.rectangle([WIDTH-74, y+1, WIDTH-11, y+13], fill=DARK_BG, outline=BORDER_DARK)
    
    y += CHANNEL_ROW_HEIGHT

# Save
img.save('src/main/resources/assets/bonepipe/textures/gui/adapter_channels.png')
print(f"✅ Generated adapter_channels.png ({TEXTURE_SIZE}x{TEXTURE_SIZE} with {WIDTH}x{HEIGHT} GUI)")
print(f"   Channels: START_Y={CHANNELS_START_Y}, ROW_HEIGHT={CHANNEL_ROW_HEIGHT}")

