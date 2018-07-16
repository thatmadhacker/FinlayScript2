package com.thatmadhacker.packagemanager;

import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sun.misc.BASE64Encoder;

public class Server implements Runnable {

	public static final File ADDONS = new File("addons/");
	static List<String> packages = new ArrayList<String>();
	static Map<String, String> packageNames = new HashMap<String, String>();
	public static void main(String[] args) {
		try {
			if (!ADDONS.exists() || !ADDONS.isDirectory()) {
				ADDONS.delete();
				ADDONS.mkdirs();
			}
			for (File f : ADDONS.listFiles()) {
				try {
					packages.add(f.getName());
					File f1 = new File(f, "packagename.txt");
					Scanner s = new Scanner(f1);
					packageNames.put(f.getName(), s.nextLine());
					s.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			ServerSocket ss = new ServerSocket(1995);

			while (!ss.isClosed()) {

				Socket s = ss.accept();
				new Server(s);

			}

			ss.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Socket s;

	public Server(Socket s) {
		this.s = s;
		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			Scanner in = new Scanner(s.getInputStream());
			PrintWriter out = new PrintWriter(s.getOutputStream(), true);
			
			String message = in.nextLine();
			if(message.startsWith("?CONTAINS")){
				if(packages.contains(message.substring(10))){
					out.println("TRUE");
				}else{
					out.println("FALSE");
				}
			}else if(message.startsWith("{LISTADDON")){
				for(int i = 0; i < packageNames.values().size();i++){
					String s = (String) packageNames.values().toArray()[i];
					if(s.equals(message.substring(11))){
						out.println((String) packageNames.keySet().toArray()[i]);
					}
				}
				out.println("{END");
			}else if(message.startsWith("{DOWNLOAD")){
				String packageName = message.substring(10);
				File file = new File(ADDONS,packageNames.get(packageName)+"/main.jar");
				byte[] bytes = Files.readAllBytes(file.toPath());
				out.println(new BASE64Encoder().encode(bytes));
			}
			
			in.close();
			out.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
