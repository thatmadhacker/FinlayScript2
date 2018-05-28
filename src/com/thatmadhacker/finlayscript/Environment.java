package com.thatmadhacker.finlayscript;

import java.util.HashMap;
import java.util.Map;

public class Environment {
	public Map<String,String> variables;
	public Map<String,String> types;
	public Map<String,Library> methods;
	public Environment() {
		this.variables = new HashMap<String,String>();
		this.types = new HashMap<String,String>();
		this.methods = new HashMap<String,Library>();
		for(Object o : System.getProperties().keySet()){
			if(o instanceof String){
				String s = (String) o;
				variables.put(s, System.getProperties().getProperty(s));
				types.put(s, "STRING");
			}
			
		}
	}
	
}
