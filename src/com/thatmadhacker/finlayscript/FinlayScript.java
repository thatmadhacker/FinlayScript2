package com.thatmadhacker.finlayscript;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
		if(s.startsWith("write(")){
			found = false;
			if(p.hasPermission("IO")){
				s = s.substring(6,s.lastIndexOf(")"));
				String path = s.split(",")[0].replaceAll("\"", "").trim();
				String data = s.split(",")[1].replaceAll("\"", "").trim();
				File f = new File(path);
				f.delete();
				f.createNewFile();
				PrintWriter out = new PrintWriter(new FileWriter(f));
				out.print(data);
				out.close();
			}
		}
		for (String str : p.variables.keySet()) {
			if (s.split(" ")[0].equals(str)) {
				if (p.types.get(str) == "STRING") {
					String data = s.substring(str.length()).split("=")[1] + " ";
					String string = "";
					for (String stri : data.split("\\+")) {
						stri = stri.trim();
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
							if (stri.equals(strinn + "()")) {
								p.env.methods.get(strinn).onMethod(strinn, orig);
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
						string += stri;
					}
					string = string.replaceAll("\"", "");
					p.variables.put(str, string);
				} else if (p.types.get(str) == "BOOLEAN") {
					String stri = s.substring(str.length()).split("=")[1] + " ";
					;
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
						if (stri.equals(strinn + "()")) {
							p.env.methods.get(strinn).onMethod(strinn, orig);
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
				} else if (p.types.get(str) == "INTEGER") {
					String data = s.substring(str.length()).split("=")[1] + " ";
					int value = parseEquasion(data.trim(), p);
					p.variables.put(str, String.valueOf(value));
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
				if (found1) {
					ret = ret.replaceAll("\"", "");
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
			String string = "";
			for (String stri : data.split("\\+")) {
				stri = stri.trim();
				boolean found1 = false;
				for (String strin : p.classes.keySet()) {
					if (stri.startsWith(strin + ".")) {
						for (String strinn : p.classes.get(strin).methods.keySet()) {
							if (stri.substring(strin.length() + 1).equals(strinn + "()")) {
								if (!found) {
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
				if (!found1) {
					for (String strin : p.variables.keySet()) {
						if (strin.equals(stri)) {
							stri = p.variables.get(strin);
						}
					}
				}
				string += stri;
			}
			string = string.replaceAll("\"", "");
			p.variables.put(varName, string);
			p.types.put(varName, "STRING");
		} else if (s.startsWith("boolean") && !s.startsWith("boolean()")) {
			s = s.substring(7).trim();
			String str = s.split(" ")[0];
			String stri = s.substring(str.length()).split("=")[1] + " ";
			;
			boolean found1 = false;
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
		}
	}

	public static int parseEquasion(String s, final Program p) {
		for (String str : p.classes.keySet()) {
			for (String method : p.classes.get(str).methods.keySet()) {
				if (s.contains(str + "." + method+"()")) {
					p.classes.get(str).execMethod(method);
					String result = p.returnValue;
					s.replaceAll(str + "." + method + "()", result);
				}
			}
		}
		for (String method : p.methods.keySet()) {
			if (s.contains(method+"()")) {
				p.execMethod(method);
				String result = p.returnValue;
				s = s.replaceAll(method+"\\(\\)", result);
			}
		}
		for (String str : p.variables.keySet()) {
			s = s.replaceAll(str, p.variables.get(str));
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
}
