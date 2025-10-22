package com.bonepipe.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Text field for frequency input
 */
public class FrequencyTextField extends EditBox {
    
    private final Consumer<String> onChanged;
    private boolean suppressCallback = false;
    
    public FrequencyTextField(Font font, int x, int y, int width, int height, 
                             Component message, Consumer<String> onChanged) {
        super(font, x, y, width, height, message);
        this.onChanged = onChanged;
        this.setMaxLength(32);
        this.setBordered(true);
    }
    
    @Override
    public void insertText(String text) {
        super.insertText(text);
        if (onChanged != null && !suppressCallback) {
            onChanged.accept(getValue());
        }
    }
    
    @Override
    public void setValue(String text) {
        suppressCallback = true;
        super.setValue(text);
        suppressCallback = false;
    }
    
    @Override
    public void setFocused(boolean focused) {
        boolean wasFocused = isFocused();
        super.setFocused(focused);
        
        // Save when losing focus
        if (wasFocused && !focused && onChanged != null) {
            onChanged.accept(getValue());
        }
    }
    
    /**
     * Validate frequency string (alphanumeric + dash/underscore)
     */
    public static boolean isValidFrequency(String frequency) {
        if (frequency == null || frequency.isEmpty()) {
            return false;
        }
        return frequency.matches("[a-zA-Z0-9_-]+");
    }
}
