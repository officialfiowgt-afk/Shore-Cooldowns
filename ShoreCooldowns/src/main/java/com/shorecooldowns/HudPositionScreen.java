package com.shorecooldowns;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class HudPositionScreen extends Screen {
    private final Screen parent;

    public HudPositionScreen(Screen parent) {
        super(Text.literal("Move HUD list"));
        this.parent = parent;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        ShoreCooldowns.CONFIG.hudX = (int) mouseX;
        ShoreCooldowns.CONFIG.hudY = (int) mouseY;
        return true;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, "Click anywhere to place the cooldown list. Press ESC when done.",
                width / 2, 15, 0xFFFFFFFF);
        for (int i = 0; i < 3; i++) {
            CooldownHud.drawListRow(ctx, textRenderer, "Example power " + (i + 1), ItemStack.EMPTY,
                    ShoreCooldowns.CONFIG.hudX, ShoreCooldowns.CONFIG.hudY + i * 20,
                    0.6f - i * 0.2f, "12", 0xFF55FFFF);
        }
    }

    @Override
    public void removed() { ShoreCooldowns.CONFIG.save(); }

    @Override
    public void close() { client.setScreen(parent); }
}
