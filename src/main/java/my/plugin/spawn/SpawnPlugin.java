package my.plugin.spawn;

import org.bukkit.plugin.java.JavaPlugin;

public class SpawnPlugin extends JavaPlugin {
    private SpawnManager spawnManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        spawnManager = new SpawnManager(this);

        getCommand("spawn").setExecutor(new SpawnCommand(this, spawnManager));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(spawnManager));
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }
}
