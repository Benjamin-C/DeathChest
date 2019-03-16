package deathchest;

import org.bukkit.plugin.java.JavaPlugin;

public class DeathChest extends JavaPlugin {

	// Fired when plugin is first enabled
    @Override
    public void onEnable() {
    	getServer().getPluginManager().registerEvents(new DeathEvent(this), this);
    	this.getCommand("deaths").setExecutor(new FindDeathCommand(this));
    }
    
    // Fired when plugin is disabled
    @Override
    public void onDisable() {

    }
}
