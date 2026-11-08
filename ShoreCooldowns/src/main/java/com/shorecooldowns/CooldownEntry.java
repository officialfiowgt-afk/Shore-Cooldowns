package com.shorecooldowns;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

public class CooldownEntry {
    public enum Style { SWEEP, BAR, TEXT, HUD_LIST, CROSSHAIR }
    public enum Trigger { RIGHT_CLICK, LEFT_CLICK, CHAT_MESSAGE }

    public String name = "";          // part of the item's display name
    public String itemId = "";        // optional, e.g. minecraft:netherite_sword
    public double seconds = 30;
    public boolean enabled = true;
    public Style style = Style.SWEEP;
    public Trigger trigger = Trigger.RIGHT_CLICK;
    public String triggerMessage = ""; // used when trigger == CHAT_MESSAGE
    public int color = 0xFF55FFFF;

    public transient long endTime = 0;

    public String label() { return !name.isBlank() ? name : (itemId.isBlank() ? "(unset)" : itemId); }
    public long totalMs() { return Math.max(100, (long) (seconds * 1000)); }
    public long remainingMs(long now) { return Math.max(0, endTime - now); }
    public boolean isActive(long now) { return endTime > now; }
    public void start(long now) { endTime = now + totalMs(); }

    public boolean matches(ItemStack s) {
        if (s == null || s.isEmpty()) return false;
        if (name.isBlank() && itemId.isBlank()) return false;
        if (!itemId.isBlank() && !Registries.ITEM.getId(s.getItem()).toString().equalsIgnoreCase(itemId)) return false;
        if (!name.isBlank() && !s.getName().getString().toLowerCase().contains(name.toLowerCase())) return false;
        return true;
    }
}
