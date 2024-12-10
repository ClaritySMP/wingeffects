package org.neo.wingeffect;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ElytraEffect extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private String playerWithEffect;

    @Override
    public void onEnable() {
        // Save the default config if not already present
        saveDefaultConfig();
        config = getConfig();

        // Check if the file tracking the player with the effect exists, and load it
        File effectFile = new File(getDataFolder(), "playerWithEffect.txt");
        if (effectFile.exists()) {
            try {
                playerWithEffect = new String(java.nio.file.Files.readAllBytes(effectFile.toPath()));
            } catch (IOException e) {
                getLogger().warning("Could not read the player file.");
            }
        }

        // Register the event listener
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Save the player data when the plugin is disabled
        if (playerWithEffect != null) {
            try {
                java.nio.file.Files.write(new File(getDataFolder(), "playerWithEffect.txt").toPath(), playerWithEffect.getBytes());
            } catch (IOException e) {
                getLogger().warning("Could not write the player file.");
            }
        }
    }

    @EventHandler
    public void onElytraPlacedInInventory(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        // Check if the player clicked an Elytra
        if (clickedItem != null && clickedItem.getType().name().equals("ELYTRA")) {
            // Check if this player is the first one to place the Elytra
            if (playerWithEffect == null) {
                // Assign the effect to the first player who places the Elytra in their inventory
                playerWithEffect = player.getName();

                // Get the effect details from the config (e.g., SPEED, JUMP, etc.)
                String effectType = config.getString("effect.type");
                String effectCommand = String.format("effect give %s %s infinite %d", player.getName(), effectType, config.getInt("effect.amplifier"));

                // Run the effect command for the first player who places the Elytra in their inventory
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), effectCommand);

                // Optionally notify the player
                player.sendMessage("You have received a permanent special effect for being the first to place an Elytra in your inventory!");
            }
        }
    }

    // Method to reset the effect if needed
    public void resetEffect() {
        // Reset the effect and player with effect
        playerWithEffect = null;
        new File(getDataFolder(), "playerWithEffect.txt").delete();
        getLogger().info("Effect has been reset.");
    }
}
