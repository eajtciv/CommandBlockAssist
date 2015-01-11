package com.github.eajtciv.commandblockassist.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Filterdata {


	List<FilterMateiral> list = new ArrayList<FilterMateiral>();
	public Filterdata(Inventory inventory){
		for(ItemStack item : inventory.getContents()){
			if(item != null){
				if(item.getType() == Material.FEATHER){
					list.add(new FilterMateiral(Material.AIR));
				}else if(item.getType() == Material.SIGN){
					list.add(new FilterMateiral(Material.SIGN_POST));
					list.add(new FilterMateiral(Material.WALL_SIGN));
				}else if(item.getType() == Material.LAVA_BUCKET){
					list.add(new FilterMateiral(Material.LAVA));
					list.add(new FilterMateiral(Material.STATIONARY_LAVA));
				}else if(item.getType() == Material.WATER_BUCKET){
					list.add(new FilterMateiral(Material.WATER));
					list.add(new FilterMateiral(Material.WATER_LILY));
					list.add(new FilterMateiral(Material.STATIONARY_WATER));
				}else if(item.getType() == Material.REDSTONE_TORCH_ON){
					list.add(new FilterMateiral(Material.REDSTONE_TORCH_ON));
					list.add(new FilterMateiral(Material.REDSTONE_TORCH_OFF));
				}else{
					list.add(new FilterMateiral(item));
				}
			}
		}
	}

	public boolean contains(Block block) {
		for(FilterMateiral filter : list){
			if(filter.equals(block)){
				return true;
			}
		}
		return false;
	}



}
