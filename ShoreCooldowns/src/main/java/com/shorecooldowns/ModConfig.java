package com.shorecooldowns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("shorecooldowns.json");

    public List<CooldownEntry> entries = new ArrayList<>();
    public int hudX = 6;
    public int hudY = 60;

    public static ModConfig load() {
        try {
            if (Files.exists(PATH)) {
                ModConfig c = GSON.fromJson(Files.readString(PATH), ModConfig.class);
                if (c != null) { if (c.entries == null) c.entries = new ArrayList<>(); return c; }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new ModConfig();
    }

    public void save() {
        try { Files.writeString(PATH, GSON.toJson(this)); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
