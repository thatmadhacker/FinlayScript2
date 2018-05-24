package com.thatmadhacker.finlayscript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Program {
	public Map<String,String> variables;
	public Map<String, VariableType> types;
	public Map<String,Program> classes;
	public Map<String,Integer> methods;
	public Map<String,VariableType> methodTypes;
	public List<Permission> permissions;
	public File topDir;
	public List<String> lines;
	public int exitCode = 0;
	public String returnValue = "";
	public Program(){
		lines = new ArrayList<String>();
		permissions = new ArrayList<Permission>();
		variables = new HashMap<String,String>();
		types = new HashMap<String,VariableType>();
		classes = new HashMap<String,Program>();
		methods = new HashMap<String,Integer>();
		methodTypes = new HashMap<String,VariableType>();
	}
	
	public void exec(){
		int exitCode = -1;
		if (methods.containsKey("main")) {
			for (int i = methods.get("main"); i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.startsWith("}")) {
					i = lines.size() + 1;
					continue;
				}
				try {
					int code = FinlayScript.decodeLine(line, this, topDir, i);
					if (code != -1) {
						code = exitCode;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (exitCode != -1) {
			this.exitCode = exitCode;
		}
	}
	public void execMethod(String method){
		int exitCode = -1;
		if (methods.containsKey(method)) {
			for (int i = methods.get(method); i < lines.size(); i++) {
				String line = lines.get(i);
				if (line.startsWith("}")) {
					i = lines.size() + 1;
					continue;
				}
				try {
					int code = FinlayScript.decodeLine(line, this, topDir, i);
					if (code != -1) {
						code = exitCode;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (exitCode != -1) {
			this.exitCode = exitCode;
		}
	}
}
