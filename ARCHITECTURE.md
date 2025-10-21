# 🏗️ BonePipe Architecture

**Техническая документация архитектуры проекта**

## 📐 Общая архитектура

### Основные компоненты

```
┌─────────────────────────────────────────────────────┐
│                   BonePipe Mod                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │   Adapter    │  │   Network    │  │  Upgrade │ │
│  │    Block     │◄─┤   Manager    │  │   Cards  │ │
│  └──────────────┘  └──────────────┘  └──────────┘ │
│         ▲                  │                ▲      │
│         │                  │                │      │
│         ▼                  ▼                │      │
│  ┌──────────────┐  ┌──────────────┐        │      │
│  │   Transfer   │  │   Frequency  │        │      │
│  │   Channels   │  │     Data     │        │      │
│  └──────────────┘  └──────────────┘        │      │
│         │                                   │      │
│         ▼                                   │      │
│  ┌─────────────────────────────────────────┘      │
│  │      Mekanism Integration (Optional)           │
│  └────────────────────────────────────────────────┘
│                                                     │
└─────────────────────────────────────────────────────┘
```

## 🔌 Блоки и сущности

### AdapterBlock
**Расположение**: `com.bonepipe.blocks.AdapterBlock`

```java
public class AdapterBlock extends Block implements EntityBlock {
    // Главный блок мода
    // Размещается у машины, не имеет направления
    // Хранит BlockEntity с логикой
}
```

**Свойства**:
- Не имеет BlockState с направлением
- Всегда активен (пока загружен чанк)
- GUI открывается по ПКМ

### AdapterBlockEntity
**Расположение**: `com.bonepipe.blocks.AdapterBlockEntity`

```java
public class AdapterBlockEntity extends BlockEntity implements MenuProvider {
    // Основная логика адаптера
    
    // Данные
    private String frequency;
    private UUID owner;
    private FrequencyAccessMode accessMode;
    
    // Конфигурация сторон
    private Map<Direction, SideConfig> sideConfigs;
    
    // Апгрейды
    private ItemStackHandler upgradeSlots;
    
    // Методы
    public void tick(); // Главный цикл
    public void connectToMachine(); // Подключение к соседу
    public void updateNetwork(); // Регистрация в сети
}
```

**Tick логика**:
1. Проверка подключения к машине
2. Обновление статуса в сети
3. Обработка заявок от планировщика
4. Выполнение операций переноса

## 📡 Сетевая система

### WirelessNetwork
**Расположение**: `com.bonepipe.network.WirelessNetwork`

```java
public class WirelessNetwork {
    // Логическая сеть адаптеров
    
    private FrequencyKey key; // owner + frequency + color
    private Set<NetworkNode> nodes; // Все адаптеры в сети
    private TransferScheduler scheduler; // Планировщик
    
    // Методы
    public void addNode(AdapterBlockEntity adapter);
    public void removeNode(AdapterBlockEntity adapter);
    public void tick(Level level); // Главный цикл сети
}
```

### NetworkManager
**Расположение**: `com.bonepipe.network.NetworkManager`

```java
public class NetworkManager {
    // Singleton управления всеми сетями
    
    private static NetworkManager INSTANCE;
    private Map<FrequencyKey, WirelessNetwork> networks;
    
    public static NetworkManager get(Level level);
    public WirelessNetwork getOrCreateNetwork(FrequencyKey key);
    public void tickNetworks(Level level);
}
```

### NetworkNode
**Расположение**: `com.bonepipe.network.NetworkNode`

```java
public class NetworkNode {
    // Представление адаптера в сети
    
    private BlockPos pos;
    private Level level;
    private Map<TransferChannel, NodeChannelConfig> channels;
    
    // Для каждого канала: INPUT/OUTPUT/BOTH, фильтры, приоритет
}
```

## 🔄 Система переноса

### TransferChannel (enum)
```java
public enum TransferChannel {
    ITEMS,
    FLUIDS,
    ENERGY,
    MEK_GAS,
    MEK_INFUSE,
    MEK_PIGMENT,
    MEK_SLURRY,
    REDSTONE
}
```

### TransferScheduler
**Расположение**: `com.bonepipe.transfer.TransferScheduler`

```java
public class TransferScheduler {
    // Планировщик операций переноса
    
    public void schedule(WirelessNetwork network, Level level) {
        // 1. Собрать все INPUT узлы (источники)
        // 2. Собрать все OUTPUT узлы (приёмники)
        // 3. Для каждого канала:
        //    - Применить фильтры
        //    - Балансировка (round-robin + priority)
        //    - Simulate → Real для каждой пары
    }
}
```

### Алгоритм переноса (Items пример)

```java
// Псевдокод для одной пары src → dst

// 1. Simulate extract
ItemStack simulated = srcHandler.extractItem(slot, amount, true);
if (simulated.isEmpty()) continue;

// 2. Apply filters
if (!matchesFilter(simulated, dstNode.getFilter())) continue;

// 3. Simulate insert
ItemStack remainder = dstHandler.insertItem(slot, simulated, true);
int actualAmount = simulated.getCount() - remainder.getCount();

// 4. Apply limits (bandwidth, speed, tick limits)
actualAmount = Math.min(actualAmount, getBandwidthLimit());
actualAmount = Math.min(actualAmount, getTickLimit());

// 5. Real operations
if (actualAmount > 0) {
    ItemStack extracted = srcHandler.extractItem(slot, actualAmount, false);
    ItemStack leftover = dstHandler.insertItem(slot, extracted, false);
    
    // 6. Statistics
    network.recordTransfer(ITEMS, actualAmount - leftover.getCount());
}
```

## 🎴 Система апгрейдов

### UpgradeCard (базовый класс)
```java
public abstract class UpgradeCard extends Item {
    public abstract UpgradeType getType();
    public abstract void apply(AdapterBlockEntity adapter);
    
    // Типы
    public enum UpgradeType {
        SPEED,      // Уменьшает тики между операциями
        BANDWIDTH,  // Увеличивает объём за операцию
        FILTER,     // Расширенные фильтры
        PRIORITY    // Приоритеты и алгоритмы
    }
}
```

### Эффекты карт

| Карта | Базовое значение | Tier 1 | Tier 2 | Tier 3 |
|-------|------------------|--------|--------|--------|
| Speed | 20 ticks/op | 10 | 5 | 1 |
| Bandwidth | 64 items | 128 | 256 | 1024 |
| Filter | Basic | Tags | NBT | Advanced |
| Priority | Round-robin | Weighted | Fill-to-cap | Drain-first |

## 🎨 GUI система

### AdapterScreen
**Расположение**: `com.bonepipe.gui.AdapterScreen`

```java
public class AdapterScreen extends AbstractContainerScreen<AdapterMenu> {
    // Главное окно с вкладками
    
    private TabManager tabManager;
    private List<ITab> tabs;
    
    // Вкладки:
    // - ItemsTab
    // - FluidsTab  
    // - EnergyTab
    // - GasTab (если Mekanism)
    // - InfuseTab (если Mekanism)
    // - PigmentTab (если Mekanism)
    // - SlurryTab (если Mekanism)
    // - RedstoneTab
    // - NetworkTab
}
```

### SideConfigWidget
```java
public class SideConfigWidget extends AbstractWidget {
    // Виджет для настройки 6 сторон машины
    // Визуально: кубик как в Mekanism
    // Клик по стороне → меню INPUT/OUTPUT/BOTH/DISABLED
}
```

## 🧪 Интеграция Mekanism

### MekanismCompat
**Расположение**: `com.bonepipe.integration.mekanism.MekanismCompat`

```java
@Mod.EventBusSubscriber
public class MekanismCompat {
    private static boolean LOADED = false;
    
    public static void init() {
        LOADED = ModList.get().isLoaded("mekanism");
        if (LOADED) {
            registerChemicalHandlers();
        }
    }
    
    public static boolean isLoaded() { return LOADED; }
}
```

### ChemicalTransferHandler
```java
public class ChemicalTransferHandler implements ITransferHandler {
    // Обработчик для Gas/Infuse/Pigment/Slurry
    
    @Override
    public TransferResult transfer(
        NetworkNode src, NetworkNode dst,
        ChemicalType type, long amount
    ) {
        // Используем Mekanism API:
        // - IGasHandler
        // - IInfusionHandler  
        // - IPigmentHandler
        // - ISlurryHandler
    }
}
```

## 📦 Пакетная система (Networking)

### PacketHandler
```java
public class PacketHandler {
    private static SimpleChannel CHANNEL;
    
    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(...);
        
        // C→S пакеты
        CHANNEL.registerMessage(0, PacketUpdateFrequency.class, ...);
        CHANNEL.registerMessage(1, PacketUpdateSideConfig.class, ...);
        CHANNEL.registerMessage(2, PacketUpdateFilter.class, ...);
        
        // S→C пакеты
        CHANNEL.registerMessage(10, PacketSyncAdapter.class, ...);
        CHANNEL.registerMessage(11, PacketSyncNetworkStats.class, ...);
    }
}
```

## ⚡ Производительность

### Оптимизации

1. **Кеширование Capabilities**
   ```java
   private LazyOptional<IItemHandler>[] sidedHandlers;
   
   private void cacheCapabilities() {
       for (Direction dir : Direction.values()) {
           sidedHandlers[dir.ordinal()] = 
               machine.getCapability(ITEM_HANDLER, dir);
       }
   }
   ```

2. **Batching операций**
   ```java
   // Группировка одинаковых ресурсов
   Map<ItemStack, Integer> batchedTransfers = new HashMap<>();
   
   for (Transfer t : pendingTransfers) {
       ItemStack key = t.getStack().copy();
       key.setCount(1);
       batchedTransfers.merge(key, t.getAmount(), Integer::sum);
   }
   ```

3. **Lazy вычисления**
   ```java
   // Пересчёт только при изменении
   private boolean dirty = true;
   private List<NetworkNode> cachedInputNodes;
   
   public List<NetworkNode> getInputNodes() {
       if (dirty) {
           cachedInputNodes = nodes.stream()
               .filter(n -> n.isInput(channel))
               .collect(Collectors.toList());
           dirty = false;
       }
       return cachedInputNodes;
   }
   ```

### Лимиты производительности

```java
public class NetworkConfig {
    // Максимум операций за тик на всю сеть
    public static final int MAX_TRANSFERS_PER_TICK = 100;
    
    // Максимум пар src→dst за один проход
    public static final int MAX_PAIRS_PER_CHANNEL = 20;
    
    // Таймаут на операцию (защита от лагов)
    public static final long OPERATION_TIMEOUT_NS = 1_000_000; // 1ms
}
```

## 🔐 Безопасность

### Проверки

1. **Mekanism Security**
   ```java
   // Пример для предметов
   if (machine instanceof ISecurityTile) {
       ISecurityTile security = (ISecurityTile) machine;
       if (!security.canAccess(player)) {
           return TransferResult.DENIED;
       }
   }
   ```

2. **Side Config уважение**
   ```java
   // Проверка через конфиг машины
   ISideConfiguration sideConfig = machine.getSideConfig();
   if (!sideConfig.supports(side, TransmissionType.ITEM)) {
       return TransferResult.INVALID_SIDE;
   }
   ```

3. **Chunk loaded проверка**
   ```java
   public boolean isValid(NetworkNode node) {
       return node.getLevel()
           .hasChunkAt(node.getPos());
   }
   ```

## 📊 Диагностика и статистика

### NetworkStatistics
```java
public class NetworkStatistics {
    private Map<TransferChannel, ChannelStats> stats;
    
    public static class ChannelStats {
        long totalTransferred;      // За всё время
        long transferredLastSecond; // За последнюю секунду
        int successfulOps;          // Успешных операций
        int failedOps;              // Неудачных
        
        // Rolling window для TPS графика
        private CircularBuffer<Long> history;
    }
}
```

## 🗂️ Структура файлов

```
src/main/java/com/bonepipe/
├── BonePipe.java                    # Главный класс мода
├── core/
│   ├── Registration.java            # Регистрация всего
│   └── Config.java                  # Конфиг мода
├── blocks/
│   ├── AdapterBlock.java
│   ├── AdapterBlockEntity.java
│   └── AdapterBlockRenderer.java
├── items/
│   ├── UpgradeCard.java
│   ├── SpeedCard.java
│   ├── BandwidthCard.java
│   ├── FilterCard.java
│   └── PriorityCard.java
├── network/
│   ├── WirelessNetwork.java
│   ├── NetworkManager.java
│   ├── NetworkNode.java
│   ├── FrequencyKey.java
│   └── FrequencyData.java
├── transfer/
│   ├── TransferChannel.java
│   ├── TransferScheduler.java
│   ├── ITransferHandler.java
│   ├── ItemTransferHandler.java
│   ├── FluidTransferHandler.java
│   ├── EnergyTransferHandler.java
│   └── ChemicalTransferHandler.java
├── gui/
│   ├── AdapterScreen.java
│   ├── AdapterMenu.java
│   ├── tabs/
│   │   ├── ITab.java
│   │   ├── ItemsTab.java
│   │   ├── FluidsTab.java
│   │   ├── EnergyTab.java
│   │   └── NetworkTab.java
│   └── widgets/
│       ├── SideConfigWidget.java
│       ├── FilterWidget.java
│       └── StatisticsWidget.java
├── integration/
│   └── mekanism/
│       ├── MekanismCompat.java
│       ├── ChemicalTransferHandler.java
│       └── SecurityHelper.java
├── packets/
│   ├── PacketHandler.java
│   ├── PacketUpdateFrequency.java
│   ├── PacketUpdateSideConfig.java
│   └── PacketSyncAdapter.java
└── util/
    ├── SideConfig.java
    ├── FilterConfig.java
    ├── NetworkStatistics.java
    └── CapabilityCache.java
```

## 🧪 Тестирование

### Unit тесты
```java
@Test
public void testRoundRobinDistribution() {
    // Создать сеть с 1 INPUT и 3 OUTPUT
    // Проверить равномерное распределение
}

@Test
public void testBandwidthLimit() {
    // Проверить что за тик не передаётся больше лимита
}

@Test
public void testMekanismSecurity() {
    // Проверить что заблокированная машина = 0 переноса
}
```

### Integration тесты
```java
@GameTest
public void testElectrolyticSeparator() {
    // Полный цикл: вода → H₂ + O₂
    // Через адаптеры на одной частоте
}
```

---

**Документ обновляется по мере разработки**
