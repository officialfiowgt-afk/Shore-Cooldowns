package com.shorecooldowns;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class EditScreen extends Screen {
    private static final int[] COLORS = {
            0xFF55FFFF, 0xFFFF5555, 0xFF55FF55, 0xFFFFAA00, 0xFFFF55FF, 0xFFFFFF55, 0xFFFFFFFF };

    private final Screen parent;
    private final CooldownEntry e;
    private TextFieldWidget nameF, idF, secF, msgF;
    private boolean deleted = false;

    public EditScreen(Screen parent, CooldownEntry e) {
        super(Text.literal("Edit Cooldown"));
        this.parent = parent;
        this.e = e;
    }

    @Override
    protected void init() {
        int lx = width / 2 - 205, rx = width / 2 + 5;

        nameF = field(lx, 50, e.name, 100);
        idF = field(lx, 86, e.itemId, 100);
        secF = field(lx, 122, String.valueOf(e.seconds), 10);
        msgF = field(lx, 158, e.triggerMessage, 200);

        ButtonWidget enabled = ButtonWidget.builder(Text.empty(), b -> {
            e.enabled = !e.enabled; b.setMessage(enabledText());
        }).dimensions(rx, 50, 200, 20).build();
        enabled.setMessage(enabledText());
        addDrawableChild(enabled);

        ButtonWidget style = ButtonWidget.builder(Text.empty(), b -> {
            CooldownEntry.Style[] v = CooldownEntry.Style.values();
            e.style = v[(e.style.ordinal() + 1) % v.length];
            b.setMessage(Text.literal("Display: " + e.style));
        }).dimensions(rx, 76, 200, 20).build();
        style.setMessage(Text.literal("Display: " + e.style));
        addDrawableChild(style);

        ButtonWidget trig = ButtonWidget.builder(Text.empty(), b -> {
            CooldownEntry.Trigger[] v = CooldownEntry.Trigger.values();
            e.trigger = v[(e.trigger.ordinal() + 1) % v.length];
            b.setMessage(Text.literal("Starts on: " + e.trigger));
        }).dimensions(rx, 102, 200, 20).build();
        trig.setMessage(Text.literal("Starts on: " + e.trigger));
        addDrawableChild(trig);

        ButtonWidget color = ButtonWidget.builder(Text.empty(), b -> {
            int idx = 0;
            for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == e.color) idx = i;
            e.color = COLORS[(idx + 1) % COLORS.length];
            b.setMessage(colorText());
        }).dimensions(rx, 128, 200, 20).build();
        color.setMessage(colorText());
        addDrawableChild(color);

        addDrawableChild(ButtonWidget.builder(Text.literal("Test (start timer now)"), b -> {
            apply(); e.start(System.currentTimeMillis());
        }).dimensions(rx, 154, 200, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("§cDelete"), b -> {
            deleted = true;
            ShoreCooldowns.CONFIG.entries.remove(e);
            client.setScreen(parent);
        }).dimensions(width / 2 - 100, height - 28, 98, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 + 2, height - 28, 98, 20).build());
    }

    private TextFieldWidget field(int x, int y, String value, int max) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, 200, 20, Text.empty());
        f.setMaxLength(max);
        f.setText(value);
        addDrawableChild(f);
        return f;
    }

    private Text enabledText() { return Text.literal(e.enabled ? "§aEnabled" : "§cDisabled"); }
    private Text colorText() { return Text.literal("Color: ■").styled(s -> s.withColor(e.color & 0xFFFFFF)); }

    private void apply() {
        e.name = nameF.getText().trim();
        e.itemId = idF.getText().trim();
        e.triggerMessage = msgF.getText();
        try { e.seconds = Math.max(0.1, Double.parseDouble(secF.getText().trim())); }
        catch (NumberFormatException ignored) { }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int lx = width / 2 - 205;
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, 15, 0xFFFFFFFF);
        ctx.drawTextWithShadow(textRenderer, "Item name contains:", lx, 40, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Item ID (optional):", lx, 76, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Cooldown (seconds):", lx, 112, 0xFFAAAAAA);
        ctx.drawTextWithShadow(textRenderer, "Trigger message (for chat trigger):", lx, 148, 0xFFAAAAAA);
    }

    @Override
    public void removed() {
        if (!deleted) apply();
        ShoreCooldowns.CONFIG.save();
    }

    @Override
    public void close() { client.setScreen(parent); }
}
