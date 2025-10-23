#!/usr/bin/env fish

# Regenerate adapter_new.png for BonePipe GUI (fish-friendly)
# This script delegates the heavy lifting to a Python helper script.

set OUT_PATH src/main/resources/assets/bonepipe/textures/gui/adapter_new.png

echo "🖌️  Regenerating GUI texture: $OUT_PATH"

if not python3 -c "import PIL" 2>/dev/null
    echo "❗ Python Pillow (PIL) not found. Install with: pip3 install Pillow"
    exit 1
end

python3 tools/regenerate_adapter_texture.py "$OUT_PATH"

if test $status -ne 0
    echo "❌ Failed to regenerate texture"
    exit 1
end

echo "✅ Texture regenerated: $OUT_PATH"
exit 0
