package com.github.eajtciv.commandblockassist.utility;

import java.util.ArrayList;
import java.util.List;

public class DataTagItem {

	public enum Type {SquareBracket, CurlyBrace};
	private Type type;
	private List<String> list = new ArrayList<String>();

	public DataTagItem(){
		this(Type.CurlyBrace);
	}

	public DataTagItem(Type type){
		this.type = type;
	}

	public void add(String value){
		if(value != null && value.length() != 0){
			list.add(value);
		}
	}

	public void add(int value){
		add(String.valueOf(value));
	}

	public void add(DataTagItem value){
		add(value.toString());
	}

	public void add(String key, Object value) {
		if(value != null && value.toString().length() != 0){
			list.add(key + ":" + value.toString());
		}

	}

	public void add(String key, int value) {
		add(key, (Integer)value);
	}


	private String[] getBrackets() {
		switch(type){
		case CurlyBrace:
			return new String[]{"{","}"};
		case SquareBracket:
			return new String[]{"[","]"};
		default:
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(getBrackets()[0]);
		String con = Utility.listJoin(list, ",");
		if(con.length() == 0){
			return "";
		}
		str.append(con);
		str.append(getBrackets()[1]);
		return str.toString();
	}


}
