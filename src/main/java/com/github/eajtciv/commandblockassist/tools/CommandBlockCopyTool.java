package com.github.eajtciv.commandblockassist.tools;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.github.eajtciv.commandblockassist.CommandBlockAssist;
import com.github.eajtciv.commandblockassist.CommandBlockAssistConfig;
import com.github.eajtciv.commandblockassist.util.CommandBlockUtl;
import com.github.eajtciv.commandblockassist.util.CoolTimeManager;
import com.github.eajtciv.commandblockassist.util.Utility;
/**
 * @author eajtciv
 */
public class CommandBlockCopyTool implements Listener {

	private CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();
	private CoolTimeManager coolTime = new CoolTimeManager(0.7);

	private final String MATADETA_NAME = "COMMANDBLOCKASSIST.COPY";
	private final String PERMISSION_NAME = "COMMANDBLOCKASSIST.COPY";



	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		ItemStack handItem = event.getItem();

		if (player.hasPermission(PERMISSION_NAME) == false) {
			return;
		}

		if (player.getGameMode() != GameMode.CREATIVE) {
			return;
		}

		if (handItem == null || handItem.isSimilar(config.getCopyTool()) == false) {
			return;
		}

		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			if(player.isSneaking()){
				this.select(block, player);
			}else{
				if(this.misBreakPrevent(block, player, event)){
					return;
				}
			}
		}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(player.isSneaking()){
				this.write(block, player, event);
			}else{
				if(block.getType() != Material.COMMAND){
					Block target = Utility.getThePutBlock(block, event.getBlockFace());
					if(target.getType() == Material.AIR){
						CommandBlockUtl.writingCommanBlock(target, null, player);
					}
				}
			}
		}else if(event.getAction() == Action.LEFT_CLICK_AIR){
			this.clear(player);
		}
		event.setCancelled(true);
	}



	private boolean misBreakPrevent(Block block, Player player, PlayerInteractEvent event) {
		String command = CommandBlockUtl.getCommanBlockCommand(block);
		if((command == null || command.equals("") || coolTime.hasCoolTime(player)) == false){
			coolTime.setCoolTime(player);
			return false;
		}
		return true;
	}



	private void clear(Player player) {
		if(player.isSneaking() && Utility.removeMetadata(plugin, player, MATADETA_NAME)){
			player.sendMessage(plugin.getPluginName() + ChatColor.AQUA + "クリアしました。");
		}
	}



	private void write(Block target, Player player, PlayerInteractEvent event) {
		String command = Utility.getMetadata(plugin, player, MATADETA_NAME, String.class);

		if(command == null){
			player.sendMessage(plugin.getPluginName() + ChatColor.RED + "ブロックを選択してください。");
			return;
		}

		Boolean placeMode = Utility.getMetadata(plugin, player, config.getPlaceModeName(), Boolean.class);
		if(placeMode == null){
			placeMode = config.isPlaceMode();
		}

		if(placeMode){
			target = Utility.getThePutBlock(target, event.getBlockFace());
		}

		if(CommandBlockAssist.writingCommanBlock(target, command, player) == false){
			return;
		}

		player.sendMessage(plugin.getPluginName() + String.format("§e[%s]§6を書き込みました。", command));
	}



	private void select(Block block, Player player) {
		String nowCommand = CommandBlockUtl.getCommanBlockCommand(block);
		String oldCommand = Utility.getMetadata(plugin, player, MATADETA_NAME, String.class);

		if (nowCommand == null || nowCommand.equals("")) {
			return;
		}

		if (oldCommand == null || nowCommand.equals(oldCommand) == false) {
			Utility.setMetadata(plugin, player, MATADETA_NAME, nowCommand);

			player.sendMessage(plugin.getPluginName() + String.format("§e[%s]§6を選択しました。", nowCommand));
		}
	}

}
