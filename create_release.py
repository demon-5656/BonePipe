#!/usr/bin/env python3
import os
import shutil
import subprocess

VERSION = "3.0.12"
JAR_NAME = f"bonepipe-{VERSION}.jar"
RELEASE_JAR = f"bonepipe-{VERSION}-release.jar"

JAR_PATH = os.path.join("build", "libs", JAR_NAME)
# mods dir for PrismLauncher
MODS_DIR = os.path.expanduser("~/.local/share/PrismLauncher/instances/1.19.2/minecraft/mods")

print(f"🚀 Creating BonePipe v{VERSION} release...")

# Check if JAR exists
if not os.path.isfile(JAR_PATH):
    print(f"❌ Error: {JAR_NAME} not found in build/libs/")
    print("Run ./gradlew build first")
    exit(1)


# Copy JAR for release
shutil.copy(JAR_PATH, RELEASE_JAR)
size = os.path.getsize(RELEASE_JAR) // 1024
print(f"✅ Created {RELEASE_JAR} (Size: {size}K)")

# Copy JAR to mods dir for testing
if os.path.isdir(MODS_DIR):
    # Remove old versions
    for f in os.listdir(MODS_DIR):
        if f.startswith("bonepipe-") and f.endswith(".jar"):
            os.remove(os.path.join(MODS_DIR, f))
    shutil.copy(JAR_PATH, os.path.join(MODS_DIR, JAR_NAME))
    print(f"✅ Installed: {os.path.join(MODS_DIR, JAR_NAME)}")
else:
    print(f"❌ Mods directory not found: {MODS_DIR}")

# Create release notes
notes = f"""## 🦴 BonePipe v{VERSION}

### ✨ Основные возможности
- Беспроводная передача предметов, жидкостей и газов
- Адаптеры с настройкой каналов и частоты
- Поддержка Mekanism Gas
- Базовая фильтрация
- Сохранение конфигурации адаптера
- Синхронизация данных между сервером и клиентом

### 📦 Пакет
- Minecraft: 1.19.2
- Forge: 43.3.0+
- Размер: {size}K

### 🔧 Установка
1. Скачать {RELEASE_JAR}
2. Поместить в папку mods/
3. Запустить Minecraft 1.19.2 с Forge

### 📝 Изменения в v{VERSION}
- Исправлено сохранение конфигурации адаптера
- Исправлен баг с NPE при загрузке мира
- Улучшена синхронизация данных между сервером и клиентом
"""
with open("release_notes.txt", "w", encoding="utf-8") as f:
    f.write(notes)


print(f"🌐 Creating GitHub release v{VERSION}...")
# Проверяем, существует ли релиз
check_cmd = ["gh", "release", "view", f"v{VERSION}"]
check_result = subprocess.run(check_cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
if check_result.returncode == 0:
    print(f"ℹ️ Release v{VERSION} already exists. Uploading jar...")
    upload_cmd = ["gh", "release", "upload", f"v{VERSION}", RELEASE_JAR]
    upload_result = subprocess.run(upload_cmd)
    if upload_result.returncode == 0:
        print(f"✅ File {RELEASE_JAR} uploaded to release v{VERSION}!")
        print(f"🔗 https://github.com/demon-5656/BonePipe/releases/tag/v{VERSION}")
    else:
        print(f"❌ Failed to upload file to release v{VERSION}")
        print(f"You can upload manually with:\n  {' '.join(upload_cmd)}")
        exit(1)
else:
    cmd = [
        "gh", "release", "create", f"v{VERSION}",
        RELEASE_JAR,
        "--title", f"BonePipe v{VERSION} — Stable Release",
        "--notes-file", "release_notes.txt"
    ]
    result = subprocess.run(cmd)
    if result.returncode == 0:
        print(f"✅ Release v{VERSION} created successfully!")
        print(f"🔗 https://github.com/demon-5656/BonePipe/releases/tag/v{VERSION}")
    else:
        print("❌ Failed to create release")
        print(f"You can create it manually with:\n  {' '.join(cmd)}")
        exit(1)

print(f"\n🎉 Done! Release v{VERSION} is ready for testing!")
