# 🦴 BonePipe - Текущий статус проекта

**Версия**: v2.1.0  
**Дата**: 22 октября 2025  
**Статус**: ✅ Стабильная сборка

---

## 📦 Что сейчас работает

### ✅ Основные блоки

#### 🔌 Wireless Adapter
**Местоположение**: `src/main/java/com/bonepipe/blocks/AdapterBlock.java`

**Функционал**:
- ✅ Беспроводной перенос ресурсов через частоты
- ✅ Подключение к любой машине (Items/Fluids/Energy/Gas)
- ✅ Per-side конфигурация (6 сторон независимо)
- ✅ Поддержка апгрейдов (4 слота внутри)
- ✅ **Новое в v2.1.0**: Установка апгрейдов правым кликом
- ✅ Chunk loading support
- ✅ Автоопределение подключенной машины
- ✅ Сохранение конфигурации в NBT

**Взаимодействие**:
- **ПКМ с апгрейдом** → устанавливает карту в адаптер
- **ПКМ** → открывает GUI
- **Shift + ПКМ** → поворачивает блок (6 направлений)

**Рецепт**: 4 Bone Blocks + 4 Ender Pearls + 1 Diamond

#### 🎛️ Network Controller
**Местоположение**: `src/main/java/com/bonepipe/blocks/ControllerBlock.java`

**Функционал**:
- ✅ Центральное управление сетью
- ✅ Показывает все адаптеры на частоте
- ✅ Real-time статистика (текущая/пиковая/средняя скорость)
- ✅ Per-node метрики с координатами
- ✅ Uptime tracking
- ✅ Светится когда сеть активна (light level 15)
- ✅ Scrollable список узлов (большой GUI 256x220)

**Рецепт**: 3 Bone Blocks + 1 Ender Pearl + 1 Redstone Block

---

### 🎴 Система апгрейдов (5 типов)

**Местоположение**: `src/main/java/com/bonepipe/items/UpgradeCardItem.java`

#### ⚡ Speed Upgrade Card
- **Эффект**: x2.0 к скорости переноса
- **Стакается**: Да (x2/x4/x8/x16 с 1/2/3/4 картами)
- **Максимум**: **x16 скорость**
- **Влияет на**: Items, Fluids, Energy, Chemicals

#### 📦 Stack Size Upgrade Card
- **Эффект**: +8 предметов к размеру стека
- **Стакается**: Да (+8/+16/+24/+32)
- **Максимум**: **96 предметов** (64+32)
- **Влияет на**: Только Items

#### 🔍 Filter Upgrade Card
- **Эффект**: Включает фильтрацию предметов/жидкостей
- **Слоты**: 1 базовый filter slot
- **Режимы**: Whitelist/Blacklist
- **NBT**: Учитывает NBT данные

#### 📊 Filter Capacity Upgrade Card
- **Эффект**: +9 filter slots
- **Стакается**: Да (+9/+18/+27/+36)
- **Максимум**: **37 слотов** (1 базовый + 4×9)
- **Требует**: Filter Upgrade для работы
- **Аналог**: AE2 Capacity Cards

#### 📡 Range Upgrade Card
- **Эффект**: x2.0 радиус беспроводной связи
- **Примечание**: Работает если в конфиге ограничен радиус
- **По умолчанию**: Радиус неограничен

---

## 🎨 GUI система

### 📱 Wireless Adapter GUI
**Местоположение**: `src/main/java/com/bonepipe/gui/AdapterScreen.java`

**Особенности** (v2.1.0):
- ✅ Mekanism-style архитектура (imageHeight=166, BASE_Y_OFFSET=84)
- ✅ **Слоты апгрейдов УБРАНЫ** - больше не в GUI
- ✅ 5 вкладок: Items / Fluids / Energy / Gas / Network
- ✅ SideConfigWidget (46px высота, компактный дизайн)
- ✅ Частотное поле (frequency input)
- ✅ Toggle кнопки (Enable/Disable)
- ✅ Status индикаторы (Connected/Disconnected)
- ✅ Автоматическое переключение вкладок
- ✅ Сохранение состояния между открытиями

**Компоненты**:
- `SideConfigWidget` - настройка 6 сторон (направления + режимы)
- `FrequencyTextField` - ввод частоты сети
- `ToggleButton` - включение/выключение адаптера
- `StatusIndicator` - визуальная индикация статуса

### 🎛️ Network Controller GUI
**Местоположение**: `src/main/java/com/bonepipe/gui/ControllerScreen.java`

**Размеры**: 256×220 (большой экран)

**Особенности**:
- ✅ Scrollable список узлов
- ✅ Per-node статистика с координатами
- ✅ Общая статистика сети
- ✅ Current/Peak/Average transfer rates
- ✅ Uptime отображение
- ✅ Active/Total nodes count

---

## 🔌 Интеграции

### 📚 JEI (Just Enough Items)
**Местоположение**: `src/main/java/com/bonepipe/integration/jei/BonePipeJEIPlugin.java`

**Что показывает**:
- ✅ Information pages для всех предметов
- ✅ Подробные гайды по использованию
- ✅ Описание каждого типа апгрейдов с примерами
- ✅ Пошаговые инструкции
- ✅ Визуальное форматирование

**Использование**: Нажмите `U` на любом предмете BonePipe в JEI

### 🔎 The One Probe (TOP)
**Местоположение**: `src/main/java/com/bonepipe/integration/top/TOPProvider.java`

**Для адаптеров**:
- Базовая информация: Frequency, Connected machine, Transfer rates
- Расширенная (Shift): Апгрейды, Filter info, Total transferred

**Для контроллера**:
- Базовая: Frequency, Node count
- Расширенная (Shift): Rates, Uptime

### 🧪 Mekanism
**Поддержка**: Только **Gas** transfer  
**Опционально**: Работает с/без Mekanism

**Что поддерживается**:
- ✅ Gas (Hydrogen, Oxygen, Chlorine, etc.)

**Что НЕ поддерживается**:
- ❌ Infusion
- ❌ Pigment
- ❌ Slurry

---

## 🛠️ Технические детали

### Архитектура кода

**Основные пакеты**:
```
com.bonepipe/
├── blocks/          # Блоки (Adapter, Controller)
├── items/           # Предметы (Upgrade Cards)
├── gui/             # GUI (Screens, Menus, Widgets)
├── network/         # Сетевая система (NetworkManager, WirelessNetwork)
├── integration/     # Интеграции (JEI, TOP)
├── util/            # Утилиты (ChunkLoad, MachineDetector)
└── core/            # Регистрация (Registration.java)
```

### Сетевая архитектура

**NetworkManager** (Singleton):
- Управляет всеми беспроводными сетями
- Индексация по частоте (frequency → WirelessNetwork)
- Регистрация/удаление узлов
- Поиск адаптеров на частоте

**WirelessNetwork**:
- Хранит узлы на одной частоте
- Балансировка нагрузки (round-robin)
- Статистика переноса
- Валидация узлов

**NetworkNode**:
- Wrapper для AdapterBlockEntity
- Координаты + владелец
- Метрики переноса

### GUI архитектура (v2.1.0)

**Mekanism-style layout**:
```
imageHeight: 166px
inventoryLabelY: 72 (imageHeight - 94)
titleLabelY: 5

Области:
- Title: y=5-11 (6px)
- Content: y=12-83 (71px)
  - SideConfigWidget: y=18-64 (46px)
- Player Inventory: y=84-138 (54px, 3 ряда)
- Hotbar: y=142-160 (18px)
```

**Слоты апгрейдов**: УБРАНЫ из GUI, установка через правый клик

### Производительность

**Базовые скорости** (без апгрейдов):
- Items: 64 items/operation
- Fluids: 4,000 mB/operation
- Energy: 12,800 FE/operation
- Chemicals: = Fluids

**Ultimate Tier** (4× Speed Upgrade):
- Items: **1,024 items/op** (64 × 16)
- Fluids: **64,000 mB/op** (4k × 16)
- Energy: **204,800 FE/op** (12.8k × 16)

**Stack Size** (4× Stack Upgrade):
- Items: **96 items** (64 + 32)

**Фильтрация** (Filter + 4× Capacity):
- **37 filter slots** (1 + 36)

---

## 🎯 Новое в v2.1.0

### Изменения GUI:
- ❌ **Убраны слоты апгрейдов** из интерфейса
- ✅ **Чистый GUI** без перекрытия инвентаря
- ✅ Mekanism-style архитектура применена

### Новая механика апгрейдов:
- ✅ **Правый клик с картой** → установка в адаптер
- ✅ **Сообщения в чате**:
  - "Upgrade installed successfully" ✅
  - "All upgrade slots are full" ❌
- ✅ **4 внутренних слота** (не видны в GUI)
- ✅ **Сохранение в NBT**

### API методы (AdapterBlockEntity):
```java
public boolean installUpgrade(ItemStack stack)
public ItemStack removeUpgrade(int slot)
public IItemHandler getUpgradeInventory()
```

### Код изменён:
- `AdapterBlock.java` - добавлена обработка UpgradeCardItem
- `AdapterBlockEntity.java` - добавлены методы install/remove
- `AdapterMenu.java` - убраны SlotItemHandler для апгрейдов
- `en_us.json` - добавлены сообщения об установке

---

## 📂 Где искать информацию

### Основные файлы:

**README.md** - Полное описание мода (v2.0.0, немного устарел)  
**CHANGELOG.md** - История всех версий с деталями  
**CURRENT_STATUS.md** - Этот файл (актуальный статус)

### Документация разработки:

**MEKANISM_SUPPORT.md** - Детали интеграции с Mekanism  
**MEKANISM_GAS_IMPLEMENTATION.md** - Техническая документация Gas support  
**ARCHITECTURE.md** - Архитектура проекта (если есть)

### Release notes:

**RELEASE_NOTES_v2.0.0-mekanism.md** - Release notes v2.0.0  
**V1.*.0_RELEASE.md** - Release notes предыдущих версий

### Completion reports:

**COMPLETION_v1.*.0.md** - Отчёты о завершении фич в каждой версии

---

## 🚀 Как запустить/протестировать

### Из исходников:

```bash
cd /home/pc243/GIT/BonePipe
./gradlew build --no-daemon
cp build/libs/bonepipe-2.1.0.jar ~/.local/share/PrismLauncher/instances/1.19.2/minecraft/mods/
```

### В игре:

1. **Скрафти адаптер**: 4 Bone Blocks + 4 Ender Pearls + 1 Diamond
2. **Размести около машины** (сундук, печь, и т.д.)
3. **Открой GUI** (правый клик)
4. **Настрой частоту** (например: "main")
5. **Выбери вкладку** (Items/Fluids/Energy/Gas/Network)
6. **Настрой стороны** (Extract/Insert для нужных ресурсов)
7. **Установи апгрейды** (правый клик с картой Speed/Stack/etc.)
8. **Готово!** Ресурсы начнут передаваться

### С контроллером:

1. **Скрафти контроллер**: 3 Bone Blocks + 1 Ender Pearl + 1 Redstone Block
2. **Размести где угодно**
3. **Открой GUI**
4. **Установи частоту** (такую же как у адаптеров)
5. **Смотри статистику** всей сети!

---

## 📊 Сравнение с аналогами

| Мод | Items | Fluids | Energy | Chemicals | Wireless | GUI Upgrades | Controller |
|-----|-------|--------|--------|-----------|----------|-------------|------------|
| **BonePipe** | ✅ | ✅ | ✅ | ✅ (Gas) | ✅ | ✅ (5 типов) | ✅ |
| Pipez | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ (4 типа) | ❌ |
| Mekanism | ✅ | ✅ | ✅ | ✅ (All) | ❌ | ❌ | ❌ |
| Flux Networks | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ✅ |
| XNet | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ✅ |

**Вывод**: BonePipe - единственный мод с беспроводным переносом Items/Fluids/Energy/Chemicals!

---

## 📜 Лицензия

**MIT License** - свободное использование

## 👨‍💻 Автор

**demon-5656** - GitHub: https://github.com/demon-5656

---

**Последнее обновление**: 22 октября 2025, v2.1.0  
**Статус**: ✅ Стабильная сборка, готов к использованию

*Made with 🦴 and ❤️ for Minecraft 1.19.2*
