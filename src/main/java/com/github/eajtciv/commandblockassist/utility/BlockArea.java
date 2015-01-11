package com.github.eajtciv.commandblockassist.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * @author eajtciv
 */
public class BlockArea {

	private World world;
	private Vector first;
	private Vector second;


	public List<Block> getAreaBlock() {
		List<Block> list = new ArrayList<Block>();
		if (this.isValid()) {
			int baseX = Math.min(first.getBlockX(), second.getBlockX());
			int baseY = Math.min(first.getBlockY(), second.getBlockY());
			int baseZ = Math.min(first.getBlockZ(), second.getBlockZ());
			for (int x = 0; x < this.getSizeX() + 1; x++) {
				for (int y = 0; y < this.getSizeY() + 1; y++) {
					for (int z = 0; z < this.getSizeZ() + 1; z++) {
						Block block = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
						list.add(block);
					}
				}
			}
		}
		return list;
	}

	public int getSizeX() {
		if (first != null && second != null) {
			return distance(first.getBlockX(), second.getBlockX());
		}
		return -1;
	}

	public int getSizeY() {
		if (first != null && second != null) {
			return distance(first.getBlockY(), second.getBlockY());
		}
		return -1;
	}

	public int getSizeZ() {
		if (first != null && second != null) {
			return distance(first.getBlockZ(), second.getBlockZ());
		}
		return -1;
	}

	public int distance(int a, int b) {
		int max = Math.max(a, b);
		int min = Math.min(a, b);
		if (min < 0) {
			max += Math.abs(min);
			min = 0;
		}
		return max - min;
	}

	public boolean setSecondPosition(Block block) {
		if (this.getWorld() == null || block.getWorld().equals(world)) {
			Vector old = second;
			second = new Vector(block.getX(), block.getY(), block.getZ());
			world = block.getWorld();
			return old == null || second.equals(old) == false;
		}
		return false;
	}

	public boolean setFastPosition(Block block) {
		if (this.getWorld() == null || block.getWorld().equals(world)) {
			Vector old = first;
			first = new Vector(block.getX(), block.getY(), block.getZ());
			world = block.getWorld();
			return old == null || first.equals(old) == false;
		}
		return false;
	}

	public boolean isValid() {
		return first != null && second != null;
	}

	public World getWorld() {
		return world;
	}

}
