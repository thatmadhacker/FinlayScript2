package com.thatmadhacker.networklib;

import com.thatmadhacker.finlayscript.Library;
import com.thatmadhacker.finlayscript.Program;

public class NetworkingLib implements Library {

	@Override
	public boolean onMethod(String name, String line, Program p, String[] args) {
		if(name.equalsIgnoreCase("openSocket")){
			
		}
		return false;
	}

	@Override
	public boolean onLine(String line, Program p) {
		return false;
	}

	@Override
	public void init(Program p) {
		
	}

}
