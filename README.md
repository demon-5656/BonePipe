# 🦴 BonePipe

**Wireless Resource Transfer System for Mekanism**

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.2-green)
![Forge](https://img.shields.io/badge/Forge-43.3.0+-orange)
![Java](https://img.shields.io/badge/Java-17-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

Беспроводная система переноса ресурсов для Minecraft 1.19.2 Forge с полной интеграцией Mekanism. Никаких кабелей - только умные адаптеры и частоты!

## 🎯 Основная концепция

**BonePipe** - это революционный подход к переносу ресурсов в Minecraft:

- **Один блок адаптера** подключается к любой стороне машины Mekanism
- **Беспроводная сеть** по частотам - без визуальных кабелей
- **Универсальность**: Items, Fluids, Energy + все химикаты Mekanism (Gas, Infusion, Pigment, Slurry)
- **Нулевые буферы**: прямая передача из источника в приёмник
- **Интеллектуальная балансировка**: round-robin с приоритетами и фильтрами

## ✨ Ключевые особенности

### 🔌 Адаптер (Adapter Block)
- Размещается у любой машины Mekanism
- GUI показывает все 6 сторон машины (как в конфиге Mekanism)
- Настройка для каждой стороны: что переносить, фильтры, слоты/танки
- Поддержка всех типов ресурсов одновременно

### 📡 Беспроводная сеть
- Соединение по частоте (frequency + color + owner)
- Режимы доступа: Public / Private / Trusted
- Работает только в загруженных чанках
- Диагностика и статистика в реальном времени

### 🎴 Система апгрейдов (4 типа карт)
1. **Speed Card** - частота операций переноса
2. **Bandwidth Card** - объём за одну операцию  
3. **Filter Card** - расширенные фильтры (whitelist/blacklist, теги, NBT)
4. **Priority Card** - приоритеты и алгоритмы распределения

### 🧪 Полная совместимость с Mekanism
- **Items** через Forge Capabilities
- **Fluids** через Forge FluidHandler
- **Energy** через Forge Energy
- **Chemicals**: Gas / Infusion Type / Pigment / Slurry через Mekanism API
- Уважение к настройкам безопасности и сторон машин

## 🚀 Быстрый старт

### Установка
1. Скачайте BonePipe из [Releases](../../releases)
2. Поместите `.jar` в папку `mods`
3. Установите Mekanism (опционально, но рекомендуется)

### Базовое использование

```
1. Создайте Adapter
2. Разместите у машины (любая сторона)
3. Откройте GUI (ПКМ)
4. Настройте частоту (Network вкладка)
5. Выберите ресурсы и стороны (Items/Fluids/Energy вкладки)
6. Разместите второй Adapter на той же частоте
7. Ресурсы начнут передаваться автоматически!
```

## 📚 Документация

- [Архитектура проекта](ARCHITECTURE.md) - технические детали реализации
- [План разработки](ROADMAP.md) - дорожная карта версий
- [Wiki](../../wiki) - подробные гайды и примеры
- [API Documentation](docs/API.md) - для разработчиков

## 🔧 Системные требования

- **Minecraft**: 1.19.2
- **Forge**: 43.3.0 или выше
- **Java**: 17
- **Опционально**: Mekanism 10.3.0+ (для химикатов)

## 🎮 Примеры использования

### Пример 1: Electrolytic Separator
```
Адаптер А (у Separator):
  - Input: Water (жидкость)
  - Output: Hydrogen (газ), Oxygen (газ)
  - Частота: "hydrogen_farm"

Адаптер Б (у хранилища H₂):
  - Input: Hydrogen
  - Частота: "hydrogen_farm"

Адаптер В (у хранилища O₂):
  - Input: Oxygen
  - Частота: "hydrogen_farm"
```

### Пример 2: Chemical Infuser
```
Сеть смешивания HCl:
  - Адаптер 1: выход Hydrogen
  - Адаптер 2: выход Chlorine
  - Адаптер 3: вход в Chemical Infuser
  - Адаптер 4: выход HCl из Infuser
  
Все на частоте "hcl_production"
```

## 🗺️ Дорожная карта

- [x] **v0.1**: Базовая система (Items/Fluids/Energy, GUI, Speed/Bandwidth карты)
- [ ] **v0.2**: Mekanism Gas, Filter/Priority карты
- [ ] **v0.3**: Остальные химикаты (Infuse/Pigment/Slurry), UX улучшения
- [ ] **v0.4**: Оптимизация, профилировка, статистика
- [ ] **v1.0**: Полное тестирование на всех машинах Mekanism

## 🤝 Разработка

### Сборка из исходников
```bash
git clone git@github.com:demon-5656/BonePipe.git
cd BonePipe
./gradlew build
```

### Структура проекта
```
src/main/java/com/bonepipe/
├── core/           # Основные классы мода
├── blocks/         # Блоки и BlockEntity
├── items/          # Предметы и карты апгрейдов
├── network/        # Беспроводная сеть и менеджеры
├── transfer/       # Система переноса ресурсов
├── gui/            # GUI и контейнеры
├── integration/    # Интеграции (Mekanism)
└── util/           # Утилиты и хелперы
```

## 📋 Тестирование

Полный набор тест-кейсов включает:
- ✅ Перенос предметов между машинами
- ✅ Жидкости и газы Mekanism
- ✅ Энергия (Forge Energy)
- ✅ Все 4 типа химикатов Mekanism
- ✅ Security и side-config уважение
- ✅ Нагрузочное тестирование (30+ адаптеров)

## 🐛 Известные ограничения

- ⚠️ Работает только в загруженных чанках (без принудительного чанклоада)
- ⚠️ Никаких буферов - прямая передача
- ⚠️ Требуется Mekanism API для поддержки химикатов

## 📄 Лицензия

MIT License - см. [LICENSE](LICENSE)

## 💬 Поддержка

- 🐛 [Issue Tracker](../../issues) - сообщить о баге
- 💡 [Discussions](../../discussions) - идеи и вопросы
- 📖 [Wiki](../../wiki) - база знаний

## 🙏 Благодарности

- **Mekanism Team** - за отличный мод и API
- **Pipez** - за вдохновение системой апгрейдов
- **Forge Community** - за поддержку и документацию

---

**Сделано с ❤️ для Minecraft сообщества**
