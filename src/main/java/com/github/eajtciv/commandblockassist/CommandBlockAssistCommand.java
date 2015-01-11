package com.github.eajtciv.commandblockassist;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.github.eajtciv.commandblockassist.utility.Utility;

public class CommandBlockAssistCommand implements CommandExecutor, TabExecutor {

	private CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length == 1){
			return Arrays.asList("reload", "placeMode");
		}else if(args.length == 2){
			if(args[0].equalsIgnoreCase("PlaceMode")){
				return Arrays.asList(new String[]{"on","off","unset"});
			}
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("CommandBlockAssist") == false){
			return false;
		}

		if(args.length <= 0){
			for(String name : Arrays.asList("reload", "placeMode")){
				sender.sendMessage(ChatColor.BLUE + "/" + cmd.getName() + " " + name);
			}
		}else if(args[0].equalsIgnoreCase("Reload")){
			commandReload(sender, args);
		}else if(args[0].equalsIgnoreCase("PlaceMode")){
			commandPlaceMode(sender, args);
		}
		return true;
	}

	private void commandPlaceMode(CommandSender sender, String[] args){
		if((sender instanceof Player) == false){
			return;
		}
		Player player = (Player) sender;

		Boolean setting = Utility.getMetadata(plugin, player, config.getPlaceModeName(), Boolean.class);
		if(args.length <= 1){
			String situation = (setting == null ? (ChatColor.RESET + "未設定") : (setting ? (ChatColor.GREEN + "有効") : (ChatColor.RED + "無効")));
			player.sendMessage(plugin.getPluginName() + ChatColor.GOLD + "現在PlaceModeは" + situation + ChatColor.GOLD + "です。");
			return;
		}

		if(args[1].equalsIgnoreCase("on")){
			setting = true;
		}else if(args[1].equalsIgnoreCase("off")){
			setting = false;
		}else if(args[1].equalsIgnoreCase("unset")){
			setting = null;
		}else{
			player.sendMessage(plugin.getPluginName() + ChatColor.RED + args[1] + "は、無効です。");
			return;
		}

		String situation = (setting == null ? (ChatColor.RESET + "未設定") : (setting ? (ChatColor.GREEN + "有効") : (ChatColor.RED + "無効")));

		if(setting != null){
			Utility.setMetadata(plugin, player, config.getPlaceModeName(), setting);
		}else{
			Utility.removeMetadata(plugin, player, config.getPlaceModeName());
		}

		player.sendMessage(plugin.getPluginName() + ChatColor.GOLD + "PlaceModeを" + situation + ChatColor.GOLD + "にしました。");
	}

	private void commandReload(CommandSender sender, String[] args){
		sender.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "コンフィグを読み込みました。");
		config.loadConfig();
	}
}
