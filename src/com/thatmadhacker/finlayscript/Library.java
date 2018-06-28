package com.thatmadhacker.finlayscript;

public interface Library {

	public abstract boolean onMethod(String name, String line, Program p, String[] args);
	public abstract boolean onLine(String line, Program p);
	public abstract void init(Program p);
}
