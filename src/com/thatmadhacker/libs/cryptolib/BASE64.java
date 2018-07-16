package com.thatmadhacker.libs.cryptolib;

import java.io.IOException;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class BASE64 {
	
	public static byte[] decode(String s) throws IOException{
		return new BASE64Decoder().decodeBuffer(s);
	}
	public static String encode(byte[] b){
		return new BASE64Encoder().encode(b);
	}
}
