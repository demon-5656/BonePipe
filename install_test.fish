#!/usr/bin/env fish

# BonePipe Test Installation Script
# Copies mod to Minecraft mods folder for testing

set VERSION "3.0.12"
set JAR_NAME "bonepipe-$VERSION.jar"
set MODS_DIR "$HOME/.local/share/PrismLauncher/instances/1.19.2/minecraft/mods"

echo "🔧 BonePipe v$VERSION Test Installation"
echo ""

# Check if JAR exists
if not test -f "build/libs/$JAR_NAME"
    echo "❌ Error: $JAR_NAME not found!"
    echo "Run: ./gradlew build"
    exit 1
end

# Check if mods directory exists
if not test -d "$MODS_DIR"
    echo "❌ Error: Mods directory not found:"
    echo "   $MODS_DIR"
    echo ""
    echo "Available directories:"
    find ~ -type d -name "mods" 2>/dev/null | grep minecraft
    exit 1
end

# Remove old versions
echo "🗑️  Removing old BonePipe versions..."
rm -f "$MODS_DIR"/bonepipe-*.jar
echo "✅ Cleaned old versions"

# Copy new version
echo "📦 Installing BonePipe v$VERSION..."
cp "build/libs/$JAR_NAME" "$MODS_DIR/"

if test $status -eq 0
    echo "✅ Installed: $MODS_DIR/$JAR_NAME"
    echo ""
    echo "📊 File info:"
    ls -lh "$MODS_DIR/$JAR_NAME"
    echo ""
    echo "🎮 Ready to test!"
    echo "   Launch Minecraft 1.19.2 in PrismLauncher"
else
    echo "❌ Installation failed!"
    exit 1
end
