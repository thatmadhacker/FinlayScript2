package com.thatmadhacker.finlayscript;

import java.util.List;

public class TextUtils {
	
	public static String join(String[] strings, String joiner){
		String s = "";
		for(String string : strings){
			s += string + joiner;
		}
		s = s.substring(0, s.length()-joiner.length());
		return s;
	}
	public static String join(List<String> strings, String joiner){
		String s = "";
		for(String string : strings){
			s += string + joiner;
		}
		s = s.substring(0, s.length()-joiner.length());
		return s;
	}
}
