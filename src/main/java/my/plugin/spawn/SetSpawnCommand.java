package my.plugin.spawn;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private final SpawnManager spawnManager;

    public SetSpawnCommand(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Ta komenda jest tylko dla graczy!");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("spawn.set")) {
            player.sendMessage(ChatColor.RED + "Nie masz uprawnień do ustawienia spawnu!");
            return true;
        }

        Location location = player.getLocation();
        spawnManager.setSpawnLocation(location);
        player.sendMessage(ChatColor.GREEN + "Spawn został ustawiony!");
        return true;
    }
}
