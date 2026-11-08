package com.shorecooldowns;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ShoreCooldowns implements ClientModInitializer {
    public static ModConfig CONFIG;
    private static KeyBinding openKey;
    private static boolean lastUse, lastAttack;

    @Override
    public void onInitializeClient() {
        CONFIG = ModConfig.load();

        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.shorecooldowns.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "category.shorecooldowns"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) mc.setScreen(new MainScreen(null));

            boolean use = mc.options.useKey.isPressed();
            boolean atk = mc.options.attackKey.isPressed();
            if (mc.player != null && mc.currentScreen == null) {
                if (use && !lastUse) {
                    trigger(mc.player.getStackInHand(Hand.MAIN_HAND), CooldownEntry.Trigger.RIGHT_CLICK);
                    trigger(mc.player.getStackInHand(Hand.OFF_HAND), CooldownEntry.Trigger.RIGHT_CLICK);
                }
                if (atk && !lastAttack) {
                    trigger(mc.player.getStackInHand(Hand.MAIN_HAND), CooldownEntry.Trigger.LEFT_CLICK);
                }
            }
            lastUse = use;
            lastAttack = atk;
        });

        // Starts timers from server messages (system chat + action bar)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> onMessage(message.getString()));

        HudRenderCallback.EVENT.register(CooldownHud::render);
    }

    private static void trigger(ItemStack stack, CooldownEntry.Trigger t) {
        long now = System.currentTimeMillis();
        for (CooldownEntry e : CONFIG.entries) {
            if (e.enabled && e.trigger == t && e.matches(stack) && !e.isActive(now)) e.start(now);
        }
    }

    private static void onMessage(String text) {
        String lower = text.toLowerCase();
        long now = System.currentTimeMillis();
        for (CooldownEntry e : CONFIG.entries) {
            if (e.enabled && e.trigger == CooldownEntry.Trigger.CHAT_MESSAGE
                    && !e.triggerMessage.isBlank() && lower.contains(e.triggerMessage.toLowerCase())) {
                e.start(now); // restarts; message means the power just fired
            }
        }
    }
}
