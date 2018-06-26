package com.thatmadhacker.cryptolib;

import javax.crypto.SecretKey;

import com.thatmadhacker.finlayscript.FinlayScript;
import com.thatmadhacker.finlayscript.Library;
import com.thatmadhacker.finlayscript.Program;

public class CryptoLib implements Library{

	@Override
	public boolean onMethod(String name, String line, Program p, String[] args) {
		if(name.equalsIgnoreCase("genKey")){
			line = line.substring(line.indexOf("genKey(")+name.length()+1, line.lastIndexOf(")"));
			String algo = FinlayScript.parseString(line.split(",")[0].trim(),p);
			int length = Integer.valueOf(FinlayScript.parseEquasion(line.split(",")[1].trim(),p));
			try {
				SecretKey key = Symetric.genKey(algo, length);
				p.returnValue = BASE64.encode(key.getEncoded());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}else if(name.equalsIgnoreCase("encrypt")){
			line = line.substring(line.indexOf("encrypt(")+name.length()+1, line.lastIndexOf(")"));
			String algo = FinlayScript.parseString(line.split(",")[0].trim(),p);
			String key = FinlayScript.parseString(line.split(",")[1].trim(),p);
			String message = FinlayScript.parseString(line.split(",")[2].trim(),p);
			try {
				SecretKey secret = Symetric.genKeyFromByteArray(BASE64.decode(key), algo);
				p.returnValue = Symetric.encrypt(message, secret, algo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}else if(name.equalsIgnoreCase("decrypt")){
			line = line.substring(line.indexOf("decrypt(")+name.length()+1, line.lastIndexOf(")"));
			String algo = FinlayScript.parseString(line.split(",")[0].trim(),p);
			String key = FinlayScript.parseString(line.split(",")[1].trim(),p);
			String message = FinlayScript.parseString(line.split(",")[2].trim(),p);
			try {
				SecretKey secret = Symetric.genKeyFromByteArray(BASE64.decode(key), algo);
				p.returnValue = Symetric.decrypt(message, secret, algo);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}else if(name.equalsIgnoreCase("hash")){
			line = line.substring(line.indexOf("hash(")+name.length()+1, line.lastIndexOf(")"));
			String algo = FinlayScript.parseString(line.split(",")[0].trim(),p);
			String message = FinlayScript.parseString(line.split(",")[1].trim(),p);
			try {
				p.returnValue = HashingUtils.hash(message, algo);
			} catch (Exception e) {
				e.printStackTrace();
			}
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
		p.env.methods.put("genKey", this);
		p.env.methods.put("encrypt", this);
		p.env.methods.put("decrypt", this);
		p.env.methods.put("hash", this);
	}

}
