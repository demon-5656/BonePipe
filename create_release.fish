#!/usr/bin/env fish

# BonePipe v3.0.0 Release Script
# Usage: ./create_release.fish

set VERSION "0.13"
set JAR_NAME "bonepipe-$VERSION.jar"
set RELEASE_JAR "bonepipe-$VERSION-release.jar"

echo "🚀 Creating BonePipe v$VERSION release..."

# Check if JAR exists
if not test -f "build/libs/$JAR_NAME"
    echo "❌ Error: $JAR_NAME not found in build/libs/"
    echo "Run ./gradlew build first"
    printf "## 🦴 BonePipe v%s\n\n" $VERSION > release_notes.txt
    printf "### ✨ Основные возможности\n" >> release_notes.txt
    printf "- Беспроводная передача предметов, жидкостей и газов\n" >> release_notes.txt
    printf "- Адаптеры с настройкой каналов и частоты\n" >> release_notes.txt
    printf "- Поддержка Mekanism Gas\n" >> release_notes.txt
    printf "- Базовая фильтрация\n" >> release_notes.txt
    printf "- Сохранение конфигурации адаптера\n" >> release_notes.txt
    printf "- Синхронизация данных между сервером и клиентом\n\n" >> release_notes.txt
    printf "### 📦 Пакет\n" >> release_notes.txt
    printf "- Minecraft: 1.19.2\n" >> release_notes.txt
    printf "- Forge: 43.3.0+\n" >> release_notes.txt
    printf "- Размер: %s\n\n" (du -h $RELEASE_JAR | cut -f1) >> release_notes.txt
    printf "### 🔧 Установка\n" >> release_notes.txt
    printf "1. Скачать %s\n" $RELEASE_JAR >> release_notes.txt
    printf "2. Поместить в папку mods/\n" >> release_notes.txt
    printf "3. Запустить Minecraft 1.19.2 с Forge\n\n" >> release_notes.txt
    printf "### 📝 Изменения в v%s\n" $VERSION >> release_notes.txt
    printf "- Исправлено сохранение конфигурации адаптера\n" >> release_notes.txt
    printf "- Исправлен баг с NPE при загрузке мира\n" >> release_notes.txt
    printf "- Улучшена синхронизация данных между сервером и клиентом\n" >> release_notes.txt

    echo "🌐 Creating GitHub release v$VERSION..."
    gh release create "v$VERSION" $RELEASE_JAR --title "BonePipe v$VERSION — Stable Release" --notes-file release_notes.txt
- Исправлено сохранение конфигурации адаптера
- Исправлен баг с NPE при загрузке мира
- Улучшена синхронизация данных между сервером и клиентом
"
    # Передача файла без кавычек, fish корректно интерпретирует переменную

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
