package com.github.eajtciv.commandblockassist.tools;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.eajtciv.commandblockassist.CommandBlockAssist;
import com.github.eajtciv.commandblockassist.CommandBlockAssistConfig;
import com.github.eajtciv.commandblockassist.utility.DataTagUtl;
import com.github.eajtciv.commandblockassist.utility.Utility;
/**
 * @author eajtciv
 */
public class SetBlockTool implements Listener {

	private static CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private static CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();

	public final static String MATADETA_NAME = "COMMANDBLOCKASSIST.SETBLOCK";
	public final static String PERMISSION_NAME = "COMMANDBLOCKASSIST.SETBLOCK";

	public enum CoordinateMode {Absolute, Relative};

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		ItemStack handItem = event.getItem();

		//パーミッション確認
		if(player.hasPermission(PERMISSION_NAME) == false){
			return;
		}

		if(player.getGameMode() != GameMode.CREATIVE){
			return;
		}

		//持ってる物が一致するか確認
		if(handItem == null || handItem.isSimilar(config.getSetBlockTool()) == false){
			return;
		}
/*
		try{
			if(block != null){
				Object nmsWorld = ReflectUtl.invoke(block.getWorld(), "getHandle");
				String nmsPackage = nmsWorld.getClass().getPackage().getName();
				Class<?> blockPositionClass = Class.forName(nmsPackage + ".BlockPosition");
				Constructor<?> blockPositionConstructor = blockPositionClass.getConstructor(int.class, int.class, int.class);
				Object blockPosition = blockPositionConstructor.newInstance(block.getX(), block.getY(),block.getZ());
				Object titleEntity = ReflectUtl.invoke(nmsWorld, "getTileEntity", blockPosition);
				if (titleEntity != null) {
					Class<?> nbtTagCompoundClass = Class.forName(nmsPackage + ".NBTTagCompound");

					Object nbtTagCompound = nbtTagCompoundClass.newInstance();
					ReflectUtl.invoke(titleEntity, "b", nbtTagCompound);

					System.out.println(nbtTagCompound);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
*/

		if(event.getAction() == Action.RIGHT_CLICK_AIR){
			if(player.isSneaking()){
				if(getMode(player, plugin) == false){
					this.changeCoordinateMode(player);
					return;
				}
			}else{
					this.changeMode(player);
			}
		}

		//モード確認
		if(getMode(player, plugin)){
			return;
		}

		if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			this.select(player, block);
		}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			this.write(player, block, event);
		}else if(event.getAction() == Action.LEFT_CLICK_AIR){
			this.clear(player);
		}

		event.setCancelled(true);
	}

	private void clear(Player player) {
		if(player.isSneaking() && Utility.removeMetadata(plugin, player, MATADETA_NAME)){
			player.sendMessage(ChatColor.DARK_GRAY + plugin.getPluginName() + ChatColor.AQUA + "クリアしました。");
		}
	}


	//ブロック書き込み処理
	private void write(Player player, Block target, PlayerInteractEvent event){
		BlockState block = Utility.getMetadata(plugin, player, MATADETA_NAME, BlockState.class);
		//String command = config.getSetBlockCommand() + " ";

		if(block == null){
			player.sendMessage(plugin.getPluginName() + ChatColor.RED +"ブロックを選択してください。");
			return;
		}

		if(player.isSneaking() == (CommandBlockAssist.getPlaceMode(player) == false)){
			target = Utility.getThePutBlock(target, event.getBlockFace());
		}

		CoordinateMode mode = getCoordinateMode(player);

		String command = getCommand(block, target, mode);

		if(!CommandBlockAssist.writingCommanBlock(target, command, player)){
			return;
		}

		player.sendMessage(plugin.getPluginName() + ChatColor.YELLOW + "[" + command + "]" + ChatColor.GOLD +"を書き込みました。");
	}

	public static String getCommand(BlockState block, Block commandBlock, CoordinateMode mode){
		StringBuilder command = new StringBuilder();
		command.append(config.getSetBlockCommand());
		command.append(" ");

		//座標
		if(mode == CoordinateMode.Relative){
			int x = block.getX() - commandBlock.getX();
			int y = block.getY() - commandBlock.getY();
			int z = block.getZ() - commandBlock.getZ();
			command.append(String.format("~%s ~%s ~%s", x, y, z));
		}else if(mode == CoordinateMode.Absolute){
			command.append(String.format("%s %s %s", block.getX(), block.getY(), block.getZ()));
		}
		//ID
		command.append(" ");
		if(config.isNameId()){
			String id = DataTagUtl.getBlockNameId(block.getTypeId());
			if(id != null){
				command.append(id);
			}else{
				command.append(block.getTypeId());
			}
		}else{
			command.append(block.getTypeId());
		}
		//メタ&データタグ
		if(config.getSetBlockDataTag()){
			String datatag = null;

			if(config.isExactDataTag()){
				datatag = DataTagUtl.getExactBlockDataTag(block);
			}else{
				datatag = DataTagUtl.getBlockDataTag(block);
			}


			if(datatag != null){
				command.append(" ");
				command.append(block.getRawData());

				if(datatag.equals("") == false){
					command.append(" ");
					command.append(config.getSetBlockHandlingType());
					command.append(" ");
					command.append(datatag);
				}
			}else{
				if (block.getRawData() != 0) {
					command.append(" ");
					command.append(block.getRawData());
				}
			}
		}else{
			if (block.getRawData() != 0) {
				command.append(" ");
				command.append(block.getRawData());
			}
		}
		return command.toString();
	}

	//ブロック選択処理
	private void select(Player player, Block block){
		BlockState nowState = block.getState();
		BlockState oldState = Utility.getMetadata(plugin, player, MATADETA_NAME, BlockState.class);
		if((oldState != null && nowState.equals(oldState)) == false){
			Utility.setMetadata(plugin, player, MATADETA_NAME, nowState);
			player.sendMessage(plugin.getPluginName() + ChatColor.YELLOW + plugin.getBlockString(nowState) + ChatColor.GOLD +"を選択しました。");
		}
	}



	public static boolean getMode(Player player,JavaPlugin plugin){
		Boolean mode = Utility.getMetadata(plugin, player, MATADETA_NAME + ".MODE", Boolean.class);
		return mode = (mode == null ? false : mode);
	}

	public static CoordinateMode getCoordinateMode(Player player){
		CoordinateMode mode = Utility.getMetadata(plugin, player, SetBlockTool.MATADETA_NAME + ".COORDINATE.MODE", CoordinateMode.class);
		if(mode == null){
			mode = CoordinateMode.Absolute;
		}
		return mode;
	}

	private void changeCoordinateMode(Player player) {
		CoordinateMode mode = getCoordinateMode(player);
		mode = getNextCoordinateMode(mode);
		player.sendMessage(plugin.getPluginName() + String.format("§e%s§6モードになりました。", getCoordinateModeName().get(mode)));
		Utility.setMetadata(plugin, player, MATADETA_NAME + ".COORDINATE.MODE", mode);
	}

	private CoordinateMode getNextCoordinateMode(CoordinateMode mode){
		CoordinateMode[] modes = CoordinateMode.values();
		boolean temp = false;
		for(int i=0;i<modes.length;i++){
			if(temp){
				return modes[i];
			}else if(mode == modes[i]){
				temp = true;
			}
		}
		return modes[0];
	}

	private void changeMode(Player player) {
		boolean mode = !getMode(player,plugin);
		Utility.setMetadata(plugin, player, MATADETA_NAME + ".MODE", mode);
		player.sendMessage(ChatColor.DARK_GRAY + plugin.getPluginName() + ChatColor.YELLOW + (mode ? "範囲モード" : "通常モード") + ChatColor.GOLD +"になりました。");
	}

	private Map<CoordinateMode, String> getCoordinateModeName(){
		Map<CoordinateMode, String> map = new HashMap<CoordinateMode, String>();
		map.put(CoordinateMode.Absolute, "絶対座標");
		map.put(CoordinateMode.Relative, "相対座標");
		return map;
	}

}
