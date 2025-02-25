package my.plugin.spawn;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpawnCommand implements CommandExecutor, Listener {
    private final SpawnPlugin plugin;
    private final SpawnManager spawnManager;
    private final HashMap<UUID, Integer> teleportTasks = new HashMap<>();

    public SpawnCommand(SpawnPlugin plugin, SpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Ta komenda jest tylko dla graczy!");
            return true;
        }
        Player player = (Player) sender;

        if (label.equalsIgnoreCase("setspawn")) {
            if (!player.hasPermission("spawn.setspawn")) {
                player.sendMessage(ChatColor.RED + "Nie masz uprawnień do ustawienia spawna!");
                return true;
            }

            spawnManager.setSpawnLocation(player.getLocation());
            player.sendMessage("Spawn został ustawiony!");
            return true;
        }

        FileConfiguration config = plugin.getConfig();
        int teleportDelay = config.getInt("teleport.delay", 5);
        String startMessage = translateHexColorCodes(config.getString("messages.start", "Trwa teleportacja... Poczekaj %time% sekund.").replace("%time%", String.valueOf(teleportDelay)));
        String countdownMessage = translateHexColorCodes(config.getString("messages.countdown", "Trwa teleportacja... %time% sekund"));
        String teleportCompleteMessage = translateHexColorCodes(config.getString("messages.complete", "Teleportacja zakończona!"));
        String teleportCancelMessage = translateHexColorCodes(config.getString("messages.cancel", "Teleportacja anulowana! Poruszyłeś się!"));

        if (player.hasPermission("spawn.bypass")) {
            teleportPlayer(player, teleportCompleteMessage);
            return true;
        }

        player.sendMessage(startMessage);
        UUID playerId = player.getUniqueId();

        BukkitRunnable task = new BukkitRunnable() {
            int countdown = teleportDelay;

            @Override
            public void run() {
                if (countdown <= 0) {
                    teleportPlayer(player, teleportCompleteMessage);
                    teleportTasks.remove(playerId);
                    cancel();
                    return;
                }

                player.sendTitle(countdownMessage.replace("%time%", String.valueOf(countdown)), "", 0, 20, 10);
                countdown--;
            }
        };

        int taskId = task.runTaskTimer(plugin, 0, 20).getTaskId();
        teleportTasks.put(playerId, taskId);
        return true;
    }

    private void teleportPlayer(Player player, String completeMessage) {
        Location spawn = spawnManager.getSpawnLocation();
        if (spawn == null) {
            player.sendMessage(translateHexColorCodes("&cSpawn nie został ustawiony!"));
            return;
        }

        player.teleport(spawn);
        player.sendTitle(completeMessage, "", 5, 40, 10);
        player.sendMessage(completeMessage);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!teleportTasks.containsKey(playerId)) return;

        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            int taskId = teleportTasks.get(playerId);
            Bukkit.getScheduler().cancelTask(taskId);
            teleportTasks.remove(playerId);

            String cancelMessage = translateHexColorCodes(plugin.getConfig().getString("messages.cancel", "Teleportacja anulowana! Poruszyłeś się!"));

            player.sendTitle(cancelMessage, "", 5, 40, 10);
            player.sendMessage(cancelMessage);
        }
    }

    private String translateHexColorCodes(String text) {
        Pattern pattern = Pattern.compile("&#([0-9a-fA-F]{6})");
        Matcher matcher = pattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            String color = getColorFromHex(hex);
            matcher.appendReplacement(result, color);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String getColorFromHex(String hex) {

        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);

        return "§x§" + hex.charAt(0) + "§" + hex.charAt(1) + "§" + hex.charAt(2) + "§" + hex.charAt(3) + "§" + hex.charAt(4) + "§" + hex.charAt(5);
    }
}
