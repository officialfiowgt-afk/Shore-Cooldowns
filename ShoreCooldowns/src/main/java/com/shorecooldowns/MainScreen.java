package com.shorecooldowns;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.List;

public class MainScreen extends Screen {
    private static final int PER_PAGE = 5;
    private final Screen parent;
    private int page = 0;

    public MainScreen(Screen parent) {
        super(Text.literal("Shore Cooldowns"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<CooldownEntry> list = ShoreCooldowns.CONFIG.entries;
        int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
        page = Math.max(0, Math.min(page, pages - 1));

        int x = width / 2 - 120, y = 40;
        for (int i = page * PER_PAGE; i < Math.min(list.size(), (page + 1) * PER_PAGE); i++) {
            CooldownEntry e = list.get(i);
            String txt = (e.enabled ? "§a● §f" : "§c○ §7") + e.label() + " §7- " + e.seconds + "s - " + e.style;
            addDrawableChild(ButtonWidget.builder(Text.literal(txt),
                    b -> client.setScreen(new EditScreen(this, e))).dimensions(x, y, 240, 20).build());
            y += 24;
        }

        int by = 40 + PER_PAGE * 24 + 6;
        addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> { page--; clearAndInit(); })
                .dimensions(x, by, 20, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Add from held item"), b -> addFromHeld())
                .dimensions(x + 22, by, 196, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> { page++; clearAndInit(); })
                .dimensions(x + 220, by, 20, 20).build());

        by += 24;
        addDrawableChild(ButtonWidget.builder(Text.literal("Add blank"), b -> {
            CooldownEntry e = new CooldownEntry();
            ShoreCooldowns.CONFIG.entries.add(e);
            client.setScreen(new EditScreen(this, e));
        }).dimensions(x, by, 78, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Move HUD list"),
                b -> client.setScreen(new HudPositionScreen(this))).dimensions(x + 81, by, 78, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(x + 162, by, 78, 20).build());
    }

    private void addFromHeld() {
        ItemStack s = client.player == null ? ItemStack.EMPTY : client.player.getMainHandStack();
        CooldownEntry e = new CooldownEntry();
        if (!s.isEmpty()) {
            e.name = s.getName().getString();
            e.itemId = Registries.ITEM.getId(s.getItem()).toString();
        }
        ShoreCooldowns.CONFIG.entries.add(e);
        client.setScreen(new EditScreen(this, e));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFFFF);
        int pages = Math.max(1, (ShoreCooldowns.CONFIG.entries.size() + PER_PAGE - 1) / PER_PAGE);
        ctx.drawCenteredTextWithShadow(textRenderer, "Page " + (page + 1) + "/" + pages + "  -  click an entry to edit",
                width / 2, 28, 0xFFAAAAAA);
    }

    @Override
    public void removed() { ShoreCooldowns.CONFIG.save(); }

    @Override
    public void close() { client.setScreen(parent); }
}
