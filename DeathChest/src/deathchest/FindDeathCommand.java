package deathchest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class FindDeathCommand implements CommandExecutor {

	Plugin plugin;
	
	UUID tgt_uuid;
	String tgt_name;
	int shownum;
	boolean toClear;
	
	public FindDeathCommand(Plugin p) {
		plugin = p;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Server sv = plugin.getServer();
		toClear = false;
		tgt_uuid = null;
		tgt_name = null;
		shownum = 1;
		Player tgt_player;
		if(sender instanceof Player) {
			switch(args.length) {
			case 2:
				boolean done = getCount(args[1], sender);
				if(done == false) {
					return false;
				}
			case 1:
				tgt_player = sv.getPlayer(args[0]);
			break;
			case 0:
				tgt_player = (Player) sender;
			break;
			default: {
				sender.sendMessage("Too many args!");
				return false;
			}
			}
		} else {
			switch(args.length) {
			case 2:
				boolean done = getCount(args[1], sender);
				if(done == false) {
					return false;
				}
			case 1:
				tgt_player = sv.getPlayer(args[0]);
			break;
			default:
				sender.sendMessage("Wrong number of args!");
				return false;
			//break;
			}
		}
		if(tgt_player == null) {
			OfflinePlayer olps[] = sv.getOfflinePlayers();
			for(OfflinePlayer p : olps) {
					if(p.getName().equals(args[0])) {
					tgt_uuid = p.getUniqueId();
					tgt_name = p.getName();
					break;
				}
			}
		} else {
			try {
				tgt_uuid = tgt_player.getUniqueId();
				tgt_name = tgt_player.getName();
			} catch(NullPointerException e) {
				tgt_uuid = null;
				tgt_name = null;
			}
		}
		if(tgt_uuid != null) {
			if(!toClear) {
				sender.sendMessage("Finding " + tgt_name + "'s (" + tgt_uuid.toString() + ") death data");
				File f = new File(plugin.getDataFolder(), tgt_uuid.toString() + ".dta");
				if(f.exists()) {
					try {
						Scanner s = new Scanner(f);
						List<String> ls = new ArrayList<String>();
						while(s.hasNextLine()) {
							ls.add(s.nextLine());
						}
						s.close();
						if(ls.size() < 1) {
							sender.sendMessage("No deaths to show");
						} else {
							shownum = Math.min(shownum, ls.size());
							for(int i = 0; i < shownum; i++) {
								sender.sendMessage(ls.get(ls.size() - i - 1));
							}
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				} else {
					sender.sendMessage("This player has no death data");
				}
			} else {
				boolean nf = false;
				File f = new File(plugin.getDataFolder(), tgt_uuid.toString() + ".dta");
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
					fw = new FileWriter(f, false);
					PrintWriter pw = new PrintWriter(fw);
					if(nf) {
						pw.println(tgt_name);
					}
					pw.flush();
					pw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			sender.sendMessage("Player not found");
		}
		return true;
	}
		
	private boolean getCount(String count, CommandSender sender) {
		try {
			shownum = Integer.parseInt(count);
			if(shownum < 1) {
				sender.sendMessage("You must see at least 1 death");
				return false;
			}
			return true;
		} catch(NumberFormatException e) {
			if(count.equals("all")) {
				shownum = -1;
				return true;
			} else if(count.equals("clear")) {
				toClear = true;
				return true;
			} else {
				sender.sendMessage("Count is not a number");
				return false;
			}
		}
	}
}

