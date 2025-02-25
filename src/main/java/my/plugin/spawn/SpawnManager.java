package my.plugin.spawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class SpawnManager {
    private final SpawnPlugin plugin;
    private File spawnFile;
    private FileConfiguration spawnConfig;

    public SpawnManager(SpawnPlugin plugin) {
        this.plugin = plugin;
        loadSpawnFile();
    }

    private void loadSpawnFile() {
        spawnFile = new File(plugin.getDataFolder(), "spawn-location.yml");

        if (!spawnFile.exists()) {
            try {
                spawnFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
    }

    public Location getSpawnLocation() {
        if (!spawnConfig.contains("spawn")) {
            return null;
        }

        double x = spawnConfig.getDouble("spawn.x");
        double y = spawnConfig.getDouble("spawn.y");
        double z = spawnConfig.getDouble("spawn.z");
        float yaw = (float) spawnConfig.getDouble("spawn.yaw");
        float pitch = (float) spawnConfig.getDouble("spawn.pitch");
        String world = spawnConfig.getString("spawn.world");

        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public void setSpawnLocation(Location location) {
        spawnConfig.set("spawn.world", location.getWorld().getName());
        spawnConfig.set("spawn.x", location.getX());
        spawnConfig.set("spawn.y", location.getY());
        spawnConfig.set("spawn.z", location.getZ());
        spawnConfig.set("spawn.yaw", location.getYaw());
        spawnConfig.set("spawn.pitch", location.getPitch());

        saveSpawnFile();
    }

    private void saveSpawnFile() {
        try {
            spawnConfig.save(spawnFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
