package com.github.eajtciv.commandblockassist;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.eajtciv.commandblockassist.tools.CommandBlockCopyTool;
import com.github.eajtciv.commandblockassist.tools.GiveTool;
import com.github.eajtciv.commandblockassist.tools.RangeSetBlockTool;
import com.github.eajtciv.commandblockassist.tools.ReplaceTool;
import com.github.eajtciv.commandblockassist.tools.SetBlockTool;
import com.github.eajtciv.commandblockassist.tools.TpTool;
import com.github.eajtciv.commandblockassist.util.CommandBlockUtl;
import com.github.eajtciv.commandblockassist.util.Utility;


public class CommandBlockAssist extends JavaPlugin {

	private static CommandBlockAssist plugin;
	private static CommandBlockAssistConfig config;

	public void onEnable(){
		plugin  = this;
		//Utility.JarFileCopy(this.getFile(), "config.yml", this.getDataFolder());
		Utility.zipDirCopy(this.getFile(), "resources", this.getDataFolder());
		config = CommandBlockAssistConfig.getConfig();

		// ================================ 登録 ================================

		CommandBlockAssistCommand command = new CommandBlockAssistCommand();
		PluginCommand cbaCommand = this.getCommand("commandblockassist");
		cbaCommand.setExecutor(command);
		cbaCommand.setTabCompleter(command);

		if(config.isTpEnable())
			this.getServer().getPluginManager().registerEvents(new TpTool(), this);

		if(config.isGiveEnable())
			this.getServer().getPluginManager().registerEvents(new GiveTool(), this);

		if(config.isSetBlockEnable())
			this.getServer().getPluginManager().registerEvents(new SetBlockTool(), this);

		if(config.isSetBlockEnable())
			this.getServer().getPluginManager().registerEvents(new RangeSetBlockTool(), this);

		if(config.isCopyEnable())
			this.getServer().getPluginManager().registerEvents(new CommandBlockCopyTool(), this);

		if(config.isReplaceEnable())
			this.getServer().getPluginManager().registerEvents(new ReplaceTool(), this);
	}



	public static boolean writingCommanBlock(Block target, String command, Player player){
		if(CommandBlockUtl.equalsCommanBlock(target, command)){
			return false;
		}
		CommandBlockUtl.writingCommanBlock(target, command, player);
		return true;
	}



	public String getPluginName(){
		return ChatColor.DARK_GRAY + "[" + this.getName() + "]" + " ";
	}



	@SuppressWarnings("deprecation")
	public String getBlockString(BlockState b){
		return "[X:" + b.getX() + " Y:" + b.getY() + " Z:" + b.getZ() + (b.getTypeId()==0?"":" TYPE:" + b.getTypeId()) + (b.getRawData()==0?"":" DATA:"+b.getRawData()) + "]";
	}

	public String getCoordinateString(Block block){
		return "[X:" + block.getX() + " Y:" + block.getY() + " Z:" + block.getZ()+"]";
	}






	public static Boolean getPlaceMode(Player player){
		Boolean placeMode = Utility.getMetadata(plugin, player, config.getPlaceModeName(), Boolean.class);
		if(placeMode == null){
			placeMode = config.isPlaceMode();
		}
		return placeMode;
	}


	public static CommandBlockAssist getPlugin() {
		return plugin;
	}



	public static CommandBlockAssistConfig getPluginConfig() {
		return config;
	}
}
