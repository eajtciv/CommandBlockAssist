package com.github.eajtciv.commandblockassist.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
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
 * データタグを作成します。
 * @author eajtciv
 */
public class CreatingDataTag {

	public static String getBlockDataTag(BlockState block){
		List<String> list = new ArrayList<String>();
		//コマンドブロック
		if(block instanceof CommandBlock){
			CommandBlock cb = (CommandBlock)block;
			String command = cb.getCommand();
			if(!command.equals(""))
				list.add("{Command:"+command+"}");
		}

		//看板
		if(block instanceof Sign){
			Sign sign = (Sign)block;
			List<String> line = new ArrayList<String>();
			for(int cnt=0;cnt<sign.getLines().length;cnt++){
				String mes = sign.getLines()[cnt];
				if(!mes.equals(""))
					line.add("Text" + (cnt+1) + ":\"" + mes + "\"");
			}
			list.add("{"+Utility.listJoin(line,",")+"}");
		}

		//頭
		if(block instanceof Skull){
			Skull skull = (Skull)block;
			List<String> line = new ArrayList<String>();
			line.add("SkullType:"+skull.getSkullType().ordinal());
			if(skull.hasOwner()){
				line.add("Rot:"+skull.getRotation().ordinal());
				line.add("ExtraType:"+skull.getOwner());
			}
			list.add("{"+Utility.listJoin(line,",")+"}");
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
			String items = getInventoryItemTag(inventory);
			if(items != null)
				list.add("{"+items+"}");
		}

		String out = Utility.listJoin(list,",");
		return (out.length() != 0 ? out : null);
	}

	@SuppressWarnings("deprecation")
	private static String getInventoryItemTag(Inventory inventory){
		List<String> items = new ArrayList<String>();
		for(int cnt=0;cnt<inventory.getSize();cnt++){
			ItemStack item = inventory.getItem(cnt);
			if(item == null) continue;
			List<String> data = new ArrayList<String>();
			data.add("id:"+item.getTypeId());
			if(item.getDurability() != 0) data.add("Damage:"+item.getDurability());
			data.add("Count:"+item.getAmount());
			data.add("Slot:"+cnt);
			String datatag = getItemDataTag(item);
			if(datatag != null) data.add("tag:"+datatag);
			items.add("{"+Utility.listJoin(data,",")+"}");
		}
		if(items.size() != 0)
			return "Items:["+Utility.listJoin(items,",")+"]";
		return null;
	}

	@SuppressWarnings("deprecation")
	public static String getItemDataTag(ItemStack item){
		List<String> list = new ArrayList<String>();
		//for(int cnt=0;cnt<10;cnt++) list.add(null);
		ItemMeta meta = item.getItemMeta();

		List<String> display = new ArrayList<String>();
		for(int cnt=0;cnt<10;cnt++) display.add(null);
		//表示名
		if(meta.hasDisplayName()){
			display.add("Name:\""+meta.getDisplayName()+"\"");
		}
		//説明
		if(meta.hasLore()){
			String lore = "";
			for(String str : meta.getLore()){
				if(lore.length() != 0){lore += ",";}
				lore += str;
			}
			display.add("Lore:["+lore+"]");
		}

		//革装備色
		if(meta instanceof LeatherArmorMeta){
			LeatherArmorMeta lam = (LeatherArmorMeta) meta;
			Color color = lam.getColor();
			if(color.asRGB() != 10511680)
				display.add("color:"+color.asRGB()+"");
		}

		String display_out = Utility.listJoin(display,",");
		list.add((display_out.length() != 0 ? "display:{"+display_out+"}" : null));

		//エンチャント

		if(item.hasItemMeta()){
			Map<Enchantment, Integer> enchantmap = item.getEnchantments();
			String enchant = null;
			for(Entry<Enchantment, Integer> e : enchantmap.entrySet()){
				if(enchant == null){enchant = "";}
				if(enchant.length() != 0){enchant += ",";}
				enchant += "{id:" + e.getKey().getId() + ",lvl:" + e.getValue() + "}";
			}
			list.add((enchant!=null?"ench:["+enchant+"]":null));
		}


		//エンチャント本！！
		if(meta instanceof EnchantmentStorageMeta){
			String enchant = null;
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) meta;
			for(Entry<Enchantment, Integer> e : esm.getStoredEnchants().entrySet()){
				if(enchant == null){enchant = "";}
				if(enchant.length() != 0){enchant += ",";}
				enchant += "{id:" + e.getKey().getId() + ",lvl:" + e.getValue() + "}";
			}
			list.add((enchant!=null?"StoredEnchantments:["+enchant+"]":null));
		}

		//頭！！
		if(meta instanceof SkullMeta){
			SkullMeta sm = (SkullMeta) meta;
			if(sm.hasOwner())
				list.add("SkullOwner:"+sm.getOwner());
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
				list.add("pages:["+Utility.listJoin(page,",")+"]");
				//list.add("pages:["+Utility.listJoin(bm.getPages(),",")+"]");
			}
			if(bm.hasAuthor())
				list.add("author:"+bm.getAuthor());

			if(bm.hasTitle())
				list.add("title:"+bm.getTitle());
		}

		//ポーション
		if(meta instanceof PotionMeta){
			PotionMeta pm = (PotionMeta) meta;
			if(pm.hasCustomEffects()){
				List<String> potion = new ArrayList<String>();
				for(PotionEffect pe : pm.getCustomEffects()){
					potion.add( "{Id:"+pe.getType().getId());
					potion.add( "Amplifier:"+pe.getAmplifier());
					potion.add( "Duration:"+pe.getDuration()+"}");
				}
				list.add("CustomPotionEffects:["+Utility.listJoin(potion,",")+"]");
			}
		}

		//花火
		if(meta instanceof FireworkMeta){
			FireworkMeta fm = (FireworkMeta) meta;
			List<String> fireworks = new ArrayList<String>();

			fireworks.add("Flight:"+fm.getPower()+"");
			List<String> explosions = new ArrayList<String>();
			for(FireworkEffect fe : fm.getEffects()){
				explosions.add("{Flicker:"+(fe.hasFlicker() ? 1 : 0));
				explosions.add("Trail:"+(fe.hasTrail() ? 1 : 0));

				Type type = fe.getType();
				int id =0;
				if(type == FireworkEffect.Type.BALL_LARGE){id=1;}else
				if(type == FireworkEffect.Type.STAR){id=2;}else
				if(type == FireworkEffect.Type.CREEPER){id=3;}else
				if(type == FireworkEffect.Type.BURST){id=4;}

				explosions.add("Type:"+id);
				List<String> colors = new ArrayList<String>();
				for(Color color : fe.getColors()){
					colors.add(String.valueOf(color.asRGB()));
				}
				explosions.add("Colors:["+Utility.listJoin(colors,",")+"]}");
			}
			fireworks.add("Explosions:["+Utility.listJoin(explosions,",")+"]");


			if(fireworks.size() != 0)list.add("Fireworks:{"+Utility.listJoin(fireworks,",")+"}");
		}
		String out = Utility.listJoin(list,",");

		return (out.length() != 0 ? "{"+out+"}":null);
	}
}
