package com.github.eajtciv.commandblockassist.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ToolInventoryHolder implements InventoryHolder {

	private Object id;

	public ToolInventoryHolder(Object id){
		this.id = id;
	}


	@Override
	public Inventory getInventory() {
		return null;
	}


	public Object getId() {
		return id;
	}


	public static boolean equalsId(InventoryHolder holder, Object id) {
		if(holder instanceof ToolInventoryHolder){
			ToolInventoryHolder toolHolder = (ToolInventoryHolder) holder;
			return toolHolder.getId().equals(id);
		}
		return false;
	}

}
