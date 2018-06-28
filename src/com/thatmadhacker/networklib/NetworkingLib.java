package com.thatmadhacker.networklib;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.thatmadhacker.finlayscript.Library;
import com.thatmadhacker.finlayscript.Program;

public class NetworkingLib implements Library {

	Map<String,Socket> sockets = new HashMap<String,Socket>();
	Map<String,PrintWriter> outs = new HashMap<String,PrintWriter>();
	Map<String,Scanner> ins = new HashMap<String,Scanner>();
	@Override
	public boolean onMethod(String name, String line, Program p, String[] args) {
		if(name.equalsIgnoreCase("openSocket")){
			try{
				Socket s = new Socket(args[0],Integer.valueOf(args[1]));
				sockets.put(args[2], s);
			}catch(Exception e){
				e.printStackTrace();
			}
			return true;
		}else if(name.equalsIgnoreCase("closeSocket")){
			try {
				sockets.get(args[0]).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}else if(name.equalsIgnoreCase("sPrint")){
			outs.get(args[0]).print(args[1]);
			return true;
		}else if(name.equalsIgnoreCase("sRead")){
			p.returnValue = ins.get(args[0]).nextLine();
			return true;
		}else if(name.equalsIgnoreCase("sPrintln")){
			outs.get(args[0]).println(args[1]);
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
		p.env.methods.put("openSocket", this);
		p.env.methods.put("closeSocket", this);
		p.env.methods.put("sPrint", this);
		p.env.methods.put("sPrintln", this);
		p.env.methods.put("sRead", this);
	}

}
