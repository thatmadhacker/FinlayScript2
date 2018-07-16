package com.thatmadhacker.packagemanager;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import sun.misc.BASE64Decoder;

public class PackageManager {
	public static final File REPOS = new File(System.getProperty("user.home") + "/fs.repos");
	public static final File ADDONS = new File(System.getProperty("user.home") + "/fsaddons/");

	public static void main(String[] args) {
		try {
			if (!REPOS.exists() || REPOS.isDirectory()) {
				REPOS.delete();
				REPOS.createNewFile();
			}
			if (!ADDONS.exists() || !ADDONS.isDirectory()) {
				ADDONS.delete();
				ADDONS.mkdirs();
			}
			List<String> repos = new ArrayList<String>();
			Scanner in = new Scanner(REPOS);
			while(in.hasNextLine()){
				repos.add(in.nextLine());
			}
			in.close();
			if(args[0].equalsIgnoreCase("addrepo")){
				repos.add(args[1]);
				REPOS.delete();
				REPOS.createNewFile();
				PrintWriter out = new PrintWriter(new FileWriter(REPOS));
				for(String s : repos){
					out.println(s);
				}
				out.close();
				System.out.println("Added "+args[1]+" to the repos list!");
			}else if(args[0].equalsIgnoreCase("removerepo")){
				for(String s : repos){
					if(s.equalsIgnoreCase(args[1])){
						repos.remove(s);
					}
				}
				REPOS.delete();
				REPOS.createNewFile();
				PrintWriter out = new PrintWriter(new FileWriter(REPOS));
				for(String s : repos){
					out.println(s);
				}
				out.close();
				System.out.println("Removed "+args[1]+" to the repos list!");
			}else if(args[0].equalsIgnoreCase("removeaddon")){
				try{
					delete(new File(ADDONS,args[1]));
				}catch(Exception e){
					System.out.println(args[1]+" is not installed!");
					return;
				}
				System.out.println("Uninstalled "+args[1]+"!");
			}else if(args[0].equalsIgnoreCase("addaddon")){
				List<String> repoWith = new ArrayList<String>();
				for(String repo : repos){
					if(!repo.equals("")){
						try{
							System.out.println("Attempting to connect to "+repo);
							Socket s = new Socket(repo,1995);
							PrintWriter out = new PrintWriter(s.getOutputStream(),true);
							in = new Scanner(s.getInputStream());
							
							out.println("?CONTAINS "+args[1]);
							if(Boolean.valueOf(in.nextLine())){
								repoWith.add(repo);
							}
							in.close();
							out.close();
							s.close();
						}catch(Exception e){
							System.out.println(repo+" is unreachable!");
							continue;
						}
					}
				}
				List<String> packages = new ArrayList<String>();
				Map<String,String> repoMap = new HashMap<String,String>(); 
				for(String str : repoWith){
					try{
						Socket s = new Socket(str,1995);
						PrintWriter out = new PrintWriter(s.getOutputStream(),true);
						in = new Scanner(s.getInputStream());
						
						out.println("{LISTADDON "+args[1]);
						boolean b = true;
						while(b){
							String s1 = in.nextLine();
							if(s1.equals("{END")){
								b = false;
							}else{
								packages.add(s1);
								repoMap.put(str, s1);
								System.out.println(packages.size()+": "+str+" has "+s1);
							}
						}
						
						in.close();
						out.close();
						s.close();
					}catch(Exception e){
						System.out.println(str+" is unreachable!");
						continue;
					}
				}
				System.out.println("Enter the id of the package you wish to install!");
				Scanner scan = new Scanner(System.in);
				int id = Integer.valueOf(in.nextLine());
				scan.close();
				Socket s = new Socket(repoMap.get(packages.get(id)),1995);
				PrintWriter out = new PrintWriter(s.getOutputStream(),true);
				in = new Scanner(s.getInputStream());
				
				out.println("{DOWNLOAD "+packages.get(id));
				
				byte[] bytes = new BASE64Decoder().decodeBuffer(in.nextLine());
				Files.write(new File(ADDONS,args[1]+"/main.jar").toPath(), bytes, StandardOpenOption.CREATE_NEW);
				ProcessBuilder builder = new ProcessBuilder("java -jar "+new File(ADDONS,args[1]+"/main.jar").getAbsolutePath());
				builder.start();
				in.close();
				out.close();
				s.close();
			}else{
				System.out.println("addrepo <repo>");
				System.out.println("removerepo <repo>");
				System.out.println("addaddon <addon>");
				System.out.println("removeaddon <addon>");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static void delete(File f){
		for(File f1 : f.listFiles()){
			if(f1.isDirectory()){
				delete(f1);
			}else{
				f1.delete();
			}
		}
		f.delete();
	}
}
