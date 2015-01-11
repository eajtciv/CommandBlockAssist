package com.github.eajtciv.commandblockassist.tools;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.github.eajtciv.commandblockassist.CommandBlockAssist;
import com.github.eajtciv.commandblockassist.CommandBlockAssistConfig;
import com.github.eajtciv.commandblockassist.tools.SetBlockTool.CoordinateMode;
import com.github.eajtciv.commandblockassist.utility.BlockArea;
import com.github.eajtciv.commandblockassist.utility.CommandBlockUtl;
import com.github.eajtciv.commandblockassist.utility.Filterdata;
import com.github.eajtciv.commandblockassist.utility.ToolInventoryHolder;
import com.github.eajtciv.commandblockassist.utility.Utility;
/**
 * @author eajtciv
 */
public class RangeSetBlockTool implements Listener {

	private CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();
	private final String title = "SetBlock:Filter";

	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event){
		Player player = event.getPlayer();
		String message = event.getMessage();

		if(isFitPlayer(player) == false){
			return;
		}

		if(message.equalsIgnoreCase("undo")){
			this.undo(player);
			event.setCancelled(true);
		}else if(message.equalsIgnoreCase("info")){
			this.viewInfo(player);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		Player player = (Player) event.getPlayer();
		Inventory inventory = event.getInventory();
		if(isFitPlayer(player) == false){
			return;
		}

		if(ToolInventoryHolder.equalsId(inventory.getHolder(), SetBlockTool.MATADETA_NAME + ".RANGE.FILTER") == false){
			return;
		}
		Utility.removeMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".FILTER.DATA");

		Utility.setMetadata(plugin, player, SetBlockTool.MATADETA_NAME + ".FILTER", inventory);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if(isFitPlayer(player) == false){
			return;
		}

		if(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK){
			this.select(player, block, event.getAction());
		}else if(event.getAction() == Action.LEFT_CLICK_AIR){
			if(player.isSneaking()){
				this.clear(player);
			}else{
				this.openFilter(player);
			}
		}else if(event.getAction() == Action.RIGHT_CLICK_AIR){
			if(player.isSneaking()){
				this.write(player);
			}
		}
		event.setCancelled(true);
	}



	private void openFilter(Player player) {
		Inventory inventory = Utility.getMetadata(plugin, player,  SetBlockTool.MATADETA_NAME + ".FILTER", Inventory.class);

		if (inventory == null
				|| ToolInventoryHolder.equalsId(inventory.getHolder(),SetBlockTool.MATADETA_NAME + ".RANGE.FILTER") == false) {

			ToolInventoryHolder holder = new ToolInventoryHolder(SetBlockTool.MATADETA_NAME + ".RANGE.FILTER");
			inventory = Bukkit.createInventory(holder, 36, title);
		}
		player.openInventory(inventory);
	}


	private void clear(Player player) {
		if (Utility.removeMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.IN",
				SetBlockTool.MATADETA_NAME + ".RANGE.OUT",
				SetBlockTool.MATADETA_NAME + ".FILTER",
				SetBlockTool.MATADETA_NAME + ".FILTER.DATA")) {
			player.sendMessage(plugin.getPluginName() + ChatColor.AQUA
					+ "クリアしました。");
		}
	}


	private void write(Player player) {
		BlockArea in = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.IN", BlockArea.class);

		BlockArea out = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.OUT", BlockArea.class);

		if (in == null || in.isValid() == false || out == null
				|| out.isValid() == false) {
			player.sendMessage(plugin.getPluginName() + ChatColor.RED + "ブロックを選択してください。");
			return;
		}

		CoordinateMode mode = SetBlockTool.getCoordinateMode(player);

		List<Block> inBlocks = in.getAreaBlock();
		List<Block> outBlocks = out.getAreaBlock();

		List<BlockState> history = new ArrayList<BlockState>();
		int cnt = 0;
		for (int i = 0; i < inBlocks.size(); i++) {
			Block inBlock = inBlocks.get(i);
			if (isExclusion(inBlock, player) == false) {
				if((cnt < outBlocks.size()) == false){
					break;
				}

				Block outBlock = outBlocks.get(cnt++);
				history.add(outBlock.getState());

				String command = SetBlockTool.getCommand(inBlock.getState(), outBlock, mode);
				CommandBlockUtl.writingCommanBlock(outBlock, command, player);
			}
		}

		if (cnt == 0) {
			player.sendMessage(plugin.getPluginName() + ChatColor.RED + "有効なブロックが有りません。");
			return;
		}
		player.sendMessage(plugin.getPluginName() + ChatColor.GREEN + "設置しました。");

		Utility.setMetadata(plugin, player, SetBlockTool.MATADETA_NAME
				+ ".RANGE.HISTORY", history.toArray(new BlockState[0]));
	}



	//ブロック選択処理
	//リロードするとPLがアンロードされる為選択データがリセットされる。
	private void select(Player player, Block block, Action action){
		BlockArea in = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.IN", BlockArea.class);

		BlockArea out = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.OUT", BlockArea.class);

		if(out == null){
			out = new BlockArea();
		}

		if(in == null){
			in = new BlockArea();
		}


		if(player.isSneaking()){
			if(action == Action.LEFT_CLICK_BLOCK){
				if(out.setFastPosition(block)){
					player.sendMessage(plugin.getPluginName() + ChatColor.GOLD +"[Out 1] "+ChatColor.YELLOW + plugin.getCoordinateString(block) + ChatColor.GOLD +"を選択しました。");
				}
			}else if(action == Action.RIGHT_CLICK_BLOCK){
				if(out.setSecondPosition(block)){
					player.sendMessage(plugin.getPluginName() + ChatColor.GOLD +"[Out 2] "+ChatColor.YELLOW + plugin.getCoordinateString(block) + ChatColor.GOLD +"を選択しました。");
				}
			}
		}else{
			if(action == Action.LEFT_CLICK_BLOCK){
				if(in.setFastPosition(block)){
					player.sendMessage(plugin.getPluginName() + ChatColor.GOLD +"[In 1] "+ChatColor.YELLOW + plugin.getCoordinateString(block) + ChatColor.GOLD +"を選択しました。");
				}
			}else if(action == Action.RIGHT_CLICK_BLOCK){
				if(in.setSecondPosition(block)){
					player.sendMessage(plugin.getPluginName() + ChatColor.GOLD +"[In 2] "+ChatColor.YELLOW + plugin.getCoordinateString(block) + ChatColor.GOLD +"を選択しました。");
				}
			}
		}

		Utility.setMetadata(plugin, player, SetBlockTool.MATADETA_NAME + ".RANGE.IN", in);
		Utility.setMetadata(plugin, player, SetBlockTool.MATADETA_NAME + ".RANGE.OUT", out);
	}


	private void viewInfo(Player player) {
		BlockArea in = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.IN", BlockArea.class);

		BlockArea out = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.OUT", BlockArea.class);

		if(in == null || in.isValid() == false || out == null || out.isValid() == false){
			player.sendMessage(ChatColor.DARK_GRAY + plugin.getPluginName() + ChatColor.RED + "選択されていないため表示できません。");
			return;
		}


		int valid = 0;
		List<Block> inBlocks = in.getAreaBlock();
		for (Block block : inBlocks) {
			if (isExclusion(block, player) == false) {
				valid++;
			}
		}

		int sizeIn = in.getAreaBlock().size();
		int sizeOut = out.getAreaBlock().size();
		int lack = (sizeOut - valid);

		String color = (lack == 0 ? "§f": (lack > 0 ? "§a+":"§c"));


		player.sendMessage(plugin.getPluginName()
				+ String.format(
						"§6In:§e%s §6Out:§e%s §6フィルタ除外:§e%s §6不足:§e%s§e",
						sizeIn, sizeOut, (sizeIn - valid), color + lack));
	}


	private void undo(Player player) {
		BlockState[] blocks = Utility.getMetadata(plugin, player,
				SetBlockTool.MATADETA_NAME + ".RANGE.HISTORY", BlockState[].class);
		if(blocks == null){
			player.sendMessage(ChatColor.DARK_GRAY + plugin.getPluginName() + ChatColor.RED + "履歴が有りません。");
		}else{
			for(BlockState block : blocks){
				block.update(true);
			}
			Utility.removeMetadata(plugin, player,  SetBlockTool.MATADETA_NAME + ".BACKUP");
			player.sendMessage(ChatColor.DARK_GRAY + plugin.getPluginName() + ChatColor.RED + "操作を戻しました。");
		}
	}

	private boolean isExclusion(Block block,Player player){
		Filterdata filter = Utility
				.getMetadata(plugin, player, SetBlockTool.MATADETA_NAME + ".FILTER.DATA", Filterdata.class);

		if (filter == null) {
			Inventory inventory = Utility.getMetadata(plugin, player,
					SetBlockTool.MATADETA_NAME + ".FILTER", Inventory.class);
			if (inventory != null) {
				filter = new Filterdata(inventory);
				Utility.setMetadata(plugin, player, SetBlockTool.MATADETA_NAME + ".FILTER.DATA", filter);
			}
		}

		if(filter != null){
			return filter.contains(block);
		}
		return false;
	}


	private boolean isFitPlayer(Player player){
		ItemStack handItem = player.getItemInHand();
		if(player.hasPermission(SetBlockTool.PERMISSION_NAME) == false){
			return false;
		}

		if(player.getGameMode() != GameMode.CREATIVE){
			return false;
		}

		if(handItem == null || handItem.isSimilar(config.getSetBlockTool()) == false){
			return false;
		}

		if(SetBlockTool.getMode(player, plugin) == false){
			return false;
		}
		return true;
	}

}
