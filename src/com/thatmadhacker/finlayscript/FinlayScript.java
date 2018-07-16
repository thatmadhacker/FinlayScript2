package com.thatmadhacker.finlayscript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.thatmadhacker.libs.cryptolib.CryptoLib;
import com.thatmadhacker.libs.iolib.IOLib;
import com.thatmadhacker.libs.math.MathLib;
import com.thatmadhacker.libs.networklib.NetworkingLib;

public class FinlayScript {
	public static void interpretASync(Program p, File f, File topDir) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					interpret(p, f, topDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@SuppressWarnings({ "resource" })
	public static void interpret(Program p, File f, File topDir) throws Exception {
		Scanner scan = new Scanner(System.in);
		Scanner in = new Scanner(f);
		List<String> lines = new ArrayList<String>();
		while (in.hasNextLine()) {
			lines.add(in.nextLine());
		}
		in.close();
		p.lines = lines;
		p.topDir = topDir;
		for (int i = 0; i < lines.size(); i++) {
			String s = lines.get(i);
			if (s.startsWith("String()")) {
				s = s.substring(8).trim();
				p.methods.put(s.substring(0, s.lastIndexOf("{")).trim(), i);
				p.methodTypes.put(s.substring(0, s.lastIndexOf("{")).trim(), "STRING");
			} else if (s.startsWith("int()")) {
				s = s.substring(5).trim();
				p.methods.put(s.substring(0, s.lastIndexOf("{")).trim(), i);
				p.methodTypes.put(s.substring(0, s.lastIndexOf("{")).trim(), "INTEGER");
			} else if (s.startsWith("void()")) {
				s = s.substring(6).trim();
				p.methods.put(s.substring(0, s.lastIndexOf("{")).trim(), i);
				p.methodTypes.put(s.substring(0, s.lastIndexOf("{")).trim(), "VOID");
			} else if (s.startsWith("boolean()")) {
				s = s.substring(9).trim();
				p.methods.put(s.substring(0, s.lastIndexOf("{")).trim(), i);
				p.methodTypes.put(s.substring(0, s.lastIndexOf("{")).trim(), "BOOLEAN");
			} else if (s.startsWith("<import>")) {
				s = s.substring(8);
				String name = s.substring(2, s.lastIndexOf("\""));
				s = s.substring(name.length() + 2);
				s = s.substring(5);
				String as = s;
				File file = new File(topDir.getPath() + "/" + name);
				Program p1 = new Program(p.env);
				interpret(p1, file, topDir);
				p.classes.put(as, p1);
			} else if (s.startsWith("<permission>")) {
				s = s.substring(12);
				s = s.trim();
				String permission = s.trim().toUpperCase();
				System.out.println("A script is requesting " + permission.toString() + " permissions, accept? Y/N");
				if (scan.nextLine().equalsIgnoreCase("y")) {
					p.permissions.add(permission);
				} else {
					continue;
				}
			} else if (s.startsWith("<lib>")){
				s = s.substring(5);
				s = s.trim();
				String lib = s.substring(1, s.length()-1).toLowerCase();
				for(Library l : p.libraries){
					if(l.getName().equalsIgnoreCase(lib) && l.isInit() == false){
						l.init(p);
						l.setInit(true);
					}
				}
			}
		}
	}

	public static int decodeLine(String s, Program p, File topDir, int i) throws Exception {
		s = s.trim();
		String orig = s;
		p.returnValue = "";
		if (s.startsWith("//") || s.startsWith(";")) {
			return -1;
		}
		if (s.startsWith("}")) {
			return -1;
		}
		if (s.startsWith("boolean()") || s.startsWith("String()") || s.startsWith("int()") || s.startsWith("void()")) {
			return -1;
		}
		boolean found = true;
		for (Library l : p.libraries) {
			if (l.onLine(orig, p)) {
				return -1;
			}
		}
		for (String strinn : p.env.methods.keySet()) {
			if (s.startsWith(strinn + "(")) {
				String[] args = s.substring(s.indexOf("(")+1, s.lastIndexOf(")")).split(",");
				if (p.env.methods.get(strinn).onMethod(strinn, orig, p,args)) {
					found = false;
					return -1;
				}
			}
		}
		for (String l : p.lists.keySet()) {
			if (s.startsWith(l + ".add(")) {
				String data = s.substring((l + ".add(").length(), s.lastIndexOf(")"));
				p.lists.get(l).add(data);
				found = true;
				return -1;
			}
			if (s.startsWith(l + ".remove(")) {
				String data = s.substring((l + ".remove(").length(), s.lastIndexOf(")"));
				p.lists.get(l).remove(data);
				found = true;
				return -1;
			}
		}
		if (s.startsWith("list<")) {
			String type = s.substring(5, s.lastIndexOf(">"));
			String name = s.substring(s.lastIndexOf(">") + 2);
			p.lists.put(name, new ArrayList<String>());
			p.listTypes.put(name, type);
			found = true;
			return -1;
		}
		if (s.startsWith("for(")) {
			String[] parse = s.substring(4, s.lastIndexOf(")")).split(":");
			String type = parse[0].split(" ")[0];
			String name = parse[0].split(" ")[1];
			String list = parse[1];
			String method = parse[2];
			List<String> l = p.lists.get(list.trim());
			for (String s1 : l) {
				p.variables.put(name, s1);
				p.types.put(name, type);
				p.execMethod(method.trim());
			}
			p.variables.remove(name);
			p.types.remove(name);
			found = true;
			return -1;
		}
		
		if (s.startsWith("if(")) {
			found = false;
			String[] parse = s.substring(3).trim().split(",");
			if (parse.length == 2) {
				String variable = parse[0].trim();
				String method = parse[1].trim().substring(0, parse[1].lastIndexOf("("));
				if (p.variables.get(variable).equalsIgnoreCase("TRUE")) {
					p.execMethod(method);
				}
				found = false;
				return -1;
			} else {
				String value1 = parse[0].trim();
				String value2 = parse[1].trim();
				boolean b = false;
				boolean var1 = false;
				boolean var2 = false;
				for (String var : p.variables.keySet()) {
					if (var.equals(value1) && !var1) {
						value1 = p.variables.get(var);
						b = true;
						var1 = true;
					}
					if (var.equals(value2) && !var2) {
						value2 = p.variables.get(var);
						b = true;
						var2 = true;
					}
				}
				if (!b) {
					for (String classs : p.classes.keySet()) {
						if (value1.startsWith(classs + ".")) {
							for (String var : p.classes.get(classs).variables.keySet()) {
								if (value1.equals(classs + "." + var)) {
									value1 = p.classes.get(classs).variables.get(var);
								}
							}
						}
						if (value2.startsWith(classs + ".")) {
							for (String var : p.classes.get(classs).variables.keySet()) {
								if (value2.equals(classs + "." + var)) {
									value2 = p.classes.get(classs).variables.get(var);
								}
							}
						}
					}
				}
				String method = parse[2].substring(0, parse[2].lastIndexOf("("));
				if (value1.equals(value2)) {
					p.execMethod(method);
				}
				found = false;
				return -1;
			}
		}
		if (s.startsWith("while(")) {
			found = false;
			String[] parse = s.substring(6).trim().split(",");
			if (parse.length == 2) {
				String variable = parse[0].trim();
				String method = parse[1].trim().substring(0, parse[1].lastIndexOf("("));

				while (p.variables.get(variable).equalsIgnoreCase("TRUE")) {
					p.execMethod(method);
				}
				found = false;
				return -1;
			} else {
				String value1 = parse[0].trim();
				String value2 = parse[1].trim();
				boolean b = false;
				boolean var1 = false;
				boolean var2 = false;
				for (String var : p.variables.keySet()) {
					if (var.equals(value1) && !var1) {
						value1 = p.variables.get(var);
						b = true;
						var1 = true;
					}
					if (var.equals(value2) && !var2) {
						value2 = p.variables.get(var);
						b = true;
						var2 = true;
					}
				}
				if (!b) {
					for (String classs : p.classes.keySet()) {
						if (value1.startsWith(classs + ".")) {
							for (String var : p.classes.get(classs).variables.keySet()) {
								if (value1.equals(classs + "." + var)) {
									value1 = p.classes.get(classs).variables.get(var);
								}
							}
						}
						if (value2.startsWith(classs + ".")) {
							for (String var : p.classes.get(classs).variables.keySet()) {
								if (value2.equals(classs + "." + var)) {
									value2 = p.classes.get(classs).variables.get(var);
								}
							}
						}
					}
				}
				String method = parse[2].substring(0, parse[2].lastIndexOf("("));
				while (value1.equals(value2)) {
					p.execMethod(method);
					value1 = parse[0].trim();
					value2 = parse[1].trim();
					boolean b1 = false;
					boolean var11 = false;
					boolean var21 = false;
					for (String var : p.variables.keySet()) {
						if (var.equals(value1) && !var11) {
							value1 = p.variables.get(var);
							b1 = true;
							var11 = true;
						}
						if (var.equals(value2) && !var21) {
							value2 = p.variables.get(var);
							b1 = true;
							var21 = true;
						}
					}
					if (!b1) {
						for (String classs : p.classes.keySet()) {
							if (value1.startsWith(classs + ".")) {
								for (String var : p.classes.get(classs).variables.keySet()) {
									if (value1.equals(classs + "." + var)) {
										value1 = p.classes.get(classs).variables.get(var);
									}
								}
							}
							if (value2.startsWith(classs + ".")) {
								for (String var : p.classes.get(classs).variables.keySet()) {
									if (value2.equals(classs + "." + var)) {
										value2 = p.classes.get(classs).variables.get(var);
									}
								}
							}
						}
					}
				}
				found = false;
				return -1;
			}
		}

		for (String str : p.variables.keySet()) {
			if (s.split(" ")[0].equals(str)) {
				if (p.types.get(str) == "STRING") {
					String data = s.substring(str.length()).split("=")[1] + " ";
					String string = parseString(data, p);
					p.variables.put(str, string);
					return -1;
				} else if (p.types.get(str) == "BOOLEAN") {
					String stri = s.substring(str.length()).split("=")[1] + " ";
					boolean found1 = false;
					for (String strin : p.classes.keySet()) {
						if (stri.startsWith(strin + ".")) {
							for (String strinn : p.classes.get(strin).methods.keySet()) {
								if (stri.substring(strin.length() + 1).equals(strinn + "()")) {
									if (!found1) {
										boolean b = false;
										for (int i1 = p.classes.get(strin).methods.get(strinn); i1 < p.lines
												.size(); i1++) {
											String line = p.classes.get(strin).lines.get(i1);
											if (line.startsWith("}")) {
												i1 = p.classes.get(strin).lines.size() + 1;
												continue;
											}
											if (!b) {
												decodeLine(line, p.classes.get(strin), topDir, i1);
												if (!p.classes.get(strin).returnValue.equals("")) {
													String returnValue = p.classes.get(strin).returnValue;
													stri = returnValue;
													p.classes.get(strin).returnValue = "";
													b = true;
													found1 = true;
													found = false;
												}
											}
										}
									}
								}
							}
						}
					}
					for (String strinn : p.methods.keySet()) {
						if (stri.equals(strinn + "()")) {
							if (!found1) {
								boolean b = false;
								for (int i1 = p.methods.get(strinn); i1 < p.lines.size(); i1++) {
									String line = p.lines.get(i1);
									if (line.startsWith("}")) {
										i1 = p.lines.size() + 1;
										continue;
									}
									if (!b) {
										decodeLine(line, p, topDir, i1);
										if (!p.returnValue.equals("")) {
											String returnValue = p.returnValue;
											stri = returnValue;
											p.returnValue = "";
											b = true;
											found1 = true;
											found = false;
										}
									}
								}
							}
						}
					}
					for (String strinn : p.env.methods.keySet()) {
						if (stri.equals(strinn + "(")) {
							String[] args = stri.substring(stri.indexOf("("), stri.lastIndexOf(")")).split(",");
							if (p.env.methods.get(strinn).onMethod(strinn, orig, p,args)) {
								found = false;
								stri = p.returnValue;
							}
						}
					}
					for (String strin : p.variables.keySet()) {
						if (strin.equals(stri)) {
							if (!found) {
								stri = p.variables.get(strin);
								found = false;
							}
						}
					}
					for (String strin : p.env.variables.keySet()) {
						if (strin.equals(stri)) {
							if (!found) {
								stri = p.env.variables.get(strin);
								found = false;
							}
						}
					}
					p.variables.put(str, stri);
					return -1;
				} else if (p.types.get(str) == "INTEGER") {
					String data = s.substring(str.length()).split("=")[1] + " ";
					int value = parseEquasion(data.trim(), p);
					p.variables.put(str, String.valueOf(value));
					return -1;
				}
			}
		}
		for (String string : p.classes.keySet()) {
			if (s.trim().startsWith(string.trim() + ".")) {
				s = s.substring(string.length() + 1);
				found = false;
				for (int i1 = p.classes.get(string).methods
						.get(s.substring(0, s.lastIndexOf("("))); i1 < p.classes.get(string).lines.size(); i1++) {
					String line = p.classes.get(string).lines.get(i1);
					if (line.startsWith("}")) {
						i1 = p.classes.get(string).lines.size() + 1;
						found = false;
					}
					int code = decodeLine(line, p.classes.get(string), topDir, i1);
					if (!(code == -1)) {
						return code;
					}
				}
			}
		}
		if (s.startsWith("exit(")) {
			int code = Integer.valueOf(s.substring(5, s.lastIndexOf(")")).trim());
			return code;
		}
		if (s.startsWith("return(")) {
			s = s.substring(7, s.lastIndexOf(")"));
			String re = "";
			for (String ret : s.split("\\+")) {
				ret = ret.trim();
				boolean found1 = true;
				if(ret.startsWith("\"")){
					found1 = false;
					ret = ret.substring(0, ret.length()-1);
				}
				for (String cla : p.classes.keySet()) {
					if (ret.startsWith(cla + ".")) {
						ret = ret.substring(cla.length() + 1);
						for (String var : p.classes.get(cla).variables.keySet()) {
							if (ret.equals(var) && found1) {
								found1 = false;
								ret = p.classes.get(cla).variables.get(var);
							}
						}
					}
				}
				if (found1) {
					for (String var : p.variables.keySet()) {
						if (var.equals(ret)) {
							ret = p.variables.get(var);
						}
					}
				}
				re += ret;
			}
			String returnValue = re;
			p.returnValue = returnValue;
			return -1;
		}
		if (s.lastIndexOf("(") != -1 && found && !s.contains("=")) {
			boolean b = true;
			int code = -1;
			for (int i1 = p.methods.get(s.substring(0, s.lastIndexOf("("))); i1 < p.lines.size(); i1++) {
				String line = p.lines.get(i1);
				if (line.startsWith("}")) {
					i1 = p.lines.size() + 1;
					continue;
				}
				code = decodeLine(line, p, topDir, i1);
				if (code != -1) {
					p.exitCode = code;
					b = false;
					break;
				}
			}
			if (!b) {
				return code;
			}
		}
		if (s.startsWith("String") && !s.startsWith("String()")) {
			s = s.substring(6);
			String varName = s.split(" ")[1];
			String data = s.substring(varName.length()).split("=")[1] + " ";
			String string = parseString(data, p);
			p.variables.put(varName, string);
			p.types.put(varName, "STRING");
		} else if (s.startsWith("boolean") && !s.startsWith("boolean()")) {
			s = s.substring(7).trim();
			String str = s.split(" ")[0];
			String stri = s.substring(str.length()).split("=")[1] + " ";
			;
			boolean found1 = false;
			for (String strinn : p.env.methods.keySet()) {
				if (stri.startsWith(strinn + "(")) {
					String[] args = stri.substring(stri.indexOf("("), stri.lastIndexOf(")")).split(",");
					if (p.env.methods.get(strinn).onMethod(strinn, orig, p,args)) {
						found1 = true;
						stri = p.returnValue;
					}
				}
			}
			for (String strin : p.classes.keySet()) {
				if (stri.startsWith(strin + ".")) {
					for (String strinn : p.classes.get(strin).methods.keySet()) {
						if (stri.substring(strin.length() + 1).equals(strinn + "()")) {
							if (!found1) {
								boolean b = false;
								for (int i1 = p.classes.get(strin).methods.get(strinn); i1 < p.lines.size(); i1++) {
									String line = p.classes.get(strin).lines.get(i1);
									if (line.startsWith("}")) {
										i1 = p.classes.get(strin).lines.size() + 1;
										continue;
									}
									if (!b) {
										decodeLine(line, p.classes.get(strin), topDir, i1);
										if (!p.classes.get(strin).returnValue.equals("")) {
											String returnValue = p.classes.get(strin).returnValue;
											stri = returnValue;
											p.classes.get(strin).returnValue = "";
											b = true;
											found1 = true;
											found = false;
										}
									}
								}
							}
						}
					}
				}
			}
			for (String strinn : p.methods.keySet()) {
				if (stri.equals(strinn + "()")) {
					if (!found1) {
						boolean b = false;
						for (int i1 = p.methods.get(strinn); i1 < p.lines.size(); i1++) {
							String line = p.lines.get(i1);
							if (line.startsWith("}")) {
								i1 = p.lines.size() + 1;
								continue;
							}
							if (!b) {
								decodeLine(line, p, topDir, i1);
								if (!p.returnValue.equals("")) {
									String returnValue = p.returnValue;
									stri = returnValue;
									p.returnValue = "";
									b = true;
									found1 = true;
									found = false;
								}
							}
						}
					}
				}
			}
			for (String strin : p.variables.keySet()) {
				if (strin.equals(stri)) {
					if (!found) {
						stri = p.variables.get(strin);
						found = false;
					}
				}
			}
			if (!stri.equals("false") && !stri.equals("true")) {
				stri = "false";
			}
			p.variables.put(str, stri);
			p.types.put(str, "BOOLEAN");
		} else if (s.startsWith("int") && !s.startsWith("int()")) {
			s = s.substring(3);
			String varName = s.split(" ")[1];
			String data = s.substring(varName.length()).split("=")[1] + " ";
			int value = parseEquasion(data.trim(), p);
			p.variables.put(varName, String.valueOf(value));
			p.types.put(varName, "INTEGER");
		}
		return -1;
	}

	public static void main(String[] args) throws Exception {
		Program p = new Program();
		p.libraries.add(new IOLib());
		p.libraries.add(new CryptoLib());
		p.libraries.add(new NetworkingLib());
		p.libraries.add(new MathLib());
		interpret(p, new File("scripts/test.fscript"), new File("scripts/"));
		p.exec();
		System.out.println("Exit code: " + p.exitCode);
		System.out.println();
		for (String s : p.variables.keySet()) {
			System.out.println("Name: " + s + " , Type: " + p.types.get(s) + " , Value: " + p.variables.get(s));
		}
		System.out.println();
		for (String s : p.methods.keySet()) {
			System.out.println("Name: " + s + ", Type: " + p.methodTypes.get(s) + " , Line: " + (p.methods.get(s) + 1));
		}
		System.out.println();
		for (String s : p.lists.keySet()) {
			System.out.println("Name: " + s + ", Type: " + p.listTypes.get(s) + " , Data: "
					+ TextUtils.join(p.lists.get(s), " : "));
		}
		System.out.println();
		for (String c : p.classes.keySet()) {
			Program p1 = p.classes.get(c);
			System.out.println("Class: " + c);
			System.out.println();
			for (String s : p1.variables.keySet()) {
				System.out.println("Name: " + s + " , Type: " + p1.types.get(s) + " , Value: " + p1.variables.get(s));
			}
			System.out.println();
			for (String s : p1.methods.keySet()) {
				System.out.println(
						"Name: " + s + ", Type: " + p1.methodTypes.get(s) + " , Line: " + (p1.methods.get(s) + 1));
			}
			System.out.println();
			for (String s : p1.lists.keySet()) {
				System.out.println("Name: " + s + ", Type: " + p1.listTypes.get(s) + " , Data: "
						+ TextUtils.join(p1.lists.get(s), " : "));
			}
			System.out.println();
		}
	}

	public static int parseEquasion(String s, final Program p) {
		for (String str : p.classes.keySet()) {
			for (String method : p.classes.get(str).methods.keySet()) {
				if (s.contains(str + "." + method + "()")) {
					p.classes.get(str).execMethod(method);
					String result = p.returnValue;
					s.replaceAll(str + "." + method + "()", result);
				}
			}
		}
		for (String method : p.methods.keySet()) {
			if (s.contains(method + "()")) {
				p.execMethod(method);
				String result = p.returnValue;
				s = s.replaceAll(method + "\\(\\)", result);
			}
		}
		for (String str : p.variables.keySet()) {
			s = s.replaceAll(str, p.variables.get(str));
		}
		for (String str : p.lists.keySet()) {
			s = s.replaceAll(str + ".size\\(\\)", String.valueOf(p.lists.get(str).size()));
		}
		final String str = s;
		return (int) new Object() {
			int pos = -1, ch;

			void nextChar() {
				if ((++pos < str.length())) {
					ch = str.charAt(pos);
				} else {
					ch = -1;
				}
			}

			boolean eat(int charToEat) {
				while (ch == ' ')
					nextChar();
				if (ch == charToEat) {
					nextChar();
					return true;
				}
				return false;
			}

			double parse() {
				nextChar();
				double x = parseExpression();
				if (pos < str.length())
					throw new RuntimeException("Unexpected: " + (char) ch);
				return x;
			}

			// Grammar:
			// expression = term | expression `+` term | expression `-` term
			// term = factor | term `*` factor | term `/` factor
			// factor = `+` factor | `-` factor | `(` expression `)`
			// | number | functionName factor | factor `^` factor

			double parseExpression() {
				double x = parseTerm();
				for (;;) {
					if (eat('+'))
						x += parseTerm(); // addition
					else if (eat('-'))
						x -= parseTerm(); // subtraction
					else
						return x;
				}
			}

			double parseTerm() {
				double x = parseFactor();
				for (;;) {
					if (eat('*'))
						x *= parseFactor(); // multiplication
					else if (eat('/'))
						x /= parseFactor(); // division
					else
						return x;
				}
			}

			double parseFactor() {
				if (eat('+'))
					return parseFactor(); // unary plus
				if (eat('-'))
					return -parseFactor(); // unary minus

				double x;
				int startPos = this.pos;
				if (eat('(')) { // parentheses
					x = parseExpression();
					eat(')');
				} else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
					while ((ch >= '0' && ch <= '9') || ch == '.')
						nextChar();
					x = Double.parseDouble(str.substring(startPos, this.pos));
				} else if (ch >= 'a' && ch <= 'z') { // functions
					while (ch >= 'a' && ch <= 'z')
						nextChar();
					String func = str.substring(startPos, this.pos);
					x = parseFactor();
					if (func.equals("sqrt"))
						x = Math.sqrt(x);
					else if (func.equals("sin"))
						x = Math.sin(Math.toRadians(x));
					else if (func.equals("cos"))
						x = Math.cos(Math.toRadians(x));
					else if (func.equals("tan"))
						x = Math.tan(Math.toRadians(x));
					else
						throw new RuntimeException("Unknown function: " + func);
				} else {
					throw new RuntimeException("Unexpected: " + (char) ch);
				}

				if (eat('^'))
					x = Math.pow(x, parseFactor()); // exponentiation

				return x;
			}
		}.parse();
	}

	public static String parseString(String string, Program p) {
		String string1 = "";
		for (String s : string.split("\\+")) {
			s = s.trim();
			boolean b = true;
			if(s.startsWith("\"")){
				s = s.substring(1,s.length()-1);
				b = true;
			}
			for(String method : p.env.methods.keySet()){
				if(s.startsWith(method+"(")){
					String[] args = s.substring(s.indexOf("("), s.lastIndexOf(")")).split(",");
					if(p.env.methods.get(method).onMethod(method, s, p,args) && b){
						s = p.returnValue;
						b = false;
					}
				}
			}
			for (String var : p.variables.keySet()) {
				if (b) {
					if (s.equals(var)) {
						s = p.variables.get(var);
						b = false;
					}
				}
			}
			for (String method : p.methods.keySet()) {
				if (b) {
					if (s.equals(method + "()")) {
						p.execMethod(method);
						s = p.returnValue;
						p.returnValue = "";
						b = false;
					}
				}
			}
			for (String classs : p.classes.keySet()) {
				if (b) {
					if (s.startsWith(classs + ".")) {
						for (String method : p.classes.get(classs).methods.keySet()) {
							if (b) {
								if (s.equals(classs + "." + method + "()")) {
									p.classes.get(classs).execMethod(method);
									s = p.classes.get(classs).returnValue;
									p.classes.get(classs).returnValue = "";
									b = false;
								}
							}
						}
					}
				}
			}
			
			string1 += s;
		}
		return string1;
	}
}
