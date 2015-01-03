package com.github.eajtciv.commandblockassist.tools;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
import com.github.eajtciv.commandblockassist.util.Utility;
/**
 * @author eajtciv
 */
public class TpTool implements Listener {

	private CommandBlockAssist plugin = CommandBlockAssist.getPlugin();
	private CommandBlockAssistConfig config = CommandBlockAssist.getPluginConfig();

	private final String MATADETA_NAME = "COMMANDBLOCKASSIST.TP";
	private final String PERMISSION_NAME = "COMMANDBLOCKASSIST.TP";

	private enum Modes {Detail, Default};


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
		if(handItem == null || handItem.isSimilar(config.getTpTool()) == false){
			return;
		}


		if(event.getAction() == Action.LEFT_CLICK_AIR){
			if(player.isSneaking()){
				this.clear(player);
			}else{
				this.select(player);
			}
		}else if(event.getAction() == Action.LEFT_CLICK_BLOCK){
			this.select(player);
		}else if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
			this.write(player, block, event);
		}else if(event.getAction() == Action.RIGHT_CLICK_AIR){
			if(player.isSneaking()){
				this.teleport(player);
			}else{
				this.changeMode(player);
			}
		}
		event.setCancelled(true);
	}



	private void clear(Player player) {
		if (Utility.removeMetadata(plugin, player, MATADETA_NAME)||
				Utility.removeMetadata(plugin, player, MATADETA_NAME + ".MODE")){
			player.sendMessage(plugin.getPluginName() + ChatColor.AQUA + "クリアしました。");
		}
	}



	//プレイヤーをテレポート
	private void teleport(Player player){
		Location location = Utility.getMetadata(plugin, player, MATADETA_NAME, Location.class);

		if(location == null){
			player.sendMessage(plugin.getPluginName() + ChatColor.RED +"座標を記録してください。");
			return;
		}

		location = getAirBlockLocation(location.getBlock());

		Block block = location.getBlock();

		Location nowLocation = player.getLocation();
		location.setPitch(nowLocation.getPitch());
		location.setYaw(nowLocation.getYaw());

		player.teleport(location);
		player.sendMessage(plugin.getPluginName() + String.format("§e[X:%s Y:%s Z:%s]§aにテレポートしました。", block.getX(), block.getY(), block.getZ()));
	}


	//ブロック書き込み処理
	private void write(Player player, Block target, PlayerInteractEvent event){

		Modes mode = Utility.getMetadata(plugin, player, MATADETA_NAME + ".MODE", Modes.class);
		if(mode == null)
			mode = Modes.Default;

		Location location = Utility.getMetadata(plugin, player, MATADETA_NAME, Location.class);

		if(location == null){
			player.sendMessage(plugin.getPluginName() + ChatColor.RED +"座標を記録してください。");
			return;
		}

		String command = getCommand(location, mode);

		if(player.isSneaking() == (CommandBlockAssist.getPlaceMode(player) == false)){
			target = Utility.getThePutBlock(target, event.getBlockFace());
		}

		if(!CommandBlockAssist.writingCommanBlock(target, command, player)){
			return;
		}

		player.sendMessage(plugin.getPluginName() + ChatColor.YELLOW + "[" + command + "]" + ChatColor.GOLD +"を書き込みました。");
	}


	private String getCommand(Location location, Modes mode){
		StringBuilder command = new StringBuilder();
		command.append(config.getTpCommand());
		command.append(" ");

		command.append(config.getTpTarget());
		command.append(" ");

		Block block = location.getBlock();

		String x = null;
		String y = null;
		String z = null;

		if(mode == Modes.Default){
			x = String.valueOf(block.getX());
			y = String.valueOf(block.getY());
			z = String.valueOf(block.getZ());
		}else if(mode == Modes.Detail){
			x = String.format("%.5f", location.getX());
			y = String.format("%.3f", location.getY());
			z = String.format("%.5f", location.getZ());

			if(getFew(new Double(x)) == 0.0) x = String.valueOf(block.getX());
			if(getFew(new Double(y)) == 0.0) y = String.valueOf(block.getY());
			if(getFew(new Double(z)) == 0.0) z = String.valueOf(block.getZ());
		}
		command.append(String.format("%s %s %s", x, y, z));
		return command.toString();
	}

	public double getFew(double num){
		return num - new Double(num).intValue() ;
	}

	private void select(Player player){
		Location loc = player.getLocation();
		Location old = Utility.getMetadata(plugin, player, MATADETA_NAME, Location.class);
		Block nowBlock = null,oldBlock = null;

		nowBlock = loc.getBlock();
		if(old != null)oldBlock = old.getBlock();
		if((oldBlock != null && nowBlock.equals(oldBlock)) == false){
			Utility.setMetadata(plugin, player, MATADETA_NAME, loc);
			player.sendMessage(plugin.getPluginName() + String.format("§e[X:%s Y:%s Z:%s]§6を記録しました。", nowBlock.getX(), nowBlock.getY(), nowBlock.getZ()));
		}
	}

	public Map<Material,Float> getAirBlocks(){
		Map<Material,Float> airs = new HashMap<Material,Float>();
		airs.put(Material.AIR, 0f);
		airs.put(Material.LEVER, 0f);
		airs.put(Material.POWERED_RAIL, 0f);
		airs.put(Material.RAILS, 0f);
		airs.put(Material.DETECTOR_RAIL, 0f);
		airs.put(Material.STONE_PLATE, 0f);
		airs.put(Material.WOOD_PLATE, 0f);
		airs.put(Material.STATIONARY_WATER, 0f);

		airs.put(Material.STEP, 0.5f);

		airs.put(Material.FENCE, 1.5f);
		airs.put(Material.FENCE_GATE, 1.5f);
		airs.put(Material.NETHER_FENCE, 1.5f);
		return airs;
	}

	public Location getAirBlockLocation(Block block){
		Map<Material, Float> airBlocks = getAirBlocks();
		int maxHeight = block.getWorld().getMaxHeight();
		Location loc = block.getLocation();
		int aircnt = 0;
		int ensure = 2;
		for(int height = block.getY();height < maxHeight;height++){
			Float mod = airBlocks.get(loc.getBlock().getType());
			if(mod != null && mod == 0f){
				aircnt++;
				if(aircnt >= ensure){
					loc.add(0, -(ensure-1), 0);
					mod = airBlocks.get(loc.getBlock().getType());
					loc.add(0.5, mod, 0.5);
					return loc;
				}
			}else{
				aircnt = 0;
			}
			loc.add(0, 1, 0);
		}
		return null;
	}


	private void changeMode(Player player) {
		Modes mode = Utility.getMetadata(plugin, player, MATADETA_NAME + ".MODE", Modes.class);
		if(mode == null){
			mode = Modes.Detail;
		}else{
			mode = getNextMode(mode);
		}
		player.sendMessage(plugin.getPluginName() + String.format("§e%s§6モードになりました。", getModeName().get(mode)));
		Utility.setMetadata(plugin, player, MATADETA_NAME + ".MODE", mode);
	}

	private Modes getNextMode(Modes mode){
		Modes[] modes = Modes.values();
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

	private Map<Modes, String> getModeName(){
		Map<Modes, String> map = new HashMap<Modes, String>();
		map.put(Modes.Default, "通常");
		map.put(Modes.Detail, "詳細");
		return map;
	}
}
