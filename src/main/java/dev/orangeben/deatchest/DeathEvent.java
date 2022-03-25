package dev.orangeben.deatchest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DeathEvent implements Listener {
	
	private Material[] legalList = {Material.AIR, Material.GRASS, Material.TALL_GRASS, Material.SEAGRASS, Material.TALL_GRASS, Material.WATER, Material.LAVA};
	
	Plugin plugin;
	
	public DeathEvent(Plugin p) {
		plugin = p;
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent evt) {
		final Player p = evt.getEntity();
		List<ItemStack> is = new ArrayList<ItemStack>();
		for(ItemStack i : evt.getDrops()) {
			is.add(i);
		}
		evt.getDrops().clear();
		BukkitScheduler scheduler = Bukkit.getScheduler();
		scheduler.runTaskLater(plugin, () -> {
			Location loc = p.getLocation();
			Block chest0block = loc.getBlock();
//			List<ItemStack> is = fis;
			
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
			String date = sdf.format(cal.getTime());
			
			int stuff = 0;
			int chest0count = 0;
			int chest0stacks = 0;
			int chest1count = 0;
			int chest1stacks = 0;
			int dropCount = is.size();
			int dropStacks = 0;
			int tmploc = 0;
			int inChest = 0;
			
			for(ItemStack s : is) {
				if(s != null) {
					stuff++;
				}
			}
			
			if(stuff > 0) {
				int rep = 0;
				int testY = 0;
				while(!isLegal(chest0block.getType()) && rep >= 0) {
					switch(rep++%9) {
					case 0: { chest0block = loc.clone().add(1, testY, 0).getBlock(); } break;
					case 1: { chest0block = loc.clone().add(0, testY, 1).getBlock(); } break;
					case 2: { chest0block = loc.clone().add(-1, testY, 0).getBlock(); } break;
					case 3: { chest0block = loc.clone().add(0, testY, -1).getBlock(); } break;
					case 4: { chest0block = loc.clone().add(1, testY, 1).getBlock(); } break;
					case 5: { chest0block = loc.clone().add(-1, testY, 1).getBlock(); } break;
					case 6: { chest0block = loc.clone().add(1, testY, -1).getBlock(); } break;
					case 7: { chest0block = loc.clone().add(-1, testY, -1).getBlock(); } break;
					case 8: {
						switch(testY) {
						case 0: testY = -1; break;
						case -1: testY = 1; break;
						case 1: rep = -1; break;
						}
					} break;
					}
					//p.sendMessage(rep + ":(" + chest0block.getX() + ", " + chest0block.getY() + ", " + chest0block.getZ() + ")");
				}
				if(isLegal(chest0block.getType())) {
					chest0block.setType(Material.CHEST);
					Chest chest0 = (Chest) chest0block.getState();
					Inventory chest0inv = chest0.getInventory();
					int chest0size = chest0inv.getSize();
					
					chest0.setCustomName(p.getName() + " " + date);
					chest0.update();
					
					Chest chest1 = null;
					Inventory chest1inv = null;
					int chest1size = 0;
					
					if(stuff > 27) {
						loc = chest0block.getLocation();
						Block chest1block = loc.clone().add(-1, 0, 0).getBlock();
						rep = 0;
						while(!isLegal(chest1block.getType()) && rep >= 0) {
							switch(rep++) {
							case 1: { chest1block = loc.clone().add(0, 0, -1).getBlock(); } break;
							case 2: { chest1block = loc.clone().add(1, 0, 0).getBlock(); } break;
							case 3: { chest1block = loc.clone().add(0, 0, 1).getBlock(); } break;
							case 4: { rep = -1;} break;
							}
							p.sendMessage(rep + ":(" + chest1block.getX() + ", " + chest1block.getY() + ", " + chest1block.getZ() + ")");
						}
						if(isLegal(chest1block.getType())) {
							chest1block.setType(Material.CHEST);
							chest1 = (Chest) chest1block.getState();
							chest1inv = chest1.getInventory();
							chest1size = chest1inv.getSize();
							chest1.setCustomName(chest0.getCustomName() + " 2/2");
							chest0.setCustomName(chest0.getCustomName() + " 1/2");
							chest0.update();
							chest1.update();
						}
					}
					chest0count = 0;
					chest0stacks = 0;
					chest1count = 0;
					chest1stacks = 0;
					dropCount = 0;
					dropStacks = 0;
					tmploc = 0;
					ItemStack tmp = null;
					// Put the stuff in the chests or throw it on the floor
					for(int i = 0; i < stuff; i++) {
						// Put it in chest0 first
						if(i < chest0size) {
							tmp = is.remove(0);
							chest0inv.addItem(tmp);
							chest0stacks++;
							chest0count = chest0count + tmp.getAmount();
						// Then put remaining stuff in chest1
						} else if(chest1 != null && i < chest0size + chest1size) {
							tmp = is.remove(0);
							chest1inv.addItem(tmp);
							chest1stacks++;
							chest1count = chest0count + tmp.getAmount();
						// Finally throw anything else on the floor
						//Should only be used if there is no chest1
						} else if(i >= chest0size + chest1size) {
							dropStacks++;
							dropCount = dropCount + is.get(tmploc++).getAmount();
							loc.getWorld().dropItem(loc, is.remove(0));
						}
					}
					
					// Tell the player where they died
					inChest = chest0count + chest1count;
					String msg = "[DeathChest] I put " + inChest + pluralize(inChest, " item") + " (" + (chest0stacks + chest1stacks) + pluralize(chest0stacks + chest1stacks, " stack") + ") in a chest";
					if(dropCount > 0) {
						msg += " and dropped " + dropCount + pluralize(dropCount, " item")+ " (" + dropStacks + pluralize(dropStacks, " stack") +")";
					}
					msg += " at (" + chest0block.getX() + ", " + chest0block.getY() + ", " + chest0block.getZ() + "). You should get ";
					if(chest1count > 0 || dropCount > 1 || (chest0count > 0 && dropCount > 0)) {
						msg += "them";
					} else {
						msg += "it";
					}
					msg += " soon.";
					p.sendMessage(msg);
				} else {
					// Tell the player where they died if there is no chest
					String msg = "Not air, no chest. I dropped your " + pluralize(dropCount, "item") + " at " + 
							loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ".";
					p.sendMessage(msg);
				}
			}
			
			// Log the death location to the server log
			String msg = p.getName() + " had died at " + loc.getBlockX() + ", "
					+ loc.getBlockY() + ", " + loc.getBlockZ();
			if(chest0count > 0 || dropCount > 0) {
					msg = msg + ", and I have";
					if(chest0count > 0) {
						inChest = chest0count + chest1count;
						msg = msg + " placed " + inChest + pluralize(inChest, " item") + " in a chest";
						if(dropCount > 0) {
							msg = msg + " and";
						}
					}
					if(dropCount > 0) {
						msg = msg + " dropped " + dropCount + pluralize(dropCount, " item");
					}
					msg = msg + " there.";
			} else {
				msg = msg + ".";
			}
			plugin.getLogger().log(Level.INFO, msg);
			
			// Log the death location to a file
			String dta = date + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
			saveData(p.getName(), p.getUniqueId(), dta);
		}, 1);
	}
	
	private void saveData(String name, UUID uuid, String data) {
		if(!plugin.getDataFolder().exists()) {
			plugin.getDataFolder().mkdirs();
		}
		boolean nf = false;
		File f = new File(plugin.getDataFolder(), uuid.toString() + ".dta");
		if(!f.exists()) {
            try {
                f.createNewFile();
                nf = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		FileWriter fw;
		try {
			fw = new FileWriter(f, true);
			PrintWriter pw = new PrintWriter(fw);
			if(nf) {
				pw.println(name);
			}
			pw.println(data);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private boolean isLegal(Material test) {
		for(Material l : legalList) {
			if(l == test) {
				return true;
			}
		}
		return false;
	}
	
	private String pluralize(int num, String str) {
		if(num == 1) {
			return str;
		} else {
			return str + "s";
		}
	}
}
