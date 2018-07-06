package com.thatmadhacker.iolib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.thatmadhacker.finlayscript.FinlayScript;
import com.thatmadhacker.finlayscript.Library;
import com.thatmadhacker.finlayscript.Program;

public class IOLib extends Library {

	Scanner in = new Scanner(System.in);
	@Override
	public boolean onMethod(String name, String line, Program p, String[] args) {
		if (name.equalsIgnoreCase("write")) {
			if (p.hasPermission("IOWRITE") || p.hasPermission("IO*")) {
				line = line.substring(line.indexOf(name+"(")+name.length()+1, line.lastIndexOf(")"));
				String path = FinlayScript.parseString(line.split(",")[0].trim(),p);
				String data = FinlayScript.parseString(line.split(",")[1].trim(),p).replaceAll("\"", "");
				boolean append = Boolean.valueOf(line.split(",")[2]);
				File f = new File(path);
				if (!append) {
					f.delete();
					try {
						f.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				PrintWriter out = null;
				try {
					out = new PrintWriter(new FileWriter(f));
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.print(data);
				out.close();
			}
			return true;
		} else if (name.equalsIgnoreCase("create")) {
			if (p.hasPermission("IOCREATE") || p.hasPermission("IO*")) {
				String file = FinlayScript.parseString(line.substring(line.indexOf(name+"(")+name.length()+1, line.lastIndexOf(")")).trim(),p);
				File f = new File(file);
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return true;
		} else if (name.equalsIgnoreCase("delete")) {
			if (p.hasPermission("IODELETE") || p.hasPermission("IO*")) {
				String file = FinlayScript.parseString(line.substring(line.indexOf(name+"(")+name.length()+1, line.lastIndexOf(")")).trim(),p);
				File f = new File(file);
				f.delete();
			}
			return true;
		} else if (name.equalsIgnoreCase("mkdir")) {
			if (p.hasPermission("IOMKDIR") || p.hasPermission("IO*")) {
				String file = FinlayScript.parseString(line.substring(line.indexOf(name+"(")+name.length()+1, line.lastIndexOf(")")).trim(),p);
				File f = new File(file);
				f.mkdir();
			}
			return true;
		} else if (name.equalsIgnoreCase("mkdirs")) {
			if (p.hasPermission("IOMKDIR") || p.hasPermission("IO*")) {
				String file = FinlayScript.parseString(line.substring(line.indexOf(name+"(")+name.length()+1, line.lastIndexOf(")")).trim(),p);
				File f = new File(file);
				f.mkdirs();
			}
			return true;
		} else if (name.equalsIgnoreCase("read")) {
			if (p.hasPermission("IOREAD") || p.hasPermission("IO*")) {
				String file = FinlayScript.parseString(line.substring(line.indexOf(name+"(")+name.length()+1, line.lastIndexOf(")")).trim(),p);
				File f = new File(file);
				try {
					Scanner in = new Scanner(f);
					List<String> lines = new ArrayList<String>();
					while(in.hasNextLine()){
						lines.add(in.nextLine());
					}
					in.close();
					p.lists.remove("read");
					p.listTypes.remove("read");
					p.lists.put("read", lines);
					p.listTypes.put("read", "String");
					p.returnValue = combine(lines, "");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
			return true;
		}else if (name.equalsIgnoreCase("println")) {
			String string = "";
			for(String s : args){
				string = string + ","+s;
			}
			string = string.substring(1);
			string = FinlayScript.parseString(string, p);
			System.out.println(string);
			return true;
		}else if(name.equalsIgnoreCase("readConsole")){
			p.returnValue = in.nextLine();
			return true;
		}
		return false;
	}

	@Override
	public boolean onLine(String line, Program p) {
		return false;
	}

	@Override
	public void init(Program p) {
		p.env.methods.put("write", this);
		p.env.methods.put("create", this);
		p.env.methods.put("delete", this);
		p.env.methods.put("mkdir", this);
		p.env.methods.put("mkdirs", this);
		p.env.methods.put("read", this);
		p.env.methods.put("println", this);
		p.env.methods.put("readConsole", this);
	}
	public static String combine(List<String> s,String seperator){
		String string = "";
		for(String str : s){
			string += str+seperator;
		}
		string = string.substring(0, string.length()-seperator.length());
		return string;
	}

	@Override
	public String getName() {
		return "iolib";
	}

}
