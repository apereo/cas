package com.duosecurity.duoweb;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Util {
	public static String hmacSign(String skey, String data)
			throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKeySpec key = new SecretKeySpec(skey.getBytes(), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);
		byte[] raw = mac.doFinal(data.getBytes());
		return bytesToHex(raw);
	}

	public static String bytesToHex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
}
