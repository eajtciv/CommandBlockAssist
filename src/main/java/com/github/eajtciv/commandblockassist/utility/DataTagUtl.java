package com.github.eajtciv.commandblockassist.utility;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

/**
 * データタグ取得
 * @author eajtciv
 */
public class DataTagUtl {

	private static String nmsPackage = getNMSPackage();



	/**
	 * 適当にデータタグを取得
	 */
	public static String getExactItemDataTag(ItemStack item) {
		try {
			Object nmsItem = ReflectUtl.getValue(item, "handle");
			Object tag = ReflectUtl.getValue(nmsItem, "tag");
			if (tag != null) {
				return tag.toString();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}



	/**
	 * 適当にデータタグを取得
	 */
	public static String getExactBlockDataTag(BlockState block) {
		try {
			Class<?> tileEntityClass = Class.forName(nmsPackage + ".TileEntity");
			Class<?> nbtTagCompoundClass = Class.forName(nmsPackage + ".NBTTagCompound");

			for (Field f : block.getClass().getDeclaredFields()) {
				f.setAccessible(true);
				Object tileEntity = f.get(block);
				if (tileEntityClass.isInstance(tileEntity)) {
					Object nbtTagCompound = nbtTagCompoundClass.newInstance();
					ReflectUtl.invoke(tileEntity, "b", nbtTagCompound);
					return nbtTagCompound.toString();
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}



	/**
	 * BlockのIDを適当に取得します
	 * @param id BlockID
	 * @return NameID
	 */
	public static String getBlockNameId(int id) {
		try {
			Class<?> nmsBlockClass = Class.forName(nmsPackage + ".Block");

			Object REGISTRY = ReflectUtl.getField(nmsBlockClass, "REGISTRY").get(Bukkit.getServer());
			Object registryID = ReflectUtl.getValue(REGISTRY, "a");
			Object a = ReflectUtl.getValue(registryID, "a");
			Object c = ReflectUtl.getValue(REGISTRY, "c");
			Map<?, ?> cMap = (Map<?, ?>) c;

			Method get = ReflectUtl.getMethodByName(a.getClass(), "get");
			for (Entry<?, ?> e : cMap.entrySet()) {
				Object nmsBlock = e.getValue();
				String mateirlaName = e.getKey().toString();
				// Bukkit 1.7.x
				Object id2 = get.invoke(a, System.identityHashCode(nmsBlock));
				if (id2 == null) {// Spigot 1.8
					id2 = get.invoke(a, nmsBlock);
				}
				if (id == (int) id2) {
					return mateirlaName;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}



	/**
	 * ItemのIDを適当に取得します
	 * @param id ItemID
	 * @return NameID
	 */
	public static String getItemNameId(int id) {
		try {
			Class<?> nmsItemClass = Class.forName(nmsPackage + ".Item");

			Object REGISTRY = ReflectUtl.getField(nmsItemClass, "REGISTRY").get(Bukkit.getServer());
			Object registryID = ReflectUtl.getValue(REGISTRY, "a");
			Object a = ReflectUtl.getValue(registryID, "a");
			Object c = ReflectUtl.getValue(REGISTRY, "c");
			Map<?, ?> cMap = (Map<?, ?>) c;

			Method get = ReflectUtl.getMethodByName(a.getClass(), "get");
			for (Entry<?, ?> e : cMap.entrySet()) {
				Object nmsBlock = e.getValue();
				String mateirlaName = e.getKey().toString();
				// Bukkit 1.7.x
				Object id2 = get.invoke(a, System.identityHashCode(nmsBlock));
				if (id2 == null) {// Spigot 1.8
					id2 = get.invoke(a, nmsBlock);
				}
				if (id == (int) id2) {
					return mateirlaName;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}



	public static String getBlockDataTag(BlockState block){

		DataTagItem list = new DataTagItem();
		//コマンドブロック
		if(block instanceof CommandBlock){
			CommandBlock cb = (CommandBlock)block;
			String command = cb.getCommand();
			if(!command.equals(""))
				list.add("Command", "\""+command+"\"");
		}

		//看板
		if(block instanceof Sign){
			Sign sign = (Sign)block;
			DataTagItem line = new DataTagItem();
			for(int cnt=0;cnt<sign.getLines().length;cnt++){
				String mes = sign.getLines()[cnt];
				if(!mes.equals(""))
					line.add("Text" + (cnt+1), "\"" + mes + "\"");
			}
			list.add(line);
		}

		//頭
		if(block instanceof Skull){
			Skull skull = (Skull)block;
			DataTagItem line = new DataTagItem();
			line.add("Rot", getSkullRot(skull.getRotation()));
			if(skull.hasOwner()){
				line.add("SkullType", skull.getSkullType().ordinal());
				line.add("ExtraType", skull.getOwner());
			}
			list.add(line);
		}

		//イベントリ
		Inventory inventory = null;
		if(block.getType() == Material.CHEST){
			inventory = ((Chest)block).getBlockInventory();;
		}else if(block.getType() == Material.DROPPER){
			inventory = ((Dropper)block).getInventory();
		}else if(block.getType() == Material.DISPENSER){
			inventory = ((Dispenser)block).getInventory();
		}else if(block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE){
			inventory = ((Furnace)block).getInventory();
		}else if(block.getType() == Material.BREWING_STAND || block.getType() == Material.BREWING_STAND_ITEM){
			inventory = ((BrewingStand)block).getInventory();
		}else if(block.getType() == Material.HOPPER){
			inventory = ((Hopper)block).getInventory();
		}

		if(inventory != null){
			DataTagItem items = getInventoryItemTag(inventory);
			if(items != null)
				list.add("Items", items);
		}

		return (list.toString().length() != 0 ? list.toString() : null);
	}



	@SuppressWarnings("deprecation")
	private static DataTagItem getInventoryItemTag(Inventory inventory){
		DataTagItem items = new DataTagItem(DataTagItem.Type.SquareBracket);
		for(int cnt=0;cnt<inventory.getSize();cnt++){
			ItemStack item = inventory.getItem(cnt);
			if(item != null){
				DataTagItem dataTag = new DataTagItem();
				dataTag.add("id", item.getTypeId());
				if(item.getDurability() != 0) dataTag.add("Damage", item.getDurability());
				dataTag.add("Count", item.getAmount());
				dataTag.add("Slot", cnt);
				String datatag = getItemDataTag(item);
				if(datatag != null) dataTag.add("tag", datatag);
				items.add(dataTag);
			}
		}
		return items;
	}

	@SuppressWarnings("deprecation")
	public static String getItemDataTag(ItemStack item){
		DataTagItem dataTag = new DataTagItem();
		//for(int cnt=0;cnt<10;cnt++) list.add(null);
		ItemMeta meta = item.getItemMeta();

		DataTagItem display = new DataTagItem();
		//表示名
		if(meta.hasDisplayName()){
			display.add("Name", "\""+meta.getDisplayName()+"\"");
		}
		//説明
		if(meta.hasLore()){
			DataTagItem lore = new DataTagItem(DataTagItem.Type.SquareBracket);
			for(String str : meta.getLore()){
				lore.add(str);
			}
			display.add("Lore", lore);
		}

		//革装備色
		if(meta instanceof LeatherArmorMeta){
			LeatherArmorMeta lam = (LeatherArmorMeta) meta;
			Color color = lam.getColor();
			if(color.asRGB() != 10511680)
				display.add("color", color.asRGB());
		}

		dataTag.add("display", display);

		//エンチャント
		if(item.hasItemMeta()){
			DataTagItem enchant = new DataTagItem();
			Map<Enchantment, Integer> enchantmap = item.getEnchantments();
			for(Entry<Enchantment, Integer> e : enchantmap.entrySet()){
				DataTagItem items = new DataTagItem();
				items.add("id", e.getKey().getId());
				items.add("lvl", e.getValue());
				enchant.add(items);
			}
			dataTag.add("ench" ,enchant);
		}


		//エンチャント本
		if(meta instanceof EnchantmentStorageMeta){
			DataTagItem enchant = new DataTagItem();
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) meta;
			for(Entry<Enchantment, Integer> e : esm.getStoredEnchants().entrySet()){
				DataTagItem items = new DataTagItem();
				items.add("id", e.getKey().getId());
				items.add("lvl", e.getValue());
				enchant.add(items);
			}
			dataTag.add("ench" ,enchant);
		}

		//頭！！
		if(meta instanceof SkullMeta){
			SkullMeta sm = (SkullMeta) meta;
			if(sm.hasOwner())
				dataTag.add("SkullOwner:"+sm.getOwner());
		}

		//本
		if(meta instanceof BookMeta){
			BookMeta bm = (BookMeta) meta;
			if(bm.hasPages()){
				List<String> page = new ArrayList<String>();
				for(String line : bm.getPages()){
					String mes = "";
					String[] lines = line.split("\n");
					for(int cnt=0;cnt<lines.length;cnt++){
						String str = lines[cnt];
						if(!(lines.length > cnt && lines.length == 0))
							mes += "\n";
						mes += str;
					}
					page.add(mes);
				}
				dataTag.add("pages:["+Utility.listJoin(page,",")+"]");
				//list.add("pages:["+Utility.listJoin(bm.getPages(),",")+"]");
			}
			if(bm.hasAuthor())
				dataTag.add("author:"+bm.getAuthor());

			if(bm.hasTitle())
				dataTag.add("title:"+bm.getTitle());
		}

		//ポーション
		if(meta instanceof PotionMeta){
			PotionMeta pm = (PotionMeta) meta;
			if(pm.hasCustomEffects()){
				DataTagItem potion = new DataTagItem();
				for(PotionEffect pe : pm.getCustomEffects()){
					potion.add("Id", pe.getType().getId());
					potion.add("Amplifier", pe.getAmplifier());
					potion.add("Duration", pe.getDuration());
				}
				dataTag.add("CustomPotionEffects", potion);
			}
		}

		//花火
		if(meta instanceof FireworkMeta){
			FireworkMeta fm = (FireworkMeta) meta;
			DataTagItem fireworks = new DataTagItem();
			fireworks.add("Flight", fm.getPower());
			DataTagItem explosions = new DataTagItem(DataTagItem.Type.SquareBracket);
			for(FireworkEffect fe : fm.getEffects()){
				DataTagItem items = new DataTagItem();
				items.add("Flicker", (fe.hasFlicker() ? 1 : 0));
				items.add("Trail", (fe.hasTrail() ? 1 : 0));
				items.add("Type", getFireworkEffectType(fe.getType()));
				DataTagItem colors = new DataTagItem(DataTagItem.Type.SquareBracket);
				for(Color color : fe.getColors()){
					colors.add(color.asRGB());
				}
				items.add("Colors", colors);
				explosions.add(items);
			}
			fireworks.add("Explosions", explosions);
			dataTag.add("Fireworks", fireworks);
		}

		return (dataTag.toString().length() != 0 ? dataTag.toString():null);
	}

	private static int getFireworkEffectType(FireworkEffect.Type type) {
		switch(type){
		case BALL:
			return 0;
		case BALL_LARGE:
			return 1;
		case STAR:
			return 2;
		case CREEPER:
			return 3;
		case BURST:
			return 4;
		default:
			return -1;
		}
	}

	private static int getSkullRot(BlockFace face) {
		switch(face){
		case NORTH:
			return 0;
		case NORTH_NORTH_EAST:
			return 1;
		case NORTH_EAST:
			return 2;
		case EAST_NORTH_EAST:
			return 3;
		case EAST:
			return 4;
		case EAST_SOUTH_EAST:
			return 5;
		case SOUTH_EAST:
			return 6;
		case SOUTH_SOUTH_EAST:
			return 7;
		case SOUTH:
			return 8;
		case SOUTH_SOUTH_WEST:
			return 9;
		case SOUTH_WEST:
			return 10;
		case WEST_SOUTH_WEST:
			return 11;
		case WEST:
			return 12;
		case WEST_NORTH_WEST:
			return 13;
		case NORTH_WEST:
			return 14;
		case NORTH_NORTH_WEST:
			return 15;
		default:
			return -1;
		}
	}



	/**
	 * net.minecraft.serverのパッケージを返します。
	 * @return net.minecraft.server.xxx
	 */
	private static String getNMSPackage() {
		try {
			Server bukkitServer = Bukkit.getServer();
			Method getServer = ReflectUtl.getMethod(bukkitServer.getClass(), "getServer");
			return getServer.getReturnType().getPackage().getName();
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

}
