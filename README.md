# 🦴 BonePipe v2.0.0 - Full Release

**Wireless Resource Transfer System with Network Management**

![Minecraft](https://img.shields.io/badge/Minecraft-1.19.2-green)
![Forge](https://img.shields.io/badge/Forge-43.3.0+-orange)
![Java](https://img.shields.io/badge/Java-17-blue)
![Version](https://img.shields.io/badge/Version-2.0.0-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

Полнофункциональная беспроводная система переноса ресурсов для Minecraft 1.19.2 Forge с поддержкой Mekanism Gas, системой апгрейдов, продвинутой фильтрацией и центральным управлением сетью!

## 🎯 Что такое BonePipe?

**BonePipe** - это революционный подход к переносу ресурсов в Minecraft:

- 🔌 **Wireless Transfer**: Никаких кабелей - только частоты и адаптеры
- 🧪 **Universal Support**: Items, Fluids, Energy + Mekanism Gas transfer
- ⚡ **Ultra Fast**: До **x16 скорости** с апгрейдами! (204k FE/op, 64k mB/op)
- 🎴 **5 Upgrade Types**: Speed, Stack, Filter, Capacity, Range
- 🔍 **Advanced Filtering**: До **37 filter slots** (AE2-style Capacity Cards!)
- 📊 **Network Controller**: Flux Networks-style управление всей сетью
- 📚 **JEI Integration**: Полная документация в игре
- 🔎 **TOP Support**: In-world tooltips с подробной информацией

## ✨ Ключевые особенности

### 🔌 Wireless Adapter
Основной блок системы - размещается рядом с машиной.

**Возможности**:
- ✅ Items transfer (64-96 items per operation!)
- ✅ Fluids transfer (4k-64k mB per operation!)
- ✅ Energy transfer (12.8k-204k FE per operation!)
- ✅ Mekanism Gas transfer (если Mekanism установлен)
- ✅ Per-side configuration (6 сторон отдельно)
- ✅ 4 upgrade slots (стакаются эффекты!)
- ✅ Chunk loading support

**Рецепт**: 4 Bone Blocks + 4 Ender Pearls + 1 Diamond

### 🎛️ Network Controller
Центральный блок управления сетью (аналог Flux Networks Controller).

**Возможности**:
- 📊 Показывает все адаптеры на частоте
- 📈 Real-time статистика (current/peak/average rates)
- 📍 Per-node метрики с позициями
- ⏱️ Uptime tracking
- 💡 Светится когда сеть активна (light level 15)
- 📜 Scrollable node list (large GUI 256x220)

**Рецепт**: 3 Bone Blocks + 1 Ender Pearl + 1 Redstone Block

### 🎴 Система апгрейдов (5 типов)

#### ⚡ Speed Upgrade Card
- **Эффект**: x2.0 скорость переноса
- **Стакается**: Да (x2 → x4 → x8 → x16)
- **Рецепт**: 4 Bones + 4 Redstone + 1 Diamond
- **Максимум**: **x16 скорость** с 4 картами!

#### 📦 Stack Size Upgrade Card
- **Эффект**: +8 предметов к размеру стека
- **Стакается**: Да (+8 → +16 → +24 → +32)
- **Рецепт**: 4 Bones + 4 Bones + 1 Chest
- **Максимум**: **96 предметов** (64+32) с 4 картами!

#### 🔍 Filter Upgrade Card
- **Эффект**: Включает систему фильтрации
- **Слоты**: 1 базовый filter slot
- **Рецепт**: 4 Bones + 4 Paper + 1 Hopper
- **Важно**: Требуется для работы Capacity Cards

#### 📊 Filter Capacity Upgrade Card
- **Эффект**: +9 filter slots
- **Стакается**: Да (+9 → +18 → +27 → +36)
- **Рецепт**: 8 Hoppers + 1 Bone Block
- **Максимум**: **37 слотов** (1+36) с Filter + 4 Capacity!
- **Аналог**: AE2 Capacity Cards

#### 📡 Range Upgrade Card
- **Эффект**: x2.0 радиус беспроводной связи
- **Рецепт**: 4 Bones + 4 Ender Pearls + 1 Eye of Ender
- **Примечание**: Работает если в конфиге ограничен радиус

### 🔍 Продвинутая фильтрация

**Режимы**:
- ✅ **Whitelist**: Переносить только указанные предметы
- ✅ **Blacklist**: Переносить всё кроме указанных
- ✅ **NBT-aware**: Учитывает NBT данные (зачарования, урон и т.д.)

**Слоты**:
- Базовый: 1 слот (Filter Card)
- +9 слотов за каждый Capacity Card
- Максимум: **37 слотов** (Filter + 4x Capacity)

**Динамическое расширение**: GUI автоматически показывает доступные слоты!

## 🎮 Как использовать

### Быстрый старт

1. **Скрафтите 2 Wireless Adapter**
2. **Разместите около машин** (сундук, печь, и т.д.)
3. **Настройте частоту** (одинаковую на обоих)
4. **Настройте стороны** (Extract/Insert, Items/Fluids/Energy)
5. **Готово!** Ресурсы начнут передаваться автоматически

### С апгрейдами

1. **Скрафтите Speed Upgrade Cards**
2. **Вставьте в адаптер** (до 4 штук)
3. **Наслаждайтесь скоростью!** (до x16!)

### С контроллером

1. **Скрафтите Network Controller**
2. **Разместите где угодно**
3. **Откройте GUI** и установите частоту
4. **Мониторинг** всей сети в реальном времени!

## 🔧 Интеграции

### 📚 Just Enough Items (JEI)
- ✅ Информационные страницы для всех предметов
- ✅ Подробные гайды по использованию
- ✅ Описание апгрейдов с примерами
- Нажмите `U` на любом предмете BonePipe в JEI

### 🔎 The One Probe (TOP)
- ✅ Frequency и connected machine
- ✅ Transfer rates (items/s, mB/s, FE/s)
- ✅ Установленные апгрейды (при Shift)
- ✅ Network statistics для контроллера

### 🧪 Mekanism
- ✅ **Gas Transfer** (Hydrogen, Oxygen, Chlorine, etc.)
- ⚠️ **Auto-detected**: Работает только если Mekanism установлен
- ❌ **Infusion/Pigment/Slurry**: Не поддерживаются (только Gas)
- 📖 См. [MEKANISM_SUPPORT.md](MEKANISM_SUPPORT.md) для деталей

## 📊 Сравнение с конкурентами

| Особенность | BonePipe | Pipez | Mekanism | Flux Networks |
|-------------|----------|-------|----------|---------------|
| **Беспроводность** | ✅ | ❌ | ❌ | ✅ (только Energy) |
| **Items** | ✅ | ✅ | ✅ | ❌ |
| **Fluids** | ✅ | ✅ | ✅ | ❌ |
| **Energy** | ✅ | ✅ | ✅ | ✅ |
| **Chemicals** | ✅ (Gas) | ❌ | ✅ (All) | ❌ |
| **Speed Upgrades** | ✅ (x16) | ✅ (x4) | ❌ | ❌ |
| **Filter Slots** | ✅ (37) | ✅ (9) | ❌ | ❌ |
| **Controller** | ✅ | ❌ | ❌ | ✅ |
| **Max Speed (Energy)** | 204k FE/op | 80k FE/t | ∞ | ∞ |

**Вывод**: BonePipe объединяет лучшие возможности всех конкурентов!

## 📈 Производительность

### Ultimate Tier (4x Speed Upgrades)

**Items**: 64 → **1024 items/op** (x16)  
**Fluids**: 4k → **64k mB/op** (x16)  
**Energy**: 12.8k → **204k FE/op** (x16)  
**Chemicals**: Равно Fluids

## 🎓 FAQ

**Q: Нужен ли Mekanism?**  
A: Нет! Опционален. Gas transfer включается автоматически если Mekanism установлен. Без него работают Items/Fluids/Energy.

**Q: Какие химические типы Mekanism поддерживаются?**  
A: Только **Gas** (Hydrogen, Oxygen, Chlorine и т.д.). Infusion/Pigment/Slurry не поддерживаются для упрощения.

**Q: Как увеличить скорость?**  
A: Добавьте Speed Upgrade Cards. 4 карты = x16!

**Q: Сколько adapter'ов на одной частоте?**  
A: Неограниченно!

**Q: Работает ли cross-dimension?**  
A: Нет, только в одном измерении.

**Q: Нужен ли контроллер?**  
A: Нет! Он только для мониторинга.

## 🛠️ Установка

1. Скачайте **BonePipe-2.0.0.jar**
2. Поместите в папку `mods/`
3. Запустите Minecraft 1.19.2 Forge 43.3.0+
4. *Опционально*: Установите JEI и/или TOP

## 📝 Changelog

### v2.0.0 - Full Release (2025-01-21)
- ✅ JEI Integration
- ✅ TOP Integration
- ✅ Финальное тестирование
- ✅ Полная документация

### v1.6.0 - Network Controller (2025-01-20)
- ✅ Controller block (Flux-style)
- ✅ Real-time monitoring

### v1.5.0 - Statistics (2025-01-19)
- ✅ NetworkStatistics system
- ✅ Per-frequency tracking

### v1.4.0 - Advanced Filtering (2025-01-18)
- ✅ Capacity Cards (+9 slots)
- ✅ До 37 filter slots

### v1.3.0 - Upgrades Active (2025-01-17)
- ✅ Speed x2.0, Stack +8
- ✅ Stacking до x16/+32

### v1.2.0 - Mekanism (2025-01-16)
- ✅ 4 Chemical handlers
- ✅ Ultimate speeds

### v1.1.0 - Chunkloading (2025-01-15)
- ✅ ForgeChunkManager

### v1.0.0 - Initial (2025-01-14)
- ✅ Wireless Adapter
- ✅ Basic transfers

## 📄 Лицензия

MIT License - свободное использование.

## 👨‍💻 Автор

**demon-5656** - [GitHub](https://github.com/demon-5656)

## 🙏 Благодарности

- **Mekanism** за chemical system
- **Pipez** за upgrade cards
- **Flux Networks** за controller
- **AE2** за capacity cards
- **TOP/JEI** за интеграции

---

**BonePipe v2.0.0** - Полнофункциональная беспроводная передача! 🎉

*Made with 🦴 and ❤️ for Minecraft 1.19.2*
