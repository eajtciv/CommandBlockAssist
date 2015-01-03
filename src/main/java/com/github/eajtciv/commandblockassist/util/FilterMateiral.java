package com.github.eajtciv.commandblockassist.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class FilterMateiral {

	Short meta;
	Material material;

	public FilterMateiral(Material material){
		this.material = material;
	}

	public FilterMateiral(Material material, Short meta){
		this.material = material;
		this.meta = meta;
	}

	public FilterMateiral(ItemStack item){
		this.material = item.getType();
		this.meta = item.getDurability();
	}

	public boolean equals(Block block) {
		if((meta == null || block.getData() == meta) && block.getType() == material){
			return true;
		}
		return false;
	}

}
