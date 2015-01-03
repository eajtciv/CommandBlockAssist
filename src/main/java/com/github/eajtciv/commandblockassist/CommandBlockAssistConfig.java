package com.github.eajtciv.commandblockassist;

import java.io.File;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import com.github.eajtciv.commandblockassist.util.ConfigManager;
import com.github.eajtciv.commandblockassist.util.Utility;

public class CommandBlockAssistConfig {

	private String placeModeName = "COMMANDBLOCKASSIST.PLACE.MODE";

	private boolean setBlockEnable = true;
	private boolean tpEnable = true;
	private boolean copyEnable = true;
	private boolean giveEnable = true;
	private boolean replaceEnable = true;

	private Material setBlockTool = Material.DIAMOND_HOE;
	private String setBlockCommand = "setblock";
	private String setBlockHandlingType = "replace";
	private boolean setBlockDataTag = true;

	private Material tpTool = Material.GOLD_HOE;
	private String tpCommand = "tp";
	private String tpTarget = "@p";

	private Material copyTool = Material.IRON_HOE;


	private Material giveTool = Material.STONE_HOE;
	private String giveCommand = "give";
	private String giveTarget = "@p";
	private boolean giveDataTag = true;

	private Material replaceTool = Material.WOOD_HOE;

	private boolean placeMode = false;

	public static CommandBlockAssistConfig getConfig(){
		CommandBlockAssistConfig config = new CommandBlockAssistConfig();
		config.loadConfig();
		return config;
	}

	public void loadConfig(){
		File ckonfigFile = new File(CommandBlockAssist.getPlugin().getDataFolder(), "config.yml");
		FileConfiguration config = null;
		try {
			config = ConfigManager.getYaml(ckonfigFile);
		} catch (InvalidConfigurationException | IOException e) {
			return;
		}

		placeMode = config.getBoolean("PlaceMode", placeMode);

		//ENABLE
		ConfigurationSection enable = config.getConfigurationSection("Enable");
		if(enable != null){
			setBlockEnable = enable.getBoolean("SetBlock", setBlockEnable);
			tpEnable = enable.getBoolean("Tp", tpEnable);
			copyEnable = enable.getBoolean("Copy", copyEnable);
			giveEnable = enable.getBoolean("Give", giveEnable);
			replaceEnable = enable.getBoolean("Replace", replaceEnable);
		}


		//TOOLS
		ConfigurationSection setblock = config.getConfigurationSection("SetBlock");
		if(setblock != null){
			setBlockTool = Utility.getMaterial(setblock.getString("Tool", setBlockTool.name()));
			setBlockCommand = setblock.getString("Commmand", setBlockCommand);
			setBlockHandlingType = setblock.getString("HandlingType", setBlockHandlingType);
			setBlockDataTag = setblock.getBoolean("DataTag", setBlockDataTag);
		}

		ConfigurationSection tp = config.getConfigurationSection("Tp");
		if(tp != null){
			tpTool = Utility.getMaterial(tp.getString("Tool", tpTool.name()));
			tpCommand = tp.getString("Commmand", tpCommand);
			tpTarget = tp.getString("Target", tpTarget);
		}

		ConfigurationSection copy = config.getConfigurationSection("Copy");
		if(copy != null){
			copyTool = Utility.getMaterial(copy.getString("Tool",copyTool.name()));
		}

		ConfigurationSection give = config.getConfigurationSection("Give");
		if(give != null){
			giveTool = Utility.getMaterial(give.getString("Tool", giveTool.name()));
			giveCommand = give.getString("Commmand", giveCommand);
			giveTarget = give.getString("Target", giveTarget);
			giveDataTag = give.getBoolean("DataTag", giveDataTag);
		}

		ConfigurationSection replace = config.getConfigurationSection("Replace");
		if(replace != null){
			replaceTool = Utility.getMaterial(replace.getString("Tool", replaceTool.name()));
		}
	}

	//GIVE
	public ItemStack getGiveTool(){
		return new ItemStack(giveTool);
	}

	public boolean getGiveDataTag(){
		return this.giveDataTag;
	}

	public String getGiveCommand(){
		return this.giveCommand;
	}

	//COPY
	public ItemStack getCopyTool(){
		return new ItemStack(copyTool);
	}

	//TP
	public ItemStack getTpTool(){
		return new ItemStack(tpTool);
	}

	public String getTpCommand(){
		return this.tpCommand;
	}

	//SETBLOCK
	public ItemStack getSetBlockTool(){
		return new ItemStack(setBlockTool);
	}

	public String getSetBlockCommand(){
		return this.setBlockCommand;
	}

	public boolean getSetBlockDataTag(){
		return this.setBlockDataTag;
	}

	//REPLACE
	public ItemStack getReplaceTool(){
		return new ItemStack(replaceTool);
	}

	public String getPlaceModeName() {
		return placeModeName;
	}

	public boolean isPlaceMode() {
		return placeMode;
	}

	//有効化可否
	public boolean isSetBlockEnable() {
		return setBlockEnable;
	}

	public boolean isTpEnable() {
		return tpEnable;
	}

	public boolean isCopyEnable() {
		return copyEnable;
	}

	public boolean isGiveEnable() {
		return giveEnable;
	}

	public boolean isReplaceEnable() {
		return replaceEnable;
	}

	public String getSetBlockHandlingType() {
		return setBlockHandlingType;
	}

	public String getGiveTarget() {
		return giveTarget;
	}

	public String getTpTarget() {
		return tpTarget;
	}

}
