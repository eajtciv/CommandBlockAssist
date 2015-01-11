package com.github.eajtciv.commandblockassist.tools;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.github.eajtciv.commandblockassist.CommandBlockAssist;
import com.github.eajtciv.commandblockassist.CommandBlockAssistConfig;
import com.github.eajtciv.commandblockassist.utility.DataTagUtl;
import com.github.eajtciv.commandblockassist.utility.ToolInventoryHolder;
import com.github.eajtciv.commandblockassist.utility.Utility;
/**
 * @author eajtciv
 */
public class GiveTool implements Listener {

	private CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();

	private final String MATADETA_NAME = "COMMANDBLOCKASSIST.GIVE";
	private final String PERMISSION_NAME = "COMMANDBLOCKASSIST.GIVE";


	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		InventoryHolder holder = event.getInventory().getHolder();
		if(ToolInventoryHolder.equalsId(holder, MATADETA_NAME)){

			Player player = (Player) event.getWhoClicked();
			Inventory inventory = player.getInventory();

			event.setCancelled(true);

			ItemStack item = event.getCurrentItem();
			//キーボードからの場合
			if(event.getHotbarButton() != -1){
				item = inventory.getItem(event.getHotbarButton());
			}

			Set<InventoryAction> actions = new HashSet<InventoryAction>();
			actions.add(InventoryAction.PICKUP_ALL);
			actions.add(InventoryAction.PICKUP_HALF);
			actions.add(InventoryAction.MOVE_TO_OTHER_INVENTORY);
			actions.add(InventoryAction.UNKNOWN);
			actions.add(InventoryAction.HOTBAR_SWAP);

			if(actions.contains(event.getAction()) == false){
				if(event.getRawSlot() == -999){
					player.closeInventory();
				}
				return;
			}

			Utility.setMetadata(plugin, player, MATADETA_NAME, item.clone());

			player.sendMessage(plugin.getPluginName() + ChatColor.YELLOW+ "[" +item.getType() +" "+item.getAmount()+ " "+item.getDurability()+"]"+ ChatColor.GOLD + "を選択しました。");
			player.closeInventory();
		}
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		ItemStack handItem = event.getItem();

		if (player.hasPermission(PERMISSION_NAME) == false) {
			return;
		}

		if(player.getGameMode() != GameMode.CREATIVE){
			return;
		}

		if(handItem == null || handItem.isSimilar(config.getGiveTool()) == false){
			return;
		}

		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			this.select(player);
		}else if(event.getAction() == Action.LEFT_CLICK_AIR){
			if(player.isSneaking()){
				this.clear(player);
			}else{
				this.select(player);
			}
		}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			this.write(block, player, event);
		}else if(event.getAction() == Action.RIGHT_CLICK_AIR){
			this.give(player);
		}

		event.setCancelled(true);
	}



	private void give(Player player) {
		ItemStack item = Utility.getMetadata(plugin, player, MATADETA_NAME, ItemStack.class);
		if(player.isSneaking() && item != null){
			item = item.clone();
			item.setAmount(1);
			player.getInventory().addItem(item);
			//player.updateInventory();
			player.getWorld().playSound(player.getLocation(), Sound.ITEM_PICKUP, 1F, 2F);
		}
	}


	private void write(Block target, Player player, PlayerInteractEvent event) {
		ItemStack item = Utility.getMetadata(plugin, player, MATADETA_NAME, ItemStack.class);

		if(item == null){
			player.sendMessage(plugin.getPluginName() + ChatColor.RED +"アイテムを選択してください。");
			return;
		}

		String command = getCommand(item);

		if(player.isSneaking() == (CommandBlockAssist.getPlaceMode(player) == false)){
			target = Utility.getThePutBlock(target, event.getBlockFace());
		}

		if(!CommandBlockAssist.writingCommanBlock(target, command, player)){
			return;
		}

		player.sendMessage(plugin.getPluginName() + ChatColor.YELLOW + "[" + command.replace("\n", "[LF]") + "]" + ChatColor.GOLD +"を書き込みました。");
	}

	private void clear(Player player) {
		if(Utility.removeMetadata(plugin, player, MATADETA_NAME)){
			player.sendMessage(plugin.getPluginName() + ChatColor.AQUA + "クリアしました。");
		}
	}


	private void select(Player player) {
		String  title = "§n    アイテムを選択してください。    ";
		ToolInventoryHolder holder = new ToolInventoryHolder(MATADETA_NAME);
		Inventory inventory = Bukkit.createInventory(holder, 0, title);
		player.openInventory(inventory);
	}

	private String getCommand(ItemStack item){
		StringBuilder command = new StringBuilder();
		command.append(config.getGiveCommand()).append(" ");
		command.append(config.getGiveTarget()).append(" ");


		if(config.isNameId()){
			String id = DataTagUtl.getItemNameId(item.getTypeId());
			if(id == null){
				id = "minecraft:" + item.getType().name().toLowerCase(Locale.ENGLISH);
			}
			command.append(id);
		}else{
			command.append(item.getTypeId());
		}

		command.append(" ").append(item.getAmount());

		if(config.getGiveDataTag()){
			String dataTag = null;
			if(config.isExactDataTag()){
				dataTag = DataTagUtl.getExactItemDataTag(item);
			}else{
				dataTag = DataTagUtl.getItemDataTag(item);
			}

			if(dataTag != null){
				command.append(" ").append(item.getDurability());

				command.append(" ").append(dataTag);
			}else if(item.getDurability() != 0){
				command.append(" ").append(item.getDurability());
			}
		}else if(item.getDurability() != 0){
			command.append(" ").append(item.getDurability());
		}
		return command.toString();
	}

}
