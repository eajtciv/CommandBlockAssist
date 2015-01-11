package com.github.eajtciv.commandblockassist.utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
/**
 * @author eajtciv
 */
public class ConfigManager {

	private File configFile;
	private YamlConfiguration config;

	public ConfigManager(File configFile){
		this.configFile = configFile;
	}

	public FileConfiguration getConfig() {
		if(config == null) reloadConfig();
		return config;
	}

	public void reloadConfig() {
		try {
			config = getYaml(configFile);
		} catch (InvalidConfigurationException | IOException e) {
			e.printStackTrace();
		}
	}

	public void saveConfig() {
		if(config == null || configFile == null)
			return;

		try {
			getConfig().save(configFile);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @param file Byteで取得するファイル
	 * @throws IOException
	 */
	public static byte[] getFileBytes(File file) throws IOException {
		FileInputStream in = new FileInputStream(file);
		byte[] bytes = Utility.getInputStreamByte(in);
		in.close();
		return bytes;
	}

	/**
	 * 文字コード自動判別して取得(日本語系)<br>
	 * 変換可能かチェックしてるだけなので同じ系列の文字コードだと変換可能な物と判定する。<br>
	 * (JIS, SJIS, EUC-JP, UTF-8)ならいけるはず！・・・
	 * @param source テキストのByte
	 */
	public static String getText(byte[] source){
		String[] codes = {"JIS","SJIS","EUC-JP","UTF-8"};
		for(String code : codes){
			Charset charCode = Charset.forName(code);
			String string = new String(source, charCode);
			String string2 = new String(string.getBytes(charCode), charCode);
			if(string.equals(string2)){
				return string;
			}
		}
		return null;
	}



	/**
	 * @param file 読み込むYAMLファイル
	 * @return YamlConfiguration
	 * @throws InvalidConfigurationException
	 * @throws IOException
	 */
	public static YamlConfiguration getYaml(File file) throws InvalidConfigurationException, IOException {
		byte[] fileByte = getFileBytes(file);
		String text = getText(fileByte);
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.loadFromString(text);
		return yaml;
	}

}
