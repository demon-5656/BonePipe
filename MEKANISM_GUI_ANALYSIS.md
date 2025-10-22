# Mekanism GUI Architecture - Detailed Analysis

**Date**: 22 Oct 2024  
**Based on**: Mekanism 10.x (Minecraft 1.19.2)  
**Reference**: https://github.com/mekanism/Mekanism

---

## 🎯 Core Principles

Mekanism использует **единую универсальную систему** для всех машин:
1. **GuiMekanism** - базовый класс для всех GUI
2. **Стандартные размеры** - фиксированная высота/ширина
3. **Модульные виджеты** - переиспользуемые компоненты
4. **Слоистый рендеринг** - background → widgets → labels → tooltips

---

## 📐 Standard Dimensions

### Базовые размеры (GuiMekanism)

```java
// Mekanism использует эти константы для ВСЕХ машин
public abstract class GuiMekanism<T extends MekanismTileContainer<?>> 
    extends AbstractContainerScreen<T> {
    
    // STANDARD GUI DIMENSIONS
    protected static final int DEFAULT_WIDTH = 176;    // Стандартная ширина
    protected static final int DEFAULT_HEIGHT = 166;   // Стандартная высота
    
    // PLAYER INVENTORY POSITIONING
    protected static final int INVENTORY_OFFSET = 84;  // Y-смещение инвентаря игрока
    
    // LABEL POSITIONING (формулы для всех GUI)
    // titleLabelY = 5 или 6 (зависит от машины)
    // inventoryLabelY = imageHeight - 94
    
    protected int imageWidth = DEFAULT_WIDTH;
    protected int imageHeight = DEFAULT_HEIGHT;
}
```

### Почему именно 166 высота?

```
Структура GUI (сверху вниз):
┌─────────────────────────────────────┐
│ Title (y=5-6)                       │ ← 5-6px от верха
│                                     │
│ Machine-specific content            │ ← 18-78px область
│ (slots, progress bars, tanks, etc)  │   (60px высота)
│                                     │
├─────────────────────────────────────┤ ← y=84 (INVENTORY_OFFSET)
│ Player Inventory (3 rows × 9)       │ ← 54px высота (3×18)
│                                     │
│ Player Hotbar (1 row × 9)           │ ← 22px высота (18+4 отступ)
└─────────────────────────────────────┘
  Total: 5 + 60 + 1 + 54 + 22 + 24 = 166px
         ↑   ↑   ↑  ↑   ↑   ↑
         │   │   │  │   │   └─ Bottom margin
         │   │   │  │   └───── Hotbar
         │   │   │  └───────── Inventory
         │   │   └──────────── Separator
         │   └──────────────── Content area
         └──────────────────── Title area

Математика:
- Title area: 5-18px (зависит от контента)
- Content: до y=84 (INVENTORY_OFFSET)
- Inventory start: y=84
- Inventory: 3 slots × 18px = 54px
- Gap between inventory/hotbar: 4px
- Hotbar: 1 slot × 18px = 18px
- Bottom padding: ~6px

84 + 54 + 4 + 18 + 6 = 166px ✓
```

---

## 🏗️ Layout Structure

### 1. Title & Labels

```java
// В конструкторе GuiMekanism:
public GuiMekanism(T container, Inventory inv, Component title) {
    super(container, inv, title);
    this.imageWidth = DEFAULT_WIDTH;
    this.imageHeight = DEFAULT_HEIGHT;
    
    // ВАЖНО: Формула Mekanism для inventoryLabelY
    this.inventoryLabelY = this.imageHeight - 94;
    // Для 166px: 166 - 94 = 72
    
    this.titleLabelY = 5; // Или 6, зависит от дизайна
}

// Рендеринг лейблов:
@Override
protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    // Title (темно-серый цвет)
    drawString(poseStack, title, titleLabelX, titleLabelY, 0x404040);
    
    // Inventory label
    drawString(poseStack, playerInventoryTitle, 
        inventoryLabelX, inventoryLabelY, 0x404040);
}
```

### 2. Player Inventory Slots

```java
// В Menu (серверная часть):
public MekanismTileContainer(MenuType<?> type, int id, ...) {
    super(type, id);
    
    // Player inventory - стандартное расположение
    int inventoryX = 8;  // Левый отступ
    int inventoryY = 84; // INVENTORY_OFFSET
    
    // Main inventory (3 rows × 9 columns)
    for (int row = 0; row < 3; row++) {
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(
                playerInventory,
                col + row * 9 + 9,  // Slot index
                inventoryX + col * 18,
                inventoryY + row * 18
            ));
        }
    }
    
    // Hotbar (1 row × 9 columns)
    int hotbarY = inventoryY + 58; // 3×18 + 4px gap
    for (int col = 0; col < 9; col++) {
        addSlot(new Slot(
            playerInventory,
            col,
            inventoryX + col * 18,
            hotbarY
        ));
    }
}
```

### 3. Machine Content Area

```java
// Область между title и player inventory
// Y-coordinates: от ~18px до 84px (INVENTORY_OFFSET)
// Высота: ~60-66px доступно для контента машины

// Примеры размещения:
// - Energy bar: x=172, y=18, width=4, height=60
// - Progress bar: x=78, y=38, width=24, height=8
// - Input slot: x=56, y=17
// - Output slot: x=116, y=35
// - Tank: x=7, y=18, width=18, height=58
```

---

## 🎨 Rendering Pipeline

### Порядок рендеринга (критически важно!)

```java
@Override
public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
    // 1. Background (затемнённый фон за GUI)
    this.renderBackground(poseStack);
    
    // 2. Main rendering
    super.render(poseStack, mouseX, mouseY, partialTick);
    // ↑ Вызывает: renderBg() → renderLabels() → renderWidgets()
    
    // 3. Tooltips (поверх всего)
    this.renderTooltip(poseStack, mouseX, mouseY);
}

@Override
protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
    // Setup shader
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, getGuiLocation());
    
    int guiLeft = (width - imageWidth) / 2;
    int guiTop = (height - imageHeight) / 2;
    
    // ОДИН вызов blit для основного фона
    blit(poseStack, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);
    
    // Дополнительные элементы (прогресс бары, уровни жидкости и т.д.)
    renderDynamicElements(poseStack, guiLeft, guiTop, mouseX, mouseY);
}

@Override
protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
    // Координаты относительно GUI (не экрана!)
    drawString(poseStack, title, titleLabelX, titleLabelY, screenTextColor());
    drawString(poseStack, playerInventoryTitle, inventoryLabelX, inventoryLabelY, screenTextColor());
    
    // Кастомный текст (энергия, прогресс и т.д.)
    renderCustomText(poseStack);
}
```

---

## 🧩 Widget System

### GuiElement (базовый виджет)

```java
public abstract class GuiElement {
    protected final IGuiWrapper gui;
    protected final ResourceLocation resource;
    protected int x, y, width, height;
    
    // Рендеринг относительно GUI
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        // blit() использует gui.getLeft() + x, gui.getTop() + y
        gui.blit(poseStack, x, y, textureX, textureY, width, height);
    }
    
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }
}
```

### Примеры виджетов Mekanism

#### GuiEnergyBar (энергетический бар)
```java
public class GuiEnergyBar extends GuiElement {
    public static final int DEFAULT_X = 172;  // Справа от GUI
    public static final int DEFAULT_Y = 18;
    public static final int DEFAULT_WIDTH = 4;
    public static final int DEFAULT_HEIGHT = 60;
    
    @Override
    public void renderButton(PoseStack poseStack, ...) {
        // Background (пустой бар)
        blit(poseStack, x, y, textureX, textureY, width, height);
        
        // Fill (заполнение по уровню энергии)
        int fillHeight = (int)(height * energyPercentage);
        blit(poseStack, x, y + (height - fillHeight), 
             textureX + width, textureY, width, fillHeight);
    }
}
```

#### GuiSlot (слот с рамкой)
```java
public class GuiSlot extends GuiElement {
    public static final int SLOT_SIZE = 18; // 16px + 1px border each side
    
    // Стандартное расположение для input/output
    public static GuiSlot input(int x, int y) {
        return new GuiSlot(x, y, SlotType.INPUT);
    }
    
    @Override
    public void renderButton(PoseStack poseStack, ...) {
        // Рисует рамку 18×18 вокруг слота
        blit(poseStack, x, y, slotTextureX, slotTextureY, 18, 18);
    }
}
```

#### GuiGauge (жидкостный/газовый датчик)
```java
public class GuiGauge extends GuiElement {
    public enum Type {
        STANDARD(18, 60),     // Стандартный танк
        WIDE(26, 60),         // Широкий танк
        SMALL(16, 28);        // Малый танк
        
        public final int width, height;
    }
    
    @Override
    public void renderButton(PoseStack poseStack, ...) {
        // 1. Background tank
        blit(poseStack, x, y, bgTextureX, bgTextureY, width, height);
        
        // 2. Fluid/Gas fill (динамически)
        int fillHeight = (int)(height * fillPercentage);
        renderFluid(poseStack, x + 1, y + height - fillHeight - 1, 
                    width - 2, fillHeight);
        
        // 3. Overlay (стекло/рамка)
        blit(poseStack, x, y, overlayTextureX, overlayTextureY, width, height);
    }
}
```

#### GuiProgress (прогресс бар)
```java
public class GuiProgress extends GuiElement {
    public enum ProgressType {
        RIGHT(24, 8),    // Стрелка вправо
        DOWN(16, 6),     // Стрелка вниз
        UP(16, 6);       // Стрелка вверх
        
        public final int width, height;
    }
    
    @Override
    public void renderButton(PoseStack poseStack, ...) {
        // Background (серая стрелка)
        blit(poseStack, x, y, bgTextureX, bgTextureY, width, height);
        
        // Fill (зелёная заполненная часть)
        int fillWidth = (int)(width * progress);
        blit(poseStack, x, y, fillTextureX, fillTextureY, fillWidth, height);
    }
}
```

---

## 🎨 Texture Organization

### Стандартная структура текстуры (256×256)

```
┌─────────────────────────────────────────────┐
│ Main GUI Background (0,0,176,166)           │ ← Основной фон
│                                             │
│                                             │
├─────────────────────────────────────────────┤ y=166
│ Widgets Area:                               │
│ - Slots (18×18 each)                        │
│ - Buttons (various sizes)                   │
│ - Progress bars (24×8, 16×6)                │
│ - Energy bar (4×60)                         │
│ - Tank overlays (18×60, 26×60)              │
│                                             │
└─────────────────────────────────────────────┘

Примерные координаты виджетов в текстуре:
- Slot input:     (176, 0, 18, 18)
- Slot output:    (176, 18, 18, 18)
- Progress right: (176, 36, 24, 8)
- Energy empty:   (176, 44, 4, 60)
- Energy fill:    (180, 44, 4, 60)
- Tank overlay:   (184, 0, 18, 60)
```

---

## 🔧 Machine-Specific Layouts

### Пример: Energized Smelter (плавильня)

```java
public class GuiEnergizedSmelter extends GuiMekanism<...> {
    
    public GuiEnergizedSmelter(...) {
        super(container, inv, title);
        // Использует стандартные 176×166
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Energy bar (справа)
        addRenderableWidget(new GuiEnergyBar(
            172, 18,  // Стандартная позиция
            () -> machine.getEnergyContainer()
        ));
        
        // Progress arrow (в центре)
        addRenderableWidget(new GuiProgress(
            78, 38,   // Между input и output
            ProgressType.RIGHT,
            () -> machine.getProgress()
        ));
    }
    
    @Override
    protected void renderBg(...) {
        super.renderBg(...);
        // Динамические элементы уже отрисованы виджетами
    }
}

// Слоты в Menu:
// Input slot:  x=56,  y=17
// Output slot: x=116, y=35
// Energy bar:  x=172, y=18
```

### Пример: Electrolytic Separator (электролизёр)

```java
public class GuiElectrolyticSeparator extends GuiMekanism<...> {
    
    public GuiElectrolyticSeparator(...) {
        super(container, inv, title);
        // Тоже 176×166, но другой layout
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Fluid tank (слева)
        addRenderableWidget(new GuiGauge(
            7, 18,    // Левая сторона
            GuiGauge.Type.STANDARD,
            () -> machine.getFluidTank()
        ));
        
        // Gas tank left (центр-лево)
        addRenderableWidget(new GuiGauge(
            58, 18,
            GuiGauge.Type.SMALL,
            () -> machine.getLeftGasTank()
        ));
        
        // Gas tank right (центр-право)
        addRenderableWidget(new GuiGauge(
            100, 18,
            GuiGauge.Type.SMALL,
            () -> machine.getRightGasTank()
        ));
        
        // Energy bar (справа как всегда)
        addRenderableWidget(new GuiEnergyBar(172, 18, ...));
    }
}
```

---

## 📏 Key Measurements Summary

### Константы (используй ЭТИ значения)

```java
// GUI Dimensions
public static final int GUI_WIDTH = 176;
public static final int GUI_HEIGHT = 166;

// Player Inventory
public static final int INVENTORY_X = 8;
public static final int INVENTORY_Y = 84;
public static final int SLOT_SIZE = 18;

// Labels
public static final int TITLE_Y = 5;  // или 6
public static final int INVENTORY_LABEL_Y = 72; // GUI_HEIGHT - 94

// Content Area
public static final int CONTENT_START_Y = 18;
public static final int CONTENT_END_Y = 84;
public static final int CONTENT_HEIGHT = 66; // 84 - 18

// Standard Widget Positions
public static final int ENERGY_BAR_X = 172;
public static final int ENERGY_BAR_Y = 18;
public static final int ENERGY_BAR_WIDTH = 4;
public static final int ENERGY_BAR_HEIGHT = 60;
```

### Формулы (используй для расчётов)

```java
// GUI positioning (центрирование на экране)
int guiLeft = (screenWidth - imageWidth) / 2;
int guiTop = (screenHeight - imageHeight) / 2;

// Inventory label Y
int inventoryLabelY = imageHeight - 94;  // Для 166: 72

// Hotbar Y (относительно inventory start)
int hotbarY = INVENTORY_Y + (3 * SLOT_SIZE) + 4;  // 84 + 54 + 4 = 142

// Widget absolute position (на экране)
int widgetScreenX = guiLeft + widgetRelativeX;
int widgetScreenY = guiTop + widgetRelativeY;

// Mouse over detection (mouseX/Y уже в screen coords)
boolean isOver = mouseX >= guiLeft + x && mouseX < guiLeft + x + width &&
                 mouseY >= guiTop + y && mouseY < guiTop + y + height;
```

---

## 🎯 Best Practices (по опыту Mekanism)

### ✅ DO (делай так)

1. **Используй стандартные размеры**
   ```java
   this.imageWidth = 176;
   this.imageHeight = 166;
   this.inventoryLabelY = 72; // imageHeight - 94
   ```

2. **Один blit для фона**
   ```java
   blit(poseStack, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);
   ```

3. **Виджеты в init()**
   ```java
   @Override
   protected void init() {
       super.init();
       addRenderableWidget(new MyWidget(...));
   }
   ```

4. **Относительные координаты**
   ```java
   // В renderBg/renderLabels координаты относительно GUI
   drawString(poseStack, text, 10, 20, color);  // 10px от левого края GUI
   ```

5. **Стандартные цвета**
   ```java
   public static final int SCREEN_TEXT_COLOR = 0x404040;  // Тёмно-серый
   public static final int TITLE_COLOR = 0x404040;
   ```

### ❌ DON'T (не делай так)

1. **Не хардкодь 94**
   ```java
   ❌ this.inventoryLabelY = 72;
   ✅ this.inventoryLabelY = this.imageHeight - 94;
   ```

2. **Не используй абсолютные координаты в renderLabels**
   ```java
   ❌ drawString(poseStack, text, guiLeft + 10, guiTop + 20, color);
   ✅ drawString(poseStack, text, 10, 20, color);
   ```

3. **Не рисуй фон по частям**
   ```java
   ❌ blit(...); blit(...); blit(...);  // Много вызовов
   ✅ blit(poseStack, guiLeft, guiTop, 0, 0, imageWidth, imageHeight);  // Один вызов
   ```

4. **Не добавляй виджеты в render()**
   ```java
   ❌ public void render(...) { addRenderableWidget(...); }
   ✅ protected void init() { addRenderableWidget(...); }
   ```

---

## 🔍 Example: Complete Machine GUI

```java
public class MyMachineScreen extends AbstractContainerScreen<MyMachineMenu> {
    private static final ResourceLocation TEXTURE = 
        new ResourceLocation(MODID, "textures/gui/my_machine.png");
    
    // Mekanism standard constants
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;
    private static final int INVENTORY_Y = 84;
    
    public MyMachineScreen(MyMachineMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 94;  // 72
        this.titleLabelY = 6;
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Add widgets (energy bar, progress, etc.)
        // All coordinates relative to GUI
    }
    
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        this.renderTooltip(poseStack, mouseX, mouseY);
    }
    
    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        
        int guiLeft = (this.width - this.imageWidth) / 2;
        int guiTop = (this.height - this.imageHeight) / 2;
        
        // Single blit for entire background
        this.blit(poseStack, guiLeft, guiTop, 0, 0, this.imageWidth, this.imageHeight);
    }
    
    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        // Title
        this.font.draw(poseStack, this.title, 
            (float)this.titleLabelX, (float)this.titleLabelY, 0x404040);
        
        // Inventory
        this.font.draw(poseStack, this.playerInventoryTitle,
            (float)this.inventoryLabelX, (float)this.inventoryLabelY, 0x404040);
    }
}
```

---

## 📚 Reference Links

- **Mekanism GitHub**: https://github.com/mekanism/Mekanism
- **GuiMekanism**: `src/main/java/mekanism/client/gui/GuiMekanism.java`
- **GuiElement**: `src/main/java/mekanism/client/gui/element/GuiElement.java`
- **Textures**: `src/main/resources/assets/mekanism/textures/gui/`

---

**Вывод**: Mekanism использует **строгую систему стандартов**:
- 176×166 размер для всех машин
- Y=84 для инвентаря игрока (INVENTORY_OFFSET)
- inventoryLabelY = imageHeight - 94 (формула)
- Модульные виджеты с относительными координатами
- Один blit для фона, виджеты поверх

Эта система **масштабируется** - добавление новой машины требует только:
1. Создать texture (176×166 + виджеты)
2. Унаследовать GuiMekanism
3. Добавить нужные виджеты в init()
4. Готово! ✨
