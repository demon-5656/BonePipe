# BonePipe v2.0.0 - Testing Guide

## 🧪 Финальный чек-лист для тестирования

Этот документ содержит полный набор тестов для проверки всех возможностей BonePipe v2.0.0 перед релизом.

---

## ✅ 1. Базовая функциональность

### 1.1 Wireless Adapter
- [ ] Adapter крафтится (4 Bone Blocks + 4 Ender Pearls + 1 Diamond)
- [ ] Adapter размещается около машины
- [ ] GUI открывается по ПКМ
- [ ] Частота устанавливается (1-999999)
- [ ] Цвет выбирается (16 цветов)
- [ ] Access mode переключается (Public/Private/Protected)

### 1.2 Items Transfer
- [ ] Перенос items из chest в chest
- [ ] Перенос items с фильтром (whitelist)
- [ ] Перенос items с фильтром (blacklist)
- [ ] Перенос items с NBT matching
- [ ] Per-side configuration работает
- [ ] Extract/Insert режимы работают

### 1.3 Fluids Transfer
- [ ] Перенос fluids из tank в tank
- [ ] Перенос воды
- [ ] Перенос лавы
- [ ] Перенос кастомных жидкостей
- [ ] Fluids фильтр работает

### 1.4 Energy Transfer
- [ ] Перенос Forge Energy
- [ ] Из батареи в машину
- [ ] Балансировка между несколькими приёмниками
- [ ] Energy throughput корректный

---

## ✅ 2. Mekanism Integration

### 2.1 Gas Transfer
- [ ] Hydrogen перенос
- [ ] Oxygen перенос
- [ ] Chlorine перенос
- [ ] Steam перенос
- [ ] Chemical Infuser input/output

### 2.2 Infusion Transfer
- [ ] Carbon infusion
- [ ] Diamond infusion
- [ ] Redstone infusion
- [ ] Metallurgic Infuser работает

### 2.3 Pigment Transfer
- [ ] Все 16 цветов pigment
- [ ] Painter машина работает

### 2.4 Slurry Transfer
- [ ] Dirty slurry (руды)
- [ ] Clean slurry (руды)
- [ ] Chemical Washer работает

---

## ✅ 3. Upgrade System

### 3.1 Speed Upgrade
- [ ] 1x Speed Card: x2.0 скорость
- [ ] 2x Speed Cards: x4.0 скорость
- [ ] 3x Speed Cards: x8.0 скорость
- [ ] 4x Speed Cards: x16.0 скорость
- [ ] Items: 64 → 1024 items/op ✅
- [ ] Fluids: 4k → 64k mB/op ✅
- [ ] Energy: 12.8k → 204k FE/op ✅

### 3.2 Stack Upgrade
- [ ] 1x Stack Card: +8 items (72 total)
- [ ] 2x Stack Cards: +16 items (80 total)
- [ ] 3x Stack Cards: +24 items (88 total)
- [ ] 4x Stack Cards: +32 items (96 total)
- [ ] Работает с Speed Upgrades одновременно

### 3.3 Filter Upgrade
- [ ] Filter Card даёт 1 slot
- [ ] GUI показывает filter slots
- [ ] Whitelist mode работает
- [ ] Blacklist mode работает
- [ ] NBT matching включается/выключается

### 3.4 Capacity Upgrade
- [ ] 1x Capacity: 10 slots (1+9)
- [ ] 2x Capacity: 19 slots (1+18)
- [ ] 3x Capacity: 28 slots (1+27)
- [ ] 4x Capacity: 37 slots (1+36) ✅
- [ ] GUI динамически расширяется
- [ ] Работает только с Filter Card

### 3.5 Range Upgrade
- [ ] Range Card крафтится
- [ ] Устанавливается в adapter
- [ ] (Проверить если range ограничен в config)

---

## ✅ 4. Advanced Filtering

### 4.1 Filter Modes
- [ ] Whitelist: только указанные items
- [ ] Blacklist: всё кроме указанных
- [ ] NBT matching: учитывает enchantments
- [ ] NBT matching: учитывает damage

### 4.2 Filter Capacity
- [ ] Базовый: 1 slot
- [ ] +1 Capacity: 10 slots
- [ ] +2 Capacity: 19 slots
- [ ] +3 Capacity: 28 slots
- [ ] +4 Capacity: 37 slots (max!)

### 4.3 Dynamic GUI
- [ ] GUI показывает только доступные slots
- [ ] Scrollbar появляется при >27 slots
- [ ] Drag & drop работает
- [ ] Clear button очищает слот

---

## ✅ 5. Network Controller

### 5.1 Crafting & Placement
- [ ] Controller крафтится (3 Bone Blocks + Ender Pearl + Redstone Block)
- [ ] Controller размещается
- [ ] GUI открывается

### 5.2 GUI Features
- [ ] Frequency устанавливается
- [ ] Access mode переключается
- [ ] Показывает список узлов (адаптеров)
- [ ] Показывает позиции узлов (X, Y, Z)
- [ ] Показывает status узлов (●green / ●gray)
- [ ] Node count (active/total) корректный

### 5.3 Statistics
- [ ] Current transfer rate отображается
- [ ] Peak transfer rate отображается
- [ ] Average transfer rate отображается
- [ ] Uptime показывается (формат: h/m/s)
- [ ] Per-node metrics отображаются
- [ ] GUI обновляется в реальном времени

### 5.4 Active Indicator
- [ ] Controller светится когда сеть active
- [ ] Light level 15 корректный
- [ ] Не светится когда сеть idle
- [ ] Blockstate ACTIVE=true/false работает

---

## ✅ 6. Statistics & Monitoring

### 6.1 NetworkStatistics
- [ ] Statistics собираются per-frequency
- [ ] Total items tracked
- [ ] Total fluids tracked
- [ ] Total energy tracked
- [ ] Peak rate вычисляется корректно
- [ ] Average rate вычисляется корректно

### 6.2 Adapter Metrics
- [ ] Total transferred показывается
- [ ] Items per second рассчитывается
- [ ] Fluids per second рассчитывается
- [ ] Energy per second рассчитывается
- [ ] Uptime tracking работает

### 6.3 Circular Buffer
- [ ] Хранит последние 100 записей
- [ ] Старые данные удаляются
- [ ] Memory leak отсутствует

---

## ✅ 7. JEI Integration

### 7.1 Information Pages
- [ ] JEI установлен (runtime test)
- [ ] Wireless Adapter имеет info page
- [ ] Network Controller имеет info page
- [ ] Speed Upgrade имеет info page
- [ ] Stack Upgrade имеет info page
- [ ] Filter Upgrade имеет info page
- [ ] Capacity Upgrade имеет info page
- [ ] Range Upgrade имеет info page

### 7.2 Content Quality
- [ ] Тексты читаемые
- [ ] Форматирование корректное (§l§n заголовки)
- [ ] Примеры понятные
- [ ] Пошаговые инструкции есть
- [ ] Нет опечаток

### 7.3 Recipe Display
- [ ] Все рецепты отображаются в JEI
- [ ] Bone-themed recipes корректные
- [ ] Recipe click works (показывает ingredients)

---

## ✅ 8. The One Probe Integration

### 8.1 Basic Info (Adapter)
- [ ] TOP установлен (runtime test)
- [ ] Frequency показывается
- [ ] Connected machine показывается
- [ ] Transfer rate показывается (items/s)
- [ ] Transfer rate показывается (mB/s)
- [ ] Transfer rate показывается (FE/s)

### 8.2 Extended Info (Adapter - Shift)
- [ ] Upgrade list показывается
- [ ] Speed multiplier показывается (x2/x4/etc.)
- [ ] Stack bonus показывается (+8/+16/etc.)
- [ ] Filter slots показываются
- [ ] Total transferred показывается

### 8.3 Controller Info
- [ ] Frequency показывается
- [ ] Node count показывается (active/total)
- [ ] Current rate показывается (при Shift)
- [ ] Peak rate показывается (при Shift)
- [ ] Average rate показывается (при Shift)
- [ ] Uptime показывается (при Shift)

### 8.4 Formatting
- [ ] Большие числа форматируются (k/M/B)
- [ ] Время форматируется (h/m/s)
- [ ] Цвета корректные (§a green, §7 gray)

---

## ✅ 9. Performance Testing

### 9.1 Single Network
- [ ] 2 adapters работают стабильно
- [ ] 5 adapters работают стабильно
- [ ] 10 adapters работают стабильно
- [ ] 20 adapters работают стабильно
- [ ] 30+ adapters работают стабильно

### 9.2 Multiple Networks
- [ ] 2 networks (разные частоты) изолированы
- [ ] 5 networks одновременно
- [ ] 10 networks одновременно
- [ ] Networks не мешают друг другу

### 9.3 High Throughput
- [ ] Items: 1024/op с 4x Speed
- [ ] Fluids: 64k mB/op с 4x Speed
- [ ] Energy: 204k FE/op с 4x Speed
- [ ] No lag при max throughput

### 9.4 Memory Usage
- [ ] Memory leak отсутствует (long run test)
- [ ] Statistics buffer не растёт бесконечно
- [ ] Adapter removal cleanup работает

---

## ✅ 10. Compatibility Testing

### 10.1 Mekanism Machines
- [ ] Electrolytic Separator
- [ ] Chemical Infuser
- [ ] Pressurized Reaction Chamber
- [ ] Chemical Washer
- [ ] Chemical Oxidizer
- [ ] Chemical Dissolution Chamber
- [ ] Thermal Evaporation Plant
- [ ] Solar Neutron Activator

### 10.2 Vanilla & Forge
- [ ] Chest (items)
- [ ] Furnace (items + energy)
- [ ] Hopper (items)
- [ ] Bucket (fluids)
- [ ] Tanks (fluids)
- [ ] Batteries (energy)

### 10.3 Side Configuration
- [ ] Top side работает
- [ ] Bottom side работает
- [ ] North/South/East/West работают
- [ ] Multiple sides одновременно
- [ ] Разные типы на разных сторонах

---

## ✅ 11. Edge Cases

### 11.1 Empty Machines
- [ ] Empty chest (no items)
- [ ] Empty tank (no fluids)
- [ ] Empty battery (no energy)
- [ ] No crash при empty source

### 11.2 Full Machines
- [ ] Full chest (no space)
- [ ] Full tank (no space)
- [ ] Full battery (no space)
- [ ] No item loss при full target

### 11.3 Network Changes
- [ ] Добавление adapter в runtime
- [ ] Удаление adapter в runtime
- [ ] Изменение frequency в runtime
- [ ] Controller disconnect/reconnect

### 11.4 World Reload
- [ ] Save & reload world
- [ ] Frequencies сохраняются
- [ ] Upgrades сохраняются
- [ ] Filters сохраняются
- [ ] Statistics reset корректно

---

## ✅ 12. User Experience

### 12.1 GUI Usability
- [ ] GUI открывается без задержки
- [ ] Кнопки кликабельны
- [ ] Tooltips информативные
- [ ] Scrolling smooth (controller GUI)

### 12.2 Recipes
- [ ] Все рецепты bone-themed
- [ ] Рецепты сбалансированные (не слишком дешёвые/дорогие)
- [ ] Рецепты логичные (ingredients имеют смысл)

### 12.3 Localization
- [ ] Все блоки переведены (en_us.json)
- [ ] Все items переведены
- [ ] Все tooltips переведены
- [ ] GUI elements переведены

---

## ✅ 13. Documentation

### 13.1 README.md
- [ ] README.md обновлён для v2.0.0
- [ ] Все фичи описаны
- [ ] Примеры использования есть
- [ ] FAQ отвечает на вопросы
- [ ] Сравнительная таблица актуальна

### 13.2 CHANGELOG.md
- [ ] CHANGELOG.md содержит v2.0.0 entry
- [ ] Все изменения задокументированы
- [ ] Breaking changes отмечены
- [ ] Migration guide есть (если нужен)

### 13.3 In-Game Help
- [ ] JEI info pages помогают новым игрокам
- [ ] TOP tooltips дают нужную информацию
- [ ] Tooltips в GUI понятные

---

## ✅ 14. Release Preparation

### 14.1 Version Bump
- [ ] gradle.properties: mod_version = 2.0.0
- [ ] mods.toml: version = 2.0.0
- [ ] README.md: badge показывает 2.0.0

### 14.2 Build
- [ ] `./gradlew build` успешен
- [ ] Нет compilation errors
- [ ] Нет warnings (критичных)
- [ ] .jar файл создаётся

### 14.3 Assets Check
- [ ] Все текстуры есть
- [ ] Все модели есть
- [ ] Все loot tables есть
- [ ] Все recipes есть
- [ ] Все локализации есть

### 14.4 Final Testing
- [ ] Clean install test (новый .minecraft)
- [ ] Mod loads без crashes
- [ ] Все features работают
- [ ] No console errors (критичных)

---

## 📊 Testing Summary

**Когда все чекбоксы ✅**:
- ✅ BonePipe v2.0.0 готов к релизу!
- ✅ Все features протестированы
- ✅ Интеграции работают
- ✅ Документация полная

**Если есть issues**:
- 🔴 Критичные: fix before release
- 🟡 Минорные: добавить в Known Issues
- 🟢 Enhancement: отложить на v2.1.0

---

## 🚀 Post-Release

После релиза v2.0.0:
1. Создать GitHub Release с .jar файлом
2. Опубликовать на CurseForge
3. Опубликовать на Modrinth
4. Анонс в Discord/Reddit
5. Мониторинг feedback от сообщества
6. Hotfix при критичных багах

---

**Удачного тестирования! 🦴**
