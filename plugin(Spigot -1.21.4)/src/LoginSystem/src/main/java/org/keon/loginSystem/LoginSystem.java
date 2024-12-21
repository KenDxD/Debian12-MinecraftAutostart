package org.keon.loginSystem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LoginSystem extends JavaPlugin implements Listener {

    private File userDataFile;
    private FileConfiguration userData;
    private Set<UUID> loggedInPlayers;
    private Map<UUID, Long> playerJoinTimes; // Track join times
    private Map<UUID, Boolean> forcedLogin;  // Track whether the player is forced to log in again

    @Override
    public void onEnable() {

        // Set up the plugin's data folder
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Create or load the users.yml file from the plugin's data folder
        userDataFile = new File(getDataFolder(), "users.yml");

        if (!userDataFile.exists()) {
            try {
                // If the file doesn't exist, create it
                userDataFile.createNewFile();
                getLogger().info("Created new users.yml file.");
            } catch (IOException e) {
                getLogger().warning("Could not create users.yml file.");
                e.printStackTrace();
            }
        }

        // Load the user data from the file
        userData = YamlConfiguration.loadConfiguration(userDataFile);

        loggedInPlayers = new HashSet<>();
        playerJoinTimes = new HashMap<>();
        forcedLogin = new HashMap<>();

        // Register events
        getServer().getPluginManager().registerEvents(this, this);

        // Start a repeating task to check login timeouts
        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                checkLoginTimeouts();
            }
        }, 20L, 20L); // Run every second
    }

    @Override
    public void onDisable() {
        // Save the user data when the plugin is disabled
        saveUserData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can run these commands.");
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId(); // Use UUID instead of username

        if (command.getName().equalsIgnoreCase("register")) {
            if (args.length != 1) {
                player.sendMessage("Usage: /register <password>");
                return false;
            }

            String password = args[0];

            if (userData.contains(playerUUID.toString())) {
                player.sendMessage("You are already registered!");
            } else {
                userData.set(playerUUID.toString(), password);
                saveUserData();
                player.sendMessage("You have successfully registered!");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("login")) {
            if (args.length != 1) {
                player.sendMessage("Usage: /login <password>");
                return false;
            }

            String password = args[0];

            if (!userData.contains(playerUUID.toString())) {
                player.sendMessage("You are not registered. Please register first using /register <password>");
                return false;
            }

            String storedPassword = userData.getString(playerUUID.toString());
            if (storedPassword.equals(password)) {
                loggedInPlayers.add(playerUUID);
                forcedLogin.put(playerUUID, false); // Reset forced login state
                player.sendMessage("You have successfully logged in!");
                playerJoinTimes.remove(playerUUID); // Remove from the timeout check
            } else {
                player.sendMessage("Incorrect password!");
            }
            return true;
        }

        return false;
    }

    // Restrict movement for players who are not logged in
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!loggedInPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage("You must log in to move!");
        }
    }

    // Prevent damage for players who are not logged in
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            if (!loggedInPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage("Cannot take damage if not logged in!");
            }
        }
    }

    // Track when a player joins
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // If this player was forced to log in (disconnected previously), they must log in again
        if (!forcedLogin.containsKey(playerUUID) || forcedLogin.get(playerUUID)) {
            loggedInPlayers.remove(playerUUID); // Ensure they're not logged in
            player.sendMessage("You must /login!");
            player.sendMessage("Usage: /login <password>");
            player.sendMessage("Use /register if not registered!");
            player.sendMessage("Usage: /register <password>");
        }

        // Store the current time when the player joins
        playerJoinTimes.put(playerUUID, System.currentTimeMillis());
    }

    // Track when a player quits (disconnects)
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Remove the player from logged-in players and mark them as needing to log in again
        loggedInPlayers.remove(playerUUID);
        forcedLogin.put(playerUUID, true); // Force the player to log in when they reconnect
    }

    // Helper method to check if players have timed out
    private void checkLoginTimeouts() {
        long currentTime = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> entry : playerJoinTimes.entrySet()) {
            UUID playerUUID = entry.getKey();
            long joinTime = entry.getValue();

            // If more than 30 seconds have passed and the player hasn't logged in, disconnect them
            if (currentTime - joinTime > 30000L && !loggedInPlayers.contains(playerUUID)) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && player.isOnline()) {
                    player.kickPlayer("You did not log in within the allowed time!");
                    playerJoinTimes.remove(playerUUID); // Clean up the map
                    forcedLogin.put(playerUUID, true); // Mark player for forced login upon next join
                }
            }
        }
    }

    // Helper method to save user data to the file
    private void saveUserData() {
        try {
            userData.save(userDataFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save user data!");
            e.printStackTrace();
        }
    }
}
