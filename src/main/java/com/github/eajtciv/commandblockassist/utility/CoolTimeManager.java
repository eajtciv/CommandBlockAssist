package com.github.eajtciv.commandblockassist.utility;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * クールタイムを扱います。
 * @author eajtciv
 */
public class CoolTimeManager {

	private Map<Object, Long> map = new HashMap<Object, Long>();
	private int millisecond;



	/**
	 * @param millisecond ミリ秒
	 */
	public CoolTimeManager(int millisecond){
		this.millisecond = millisecond;
	}



	/**
	 * @param seconds 秒
	 */
	public CoolTimeManager(double seconds){
		this.millisecond = (int) (seconds * 1000);
	}



	/**
	 * オブジェクトにクールタイムを設定する。
	 * @param obj オブジェクト
	 */
	public void setCoolTime(Object obj){
		map.put(obj, System.currentTimeMillis());
	}



	/**
	 * 残りクールタイムを取得
	 * @param obj オブジェクト
	 * @return 秒
	 */
	public Double getCoolTime(Object obj){
		Long oldTime = map.get(obj);
		if(oldTime != null){
			double coolTime = (millisecond - (System.currentTimeMillis()-oldTime)) / 1000.0;
			if(0 < coolTime){
				return coolTime;
			}
		}
		return 0.0;
	}



	/**
	 * クールタイムが有るか返す
	 * @param name オブジェクト
	 * @return true or false
	 */
	public boolean hasCoolTime(Object name){
		Double coolTime = getCoolTime(name);
		return coolTime > 0;
	}



	/**
	 * リフレッシュ！
	 */
	public void refresh(){
		Set<Object> temp = new HashSet<Object>(map.keySet());
		for(Object key : temp){
			if(hasCoolTime(key) == false){
				map.remove(key);
			}
		}
	}



}
