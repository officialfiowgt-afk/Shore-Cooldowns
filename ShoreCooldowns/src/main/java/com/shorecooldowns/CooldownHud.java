package com.shorecooldowns;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CooldownHud {

    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        long now = System.currentTimeMillis();
        int w = ctx.getScaledWindowWidth(), h = ctx.getScaledWindowHeight();
        TextRenderer tr = mc.textRenderer;
        int listRow = 0, crossRow = 0;

        for (CooldownEntry e : ShoreCooldowns.CONFIG.entries) {
            if (!e.enabled) continue;
            long rem = e.remainingMs(now);
            if (rem <= 0) continue;

            float frac = Math.min(1f, rem / (float) e.totalMs()); // 1 = just started, 0 = ready
            String t = fmt(rem);

            boolean slotStyle = e.style == CooldownEntry.Style.SWEEP
                    || e.style == CooldownEntry.Style.BAR || e.style == CooldownEntry.Style.TEXT;
            int slot = slotStyle ? findHotbarSlot(mc, e) : -1;

            if (slot >= 0) {
                int x = w / 2 - 90 + slot * 20 + 2;
                int y = h - 19;
                switch (e.style) {
                    case SWEEP -> {
                        int top = y + (int) Math.floor(16 * (1 - frac));
                        int bottom = top + (int) Math.ceil(16 * frac);
                        ctx.fill(x, top, x + 16, bottom, 0x7FFFFFFF);
                        ctx.drawCenteredTextWithShadow(tr, t, x + 8, y + 4, 0xFFFFFFFF);
                    }
                    case BAR -> {
                        ctx.fill(x - 1, y - 7, x + 17, y - 3, 0xFF000000);
                        ctx.fill(x, y - 6, x + (int) (16 * (1 - frac)), y - 4, e.color);
                    }
                    case TEXT -> ctx.drawCenteredTextWithShadow(tr, t, x + 8, y - 12, e.color);
                    default -> { }
                }
            } else if (e.style == CooldownEntry.Style.CROSSHAIR) {
                ctx.drawCenteredTextWithShadow(tr, e.label() + " " + t + "s", w / 2, h / 2 + 14 + crossRow * 11, e.color);
                crossRow++;
            } else {
                ItemStack icon = findAnyStack(mc, e);
                drawListRow(ctx, tr, e.label(), icon, ShoreCooldowns.CONFIG.hudX,
                        ShoreCooldowns.CONFIG.hudY + listRow * 20, frac, t, e.color);
                listRow++;
            }
        }
    }

    public static void drawListRow(DrawContext ctx, TextRenderer tr, String label, ItemStack icon,
                                   int x, int y, float frac, String time, int color) {
        if (icon != null && !icon.isEmpty()) ctx.drawItem(icon, x, y);
        ctx.drawTextWithShadow(tr, label, x + 20, y, 0xFFFFFFFF);
        ctx.fill(x + 20, y + 10, x + 82, y + 15, 0xFF000000);
        ctx.fill(x + 21, y + 11, x + 21 + (int) (60 * (1 - frac)), y + 14, color);
        ctx.drawTextWithShadow(tr, time + "s", x + 86, y + 8, color);
    }

    private static String fmt(long ms) {
        return ms >= 10000 ? String.valueOf((ms + 999) / 1000) : String.format("%.1f", ms / 1000.0);
    }

    private static int findHotbarSlot(MinecraftClient mc, CooldownEntry e) {
        for (int i = 0; i < 9; i++) if (e.matches(mc.player.getInventory().getStack(i))) return i;
        return -1;
    }

    private static ItemStack findAnyStack(MinecraftClient mc, CooldownEntry e) {
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (e.matches(s)) return s;
        }
        try {
            if (!e.itemId.isBlank()) return new ItemStack(Registries.ITEM.get(Identifier.of(e.itemId)));
        } catch (Exception ignored) { }
        return ItemStack.EMPTY;
    }
}
