package com.thatmadhacker.finlayscript;

public abstract class Library {
	
	
	protected boolean init = false;
	public abstract String getName();
	public abstract boolean onMethod(String name, String line, Program p, String[] args);
	public abstract boolean onLine(String line, Program p);
	public abstract void init(Program p);
	public boolean isInit() {
		return init;
	}
	public void setInit(boolean init) {
		this.init = init;
	}
	
}
