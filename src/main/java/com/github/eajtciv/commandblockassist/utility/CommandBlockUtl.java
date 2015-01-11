package com.github.eajtciv.commandblockassist.utility;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.Player;
/**
 * コマンドブロックの操作に関する物です。
 * @author eajtciv
 */
public class CommandBlockUtl {


	/**
	 * コマンドブロック書き込み
	 * @param block 操作するブロック
	 * @param command 書き込むコマンド
	 * @param player 音を送信するプレイヤー
	 */
	public static void writingCommanBlock(Block block , String command,Player player){
		//コマンドブロックでは、無い場合コマンドブロックにする。
		if(block.getType() != Material.COMMAND){
			if(block.getType() == Material.AIR){
				player.playSound(player.getLocation(), Sound.DIG_STONE , 1F, 1F);
			}
			block.setType(Material.COMMAND);
		}
		//変更し適用する
		CommandBlock cb = (CommandBlock) block.getState();
		cb.setCommand(command);
		cb.update();
	}



	/**
	 * コマンドブロックのコマンド比較
	 * @param block ブロックブロック
	 * @param command 比較対象のコマンド
	 * @return 同じだった場合<b>true</b>
	 */
	public static boolean equalsCommanBlock(Block block , String command){
		String cmd = getCommanBlockCommand(block);
		if(cmd == null) return false;

		return cmd.equals(command);
	}



	/**
	 * コマンドブロックのコマンド取得
	 * @param block 操作するブロック
	 * @return <b>String</b> コマンドブロックではない場合 <b>null</b>
	 */
	public static String getCommanBlockCommand(Block block){
		if(block.getType() != Material.COMMAND) return null;

		CommandBlock cb = (CommandBlock) block.getState();
		return cb.getCommand();
	}

}
