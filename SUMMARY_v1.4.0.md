# 🎯 BonePipe v1.4.0 - Advanced Filtering System

## Краткое резюме

**v1.4.0** добавляет **расширяемую систему фильтрации** по аналогии с AE2 Capacity Cards!

### Ключевые изменения:
1. ✅ **Capacity Upgrade Card** - добавляет +9 filter slots
2. ✅ **Bone-themed recipes** - все апгрейды теперь используют кости
3. ✅ **Dynamic filtering** - от 1 до 37 filter slots
4. ✅ **GUI updates** - показывает доступное количество слотов

---

## Новая карта: Capacity Upgrade 📦

### Механика:
```
Filter Card: Включает фильтрацию (1 base slot)
Capacity Card: Добавляет +9 slots

Прогрессия:
- Filter only: 1 slot
- Filter + 1 Capacity: 10 slots
- Filter + 2 Capacity: 19 slots  
- Filter + 3 Capacity: 28 slots
- Filter + 4 Capacity: 37 slots 🔥
```

### Рецепт:
```
H H H
H B H    где H = Hopper, B = Bone Block
H H H

→ 1x Capacity Upgrade Card
```

**Стоимость**: 8 Hoppers + 1 Bone Block = 40 Iron + 9 Bones

---

## Обновлённые рецепты (Bone Theme) 🦴

### Было (Gold-based):
- Speed: 4 Redstone + **Gold Ingot**
- Stack: 8 Chests + **Gold Ingot**
- Filter: 4 Iron + 4 Paper + **Gold Ingot**
- Range: 8 Ender Pearls + **Gold Ingot**

### Стало (Bone-themed):
- Speed: 4 Redstone + **Bone** ⚡
- Stack: 8 Chests + **Bone** 📦
- Filter: 4 Iron + 4 Paper + **Bone** 🎯
- Range: 8 Ender Pearls + **Bone** 📡
- Capacity: 8 Hoppers + **Bone Block** 🗄️ (NEW!)

**Обоснование**: Название мода **Bone**Pipe → тематика костей!

---

## Технические изменения

### UpgradeCardItem.java:
```java
public enum UpgradeType {
    SPEED("Speed", "Increases transfer rate by 2x", 2.0, 0, 0, 0),
    FILTER("Filter", "Adds item/fluid filtering", 1.0, 0, 0, 1),
    RANGE("Range", "Increases wireless range by 2x", 1.0, 2.0, 0, 0),
    STACK("Stack Size", "Increases stack size by +8", 1.0, 0, 8, 0),
    CAPACITY("Filter Capacity", "Adds +9 filter slots", 1.0, 0, 0, 9); // NEW!
    
    final int filterSlots; // NEW field
}
```

### AdapterBlockEntity.java:
```java
private int totalFilterSlots = 0; // NEW field

private void recalculateUpgrades() {
    // ...
    totalFilterSlots += type.filterSlots; // NEW calculation
}

public int getTotalFilterSlots() {
    return hasFilter ? Math.max(1, totalFilterSlots) : 0;
}
```

### AdapterScreen.java:
```java
// Было:
"Filter: Installed"

// Стало:
"Filter Slots: " + be.getTotalFilterSlots()
```

---

## Сравнение с AE2

| Параметр | AE2 Capacity | BonePipe Capacity |
|----------|--------------|-------------------|
| Slots per card | +9 | +9 ✅ |
| Base slots | 9 | 1 (with Filter) |
| Max cards | Variable | 4 |
| Max slots | ~63 | 37 |
| Recipe | Complex (AE2) | 8 Hoppers + Bone Block |
| Stacking | Additive | Additive ✅ |

**Вывод**: Похожая механика, но проще!

---

## Баланс

### Стоимость ресурсов:

**Early Game** (базовые апгрейды):
- Speed/Stack/Filter/Range: 1 Bone каждый
- Дешевле чем с Gold (скелеты легко фармить)

**Mid-Late Game** (Capacity cards):
- 1 Capacity = 40 Iron + 9 Bones
- 4 Capacity = 160 Iron + 36 Bones

**Итого для MAX фильтрации**:
- 1 Filter (base) + 4 Capacity = **161 Iron + 37 Bones**

Сбалансировано! Не OP, но требует ресурсов для максимума.

---

## Use Cases

### Пример 1: Basic Ore Sorting (Early)
```yaml
Setup: Filter + 0 Capacity
Slots: 1
Use: Фильтр одной руды (например, только Iron Ore)
```

### Пример 2: Multi-Ore Sorting (Mid)
```yaml
Setup: Filter + 2 Capacity
Slots: 19
Use: Фильтр множества руд (Iron, Gold, Copper, Tin, etc.)
```

### Пример 3: Full AE2-Style Sorting (Late)
```yaml
Setup: Filter + 4 Capacity
Slots: 37
Use: Сортировка всех типов предметов в системе
```

---

## Roadmap Progress

### ✅ Завершено:
- **v1.1.0** - Chunkloading ✅
- **v1.2.0** - Mekanism Integration ✅
- **v1.3.0** - Upgrade System Active ✅
- **v1.4.0** - Advanced Filtering ✅

### 🔜 Следующее:
- **v1.5.0** - Statistics & Monitoring
  - Real-time transfer graphs
  - Peak performance tracking
  - Network visualizer
  - Debug overlay

### 🔮 Будущее:
- **v2.0.0** - Full Release
  - Complete testing
  - JEI integration
  - TOP/HWYLA tooltips
  - Tutorial quests

---

## Статистика релиза

| Метрика | Значение |
|---------|----------|
| Версия | v1.4.0 |
| Дата | 21.10.2025 |
| Новых файлов | 3 |
| Изменённых файлов | 9 |
| Новых айтемов | 1 (Capacity Upgrade) |
| Lines of code | ~150 |
| Max filter slots | 37 |

---

## 🎉 Заключение

**BonePipe v1.4.0 = Полноценная расширяемая фильтрация!**

✅ Capacity Cards как в AE2  
✅ Динамические слоты (1-37)  
✅ Bone-themed рецепты (уникальность)  
✅ Сбалансированная прогрессия  
✅ GUI показывает слоты  
✅ Ready для Late Game!  

**Мод теперь имеет 90% запланированных функций!** 🚀

---

**Next: v1.5.0 - Statistics & Monitoring** 📊
