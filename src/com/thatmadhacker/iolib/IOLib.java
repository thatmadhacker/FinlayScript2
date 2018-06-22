package com.thatmadhacker.iolib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.thatmadhacker.finlayscript.Library;
import com.thatmadhacker.finlayscript.Program;

public class IOLib implements Library {

	@Override
	public boolean onMethod(String name, String line, Program p) {
		if (line.startsWith("write")) {
			if (p.hasPermission("IOWRITE") || p.hasPermission("IO*")) {
				line = line.substring(6, line.lastIndexOf(")"));
				String path = line.split(",")[0].replaceAll("\"", "").trim();
				String data = line.split(",")[1].replaceAll("\"", "").trim();
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
		} else if (line.startsWith("create")) {
			if (p.hasPermission("IOCREATE") || p.hasPermission("IO*")) {
				String file = line.substring(7, line.lastIndexOf(")")).replaceAll("\"", "").trim();
				File f = new File(file);
				try {
					f.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return true;
		} else if (line.startsWith("delete")) {
			if (p.hasPermission("IODELETE") || p.hasPermission("IO*")) {
				String file = line.substring(7, line.lastIndexOf(")")).replaceAll("\"", "").trim();
				File f = new File(file);
				f.delete();
			}
			return true;
		} else if (line.startsWith("mkdir")) {
			if (p.hasPermission("IOMKDIR") || p.hasPermission("IO*")) {
				String file = line.substring(6, line.lastIndexOf(")")).replaceAll("\"", "").trim();
				File f = new File(file);
				f.mkdir();
			}
			return true;
		} else if (line.startsWith("mkdirs")) {
			if (p.hasPermission("IOMKDIR") || p.hasPermission("IO*")) {
				String file = line.substring(6, line.lastIndexOf(")")).replaceAll("\"", "").trim();
				File f = new File(file);
				f.mkdirs();
			}
			return true;
		} else if (line.startsWith("read")) {
			if (p.hasPermission("IOREAD") || p.hasPermission("IO*")) {
				String file = line.substring(5, line.lastIndexOf(")")).replaceAll("\"", "").trim();
				File f = new File(file);
				try {
					Scanner in = new Scanner(f);
					List<String> lines = new ArrayList<String>();
					while(in.hasNextLine()){
						lines.add(in.nextLine());
					}
					in.close();
					p.lists.put("read", lines);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
			}
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
	}

}
