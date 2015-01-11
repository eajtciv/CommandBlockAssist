package com.github.eajtciv.commandblockassist.tools;

import java.util.AbstractMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.eajtciv.commandblockassist.CommandBlockAssist;
import com.github.eajtciv.commandblockassist.CommandBlockAssistConfig;
import com.github.eajtciv.commandblockassist.utility.CommandBlockUtl;
import com.github.eajtciv.commandblockassist.utility.Utility;
/**
 * @author eajtciv
 */
public class ReplaceTool implements Listener {

	private CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();

	private final String MATADETA_NAME = "COMMANDBLOCKASSIST.REPLACE";
	private final String PERMISSION_NAME = "COMMANDBLOCKASSIST.REPLACE";

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		String message = event.getMessage();

		if(isFitPlayer(player) == false){
			return;
		}

		String[] split = message.split("(?<!\\\\)[=＝]");
		for(int cnt=0 ;cnt<split.length; cnt++){
			split[cnt] = split[cnt].replace("\\=", "=");
		}

		if(split.length > 1){
			Entry<String,String> entry = new AbstractMap.SimpleEntry<String,String>(split[0],split[1]);
			Utility.setMetadata(plugin, player, MATADETA_NAME, entry);
			player.sendMessage(plugin.getPluginName() + String.format("§e[%s]§6を§e[%s]§6に置き換える様に設定しました。", split[0], split[1]));
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (isFitPlayer(player) == false) {
			return;
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR) {
			if (player.isSneaking()) {
				this.clear(player);
			} else {
				this.changeMode(player);
			}
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			this.replace(player, block);
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				|| event.getAction() == Action.RIGHT_CLICK_AIR) {

			if (player.isSneaking()) {
				this.backOperation(player);
			} else {
				this.viewSetting(player);
			}
		}

		event.setCancelled(true);
	}




	private void backOperation(Player player) {
		BlockState state = Utility.getMetadata(plugin, player, MATADETA_NAME + ".OLD", BlockState.class);
		if (state instanceof CommandBlock) {
			state.update(true, false);
			player.sendMessage(plugin.getPluginName() + ChatColor.GOLD + "操作を戻しました。");
			Utility.removeMetadata(plugin, player, MATADETA_NAME + ".OLD");
		}
	}


	private void viewSetting(Player player) {
		@SuppressWarnings("unchecked")
		Entry<String,String> entry = Utility.getMetadata(plugin, player, MATADETA_NAME, Entry.class);
		if(entry != null){
			player.sendMessage(plugin.getPluginName() + String.format("§e[%s]§6を§e[%s]§6に置き換える様に設定されています。", entry.getKey(), entry.getValue()));
		}
	}


	private void clear(Player player) {
		if(Utility.removeMetadata(plugin, player, MATADETA_NAME)
				|| Utility.removeMetadata(plugin, player, MATADETA_NAME+".OLD")){
			player.sendMessage(plugin.getPluginName() + ChatColor.AQUA + "クリアしました。");
		}

	}


	private void changeMode(Player player) {
		boolean mode = !getMode(player,plugin);
		Utility.setMetadata(plugin, player, MATADETA_NAME+".MODE", mode);
		player.sendMessage(plugin.getPluginName() + ChatColor.YELLOW + (mode ? "部分" : "全て") + ChatColor.GOLD +"置き換えになりました。");
	}

	private boolean getMode(Player player,JavaPlugin plugin){
		Boolean mode = Utility.getMetadata(plugin, player, MATADETA_NAME+".MODE", Boolean.class);
		return mode = (mode == null ? false : mode);
	}


	//ブロック書き込み処理

	private void replace(Player player, Block target){

		@SuppressWarnings("unchecked")
		Entry<String,String> entry = Utility.getMetadata(plugin, player, MATADETA_NAME, Entry.class);

		if (entry == null) {
			return;
		}

		String oldCommand = CommandBlockUtl.getCommanBlockCommand(target);

		if (oldCommand == null) {
			return;
		}

		String newCommand = oldCommand;

		if(getMode(player, plugin)){
			newCommand = newCommand.replaceAll(" "+entry.getKey()+" ", " "+entry.getValue()+" ");
			newCommand = newCommand.replaceAll("^"+entry.getKey()+" ", entry.getValue()+" ");
			newCommand = newCommand.replaceAll(" "+entry.getKey()+"$", " "+entry.getValue());
			newCommand = newCommand.replaceAll("^"+entry.getKey()+"$", entry.getValue());
		}else{
			newCommand = newCommand.replace(entry.getKey(),entry.getValue());
		}


		if(oldCommand.equals(newCommand) == false){
			Utility.setMetadata(plugin, player, MATADETA_NAME + ".OLD", target.getState());//巻き戻し用

			CommandBlockUtl.writingCommanBlock(target, newCommand, player);
			player.sendMessage(plugin.getPluginName() + String.format("§e[%s]§6から§e[%s]§6に置き換えました。", oldCommand, newCommand));
		}

	}


	private boolean isFitPlayer(Player player){
		ItemStack handItem = player.getItemInHand();
		if(player.hasPermission(PERMISSION_NAME) == false){
			return false;
		}

		if(player.getGameMode() != GameMode.CREATIVE){
			return false;
		}

		if(handItem == null || handItem.isSimilar(config.getReplaceTool()) == false){
			return false;
		}
		return true;
	}

}
