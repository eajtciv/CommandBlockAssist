package com.github.eajtciv.commandblockassist.utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * メタデータ等を扱います。
 * @author eajtciv
 */
public class Utility {

	private Utility(){}

	/**
	 * エンティティからメタデータ取得し型を変えて返します。
	 * @param plugin JavaPlugin
	 * @param entity Entity
	 * @param name メタデータ名
	 * @param source 型
	 * @return sourceで指定された物にキャストします。<br>失敗した場合はnullを返します。
	 */
	public static <T> T getMetadata(JavaPlugin plugin, Entity entity, String name, Class<T> source){
		Object obj = getMetadata(plugin, entity, name);
		if(source.isInstance(obj)){
			return source.cast(obj);
		}
		return null;
	}



	/**
	 * エンティティからメタデータの取得
	 * @param plugin JavaPlugin
	 * @param entity Entity
	 * @param name メタデータ名
	 * @return 成功した場合を除いてnullを返します。
	 */
	public static Object getMetadata(JavaPlugin plugin, Entity entity, String name){
		for(MetadataValue metadata : entity.getMetadata(name)){
			if(metadata.getOwningPlugin().equals(plugin))
				return metadata.value();
		}
		return null;
	}



	/**
	 * エンティティにメタデータを設定します。
	 * @param plugin JavaPlugin
	 * @param entity Entity
	 * @param obj 収納する内容
	 * @param name 名前
	 */
	public static void setMetadata(JavaPlugin plugin, Entity entity, String name, Object obj){
		FixedMetadataValue value = new FixedMetadataValue(plugin, obj);
		entity.setMetadata(name, value);
	}



	/**
	 * エンティティからメタデータ削除
	 * @param plugin JavaPlugin
	 * @param entity Entity
	 * @param names 名前
	 * @return メタデータが見つかり削除処理をしていたらtrue
	 */
	public static boolean removeMetadata(JavaPlugin plugin, Entity entity, String... names){
		boolean result = false;
		for(String name : names){
			if(hasMetadata(plugin, entity, name)){
				entity.removeMetadata(name, plugin);
				result = true;
			}
		}
		return result;
	}



	/**
	 * エンティティに指定したメタデータが有るか確認
	 * @param plugin JavaPlugin
	 * @param entity Entity
	 * @param name 名前
	 * @return 見つかった場合<b>true</b>
	 */
	public static boolean hasMetadata(JavaPlugin plugin, Entity entity, String name) {
		if (entity.hasMetadata(name)) {
			for (MetadataValue metadata : entity.getMetadata(name)) {
				if (metadata.getOwningPlugin().equals(plugin))
					return true;
			}
		}
		return false;
	}


	/**
	 * リストを指定された文字で文字列に連結して出力
	 * @param list 文字列につなぐリスト
	 * @param connect リストを接続する文字
	 * @return String
	 */
	public static String listJoin(List<?> list, String connect) {
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			Object line = list.get(i);
			if (line != null && !line.equals("")) {
				if ((i == 0 || connect == null) == false) {
					out.append(connect);
				}
				out.append(line.toString());
			}
		}
		return out.toString();
	}



	/**
	 * BlockFaceから設置される位置を取得
	 * @param block 基準になるブロック
	 * @param face BlockFace
	 * @return Block
	 */
	public static Block getThePutBlock(Block block, BlockFace face) {
		return block.getLocation().add(face.getModX(), face.getModY(), face.getModZ()).getBlock();
	}


	public static byte[] getInputStreamByte(InputStream is) throws IOException {
		BufferedInputStream in = new BufferedInputStream(is);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int available;
		while ((available = in.available()) > 0) {
			byte[] bytes = new byte[available];
			in.read(bytes);
			out.write(bytes);
		}
		out.close();
		return out.toByteArray();
	}


	private static void inToOut(InputStream is, OutputStream os) throws IOException {
		BufferedInputStream in = new BufferedInputStream(is);
		BufferedOutputStream out = new BufferedOutputStream(os);
		int available;
		while ((available = in.available()) > 0) {
			byte[] bytes = new byte[available];
			in.read(bytes);
			out.write(bytes);
		}
	}


	/**
	 * テキスト保存
	 * @param file ファイル
	 * @param contents 内容
	 * @param code 文字コード
	 * @throws IOException
	 */
	public static void saveText(File file, String[] contents) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		boolean check = false;
		for(String str : contents){
			if(check){
				bw.write(System.getProperty("line.separator"));
			}else{
				check = true;
			}
			bw.write(str);
		}
		bw.close();
	}


	/**
	 * Zipファイルをフォルダ指定で解凍(処理は適当)
	 * @param file JARファイル
	 * @param jarDirPath JAR内のディレクトリ
	 * @param outPath 出力先
	 * @return エラーが発生した場合false 成功した場合true
	 */
	public static boolean zipDirCopy(File file, String jarDirPath, File outPath) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();

			if(!outPath.exists())
				outPath.mkdirs();

			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String path = entry.getName();

				if(!path.startsWith(jarDirPath) || entry.isDirectory()){
					continue;
				}

				File outFile = new File(outPath, path.substring(jarDirPath.length()+1));
				if(outFile.exists() == false){
					File parent = outFile.getParentFile();
					if(parent != null){
						parent.mkdirs();
					}

					InputStream is = zipFile.getInputStream(entry);
					if(path.endsWith(".txt") || path.endsWith(".yml")){
						String txt = ConfigManager.getText(getInputStreamByte(is));
						saveText(outFile, txt.split("\r\n|[\r\n]"));
					}else{
						FileOutputStream fos = new FileOutputStream(outFile);
						inToOut(is, fos);
						fos.close();
					}
					is.close();
				}
			}
			zipFile.close();
			return true;
		}catch (IOException e) {
			if(zipFile != null){
				try {
					zipFile.close();
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Zipファイル内のファイルをコピー
	 * @param file JARファイル
	 * @param jarFilePath JAR内のファイルパス
	 * @param outPath 出力先
	 * @return エラーが発生した場合<b>false</b> 成功した場合<b>true</b>
	 */
	public static boolean zipFileCopy(File file, String jarFilePath, File outPath) {
		if(!outPath.getName().contains("."))
			outPath = new File(outPath, new File(jarFilePath).getName());

		if(!outPath.getParentFile().isDirectory())
			outPath.getParentFile().mkdirs();

		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(file);
			ZipEntry ze = zipFile.getEntry(jarFilePath);
			if(ze == null){
				zipFile.close();
				return false;
			}

			InputStream is = zipFile.getInputStream(ze);
			FileOutputStream fos = new FileOutputStream(outPath);
			inToOut(is, fos);
			zipFile.close();
			fos.close();
		} catch (IOException e) {
			if(zipFile != null){
				try {
					zipFile.close();
				} catch (IOException e1) {
					// TODO 自動生成された catch ブロック
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Integerを取得
	 * @param num 数字の入っている文字列
	 * @return 変換できなかった場合<b>null</b>
	 */
	public static Integer getInteger(String num){
		try{
			return Integer.parseInt(num);
		}catch(NumberFormatException e){
			return null;
		}
	}

	/**
	 * Materialを取得
	 * @param name Material名
	 * @return Material
	 */
	@SuppressWarnings("deprecation")
	public static Material getMaterial(String name){
		Integer id = getInteger(name);
		if(id != null){
			return Material.getMaterial(id);
		}else{
			return Material.getMaterial(name.toUpperCase(Locale.ENGLISH));
		}
	}
}
