package com.thatmadhacker.finlayscript;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Program {
	public Map<String,String> variables;
	public Map<String, VariableType> types;
	public Map<String,Program> classes;
	public Map<String,Integer> methods;
	public Map<String,VariableType> methodTypes;
	public List<String> lines;
	public int exitCode = 0;
	public String returnValue = "";
	public Program(List<String> lines){
		this.lines = lines;
		variables = new HashMap<String,String>();
		types = new HashMap<String,VariableType>();
		classes = new HashMap<String,Program>();
		methods = new HashMap<String,Integer>();
		methodTypes = new HashMap<String,VariableType>();
	}
	
}
