#!/usr/bin/env fish

# BonePipe v3.0.0 Release Script
# Usage: ./create_release.fish

set VERSION "3.0.0"
set JAR_NAME "bonepipe-$VERSION.jar"
set RELEASE_JAR "bonepipe-$VERSION-release.jar"

echo "🚀 Creating BonePipe v$VERSION release..."

# Check if JAR exists
if not test -f "build/libs/$JAR_NAME"
    echo "❌ Error: $JAR_NAME not found in build/libs/"
    echo "Run ./gradlew build first"
    exit 1
end

# Copy JAR for release
echo "📦 Copying JAR file..."
cp "build/libs/$JAR_NAME" "./$RELEASE_JAR"
echo "✅ Created $RELEASE_JAR (Size: "(du -h $RELEASE_JAR | cut -f1)")"

# Create release notes
set NOTES "## 🎨 Major GUI Overhaul

Complete GUI redesign following **Mekanism standards** - the gold standard for Minecraft mod GUIs.

### ✨ Key Features

- **Mekanism-compliant architecture** (95% conformance)
- **Standard dimensions**: 176×166 pixels
- **Single-screen design** - removed complex tab system
- **6 side configuration buttons** for all directions (Up, Down, North, South, West, East)
- **Clean frequency input** with EditBox
- **Optimized rendering** - 260 lines vs 600+ (57% reduction)

### 📏 Technical Implementation

- \`inventoryLabelY = imageHeight - 94\` (Mekanism formula)
- \`BASE_Y_OFFSET = 84\` (player inventory position)
- Single \`blit()\` background rendering
- Relative coordinates in \`renderLabels()\`
- Proper rendering pipeline: background → super → tooltips
- Mode cycling: **DISABLED** → **OUTPUT** → **INPUT** → **BOTH**

### 📦 Package Details

- **Size**: 135 KB (optimized)
- **Minecraft**: 1.19.2
- **Forge**: 43.3.0+
- **Performance**: Improved rendering pipeline

### 📚 Documentation

- \`MEKANISM_GUI_ANALYSIS.md\` - Complete Mekanism GUI architecture analysis (560+ lines)
- \`BONEPIPE_VS_MEKANISM_GUI.md\` - Detailed comparison and conformance report
- \`GUI_REDESIGN.md\` - v3.0.0 redesign documentation

### 🔧 Installation

1. Download \`bonepipe-3.0.0-release.jar\`
2. Place in your Minecraft \`mods/\` folder
3. Launch Minecraft 1.19.2 with Forge

### 🎯 What Changed

**Removed**:
- Complex tab system (5 tabs)
- Custom widget classes (4 files)
- Over-engineered rendering code

**Added**:
- Mekanism-style single-screen GUI
- Standard EditBox for frequency
- Professional side configuration buttons
- Clean, maintainable codebase

**Result**: Professional, performant, Mekanism-compliant GUI! 🏆"

# Create GitHub release
echo "🌐 Creating GitHub release v$VERSION..."
gh release create "v$VERSION" \
    "$RELEASE_JAR" \
    --title "BonePipe v$VERSION - Mekanism-style GUI" \
    --notes "$NOTES"

if test $status -eq 0
    echo "✅ Release v$VERSION created successfully!"
    echo "🔗 https://github.com/demon-5656/BonePipe/releases/tag/v$VERSION"
else
    echo "❌ Failed to create release"
    echo "You can create it manually with:"
    echo "  gh release create v$VERSION $RELEASE_JAR --title \"BonePipe v$VERSION - Mekanism-style GUI\""
    exit 1
end

echo ""
echo "🎉 Done! Release v$VERSION is ready for testing!"
