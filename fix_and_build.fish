#!/usr/bin/env fish

# Run texture regeneration, build project and install to PrismLauncher
set SCRIPT_REGEN "./regenerate_adapter_texture.fish"
set SCRIPT_INSTALL "./install_test.fish"

echo "🔁 Running regeneration script"
$SCRIPT_REGEN
if test $status -ne 0
    echo "❌ Regeneration failed"
    exit 1
end

echo "🔨 Building project (Gradle)"
./gradlew build --console=plain
if test $status -ne 0
    echo "❌ Build failed"
    exit 1
end

echo "📦 Installing to test instance"
$SCRIPT_INSTALL
if test $status -ne 0
    echo "❌ Install failed"
    exit 1
end

echo "✅ fix_and_build completed successfully"
exit 0
