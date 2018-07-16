package com.thatmadhacker.libs.math;

import com.thatmadhacker.finlayscript.Library;
import com.thatmadhacker.finlayscript.Program;

public class MathLib extends Library {

	@Override
	public String getName() {
		return "math";
	}

	@Override
	public boolean onMethod(String name, String line, Program p, String[] args) {
		if(name.equalsIgnoreCase("sin")){
			double value = Double.valueOf(line.substring(line.indexOf("sin("), line.lastIndexOf(")")-1));
			p.returnValue = String.valueOf(Math.sin(value));
			return true;
		}else if(name.equalsIgnoreCase("tan")){
			double value = Double.valueOf(line.substring(line.indexOf("tan("), line.lastIndexOf(")")-1));
			p.returnValue = String.valueOf(Math.tan(value));
			return true;
		}else if(name.equalsIgnoreCase("cos")){
			double value = Double.valueOf(line.substring(line.indexOf("cos("), line.lastIndexOf(")")-1));
			p.returnValue = String.valueOf(Math.cos(value));
			return true;
		}else if(name.equals("pi")){
			p.returnValue = String.valueOf(Math.PI);
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
		p.env.methods.put("tan", this);
		p.env.methods.put("sin", this);
		p.env.methods.put("cos", this);
	}

}
