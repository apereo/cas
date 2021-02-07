package com.duosecurity.duoweb;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public final class DuoWeb {
	private static final String DUO_PREFIX = "TX";
	private static final String APP_PREFIX = "APP";
	private static final String AUTH_PREFIX = "AUTH";

	private static final int DUO_EXPIRE = 300;
	private static final int APP_EXPIRE = 3600;

	private static final int IKEY_LEN = 20;
	private static final int SKEY_LEN = 40;
	private static final int AKEY_LEN = 40;

	public static final String ERR_USER = "ERR|The username passed to sign_request() is invalid.";
	public static final String ERR_IKEY = "ERR|The Duo integration key passed to sign_request() is invalid.";
	public static final String ERR_SKEY = "ERR|The Duo secret key passed to sign_request() is invalid.";
	public static final String ERR_AKEY = "ERR|The application secret key passed to sign_request() must be at least " + AKEY_LEN + " characters.";
	public static final String ERR_UNKNOWN = "ERR|An unknown error has occurred.";

	public static String signRequest(final String ikey, final String skey, final String akey, final String username) {
		return signRequest(ikey, skey, akey, username, System.currentTimeMillis() / 1000);
	}

	public static String signRequest(final String ikey, final String skey, final String akey, final String username, final long time) {
		final String duo_sig;
		final String app_sig;

		if (username.equals("")) {
			return ERR_USER;
		}
		if (username.indexOf('|') != -1) {
			return ERR_USER;
		}
		if (ikey.equals("") || ikey.length() != IKEY_LEN) {
			return ERR_IKEY;
		}
		if (skey.equals("") || skey.length() != SKEY_LEN) {
			return ERR_SKEY;
		}
		if (akey.equals("") || akey.length() < AKEY_LEN) {
			return ERR_AKEY;
		}

		try {
			duo_sig = signVals(skey, username, ikey, DUO_PREFIX, DUO_EXPIRE, time);
			app_sig = signVals(akey, username, ikey, APP_PREFIX, APP_EXPIRE, time);
		} catch (Exception e) {
			return ERR_UNKNOWN;
		}

		return duo_sig + ":" + app_sig;
	}

	public static String verifyResponse(final String ikey, final String skey, final String akey, final String sig_response)
		throws DuoWebException, NoSuchAlgorithmException, InvalidKeyException, IOException {
		return verifyResponse(ikey, skey, akey, sig_response, System.currentTimeMillis() / 1000);
	}

	public static String verifyResponse(final String ikey, final String skey, final String akey, final String sig_response, final long time)
		throws DuoWebException, NoSuchAlgorithmException, InvalidKeyException, IOException {
		String auth_user = null;
		String app_user = null;

		final String[] sigs = sig_response.split(":");
		final String auth_sig = sigs[0];
		final String app_sig = sigs[1];

		auth_user = parseVals(skey, auth_sig, AUTH_PREFIX, ikey, time);
		app_user = parseVals(akey, app_sig, APP_PREFIX, ikey, time);

		if (!auth_user.equals(app_user)) {
			throw new DuoWebException("Authentication failed.");
		}

		return auth_user;
	}

	private static String signVals(final String key, final String username, final String ikey, final String prefix, final int expire, final long time) 
		throws InvalidKeyException, NoSuchAlgorithmException {
		final long expire_ts = time + expire;
		final String exp = Long.toString(expire_ts);

		final String val = username + "|" + ikey + "|" + exp;
		final String cookie = prefix + "|" + Base64.encodeBytes(val.getBytes());
		final String sig = Util.hmacSign(key, cookie);

		return cookie + "|" + sig;
	}

	private static String parseVals(final String key, final String val, final String prefix, final String ikey, final long time)
		throws InvalidKeyException, NoSuchAlgorithmException, IOException, DuoWebException {

		final String[] parts = val.split("\\|");
		if (parts.length != 3) {
			throw new DuoWebException("Invalid response");
		}

		final String u_prefix = parts[0];
		final String u_b64 = parts[1];
		final String u_sig = parts[2];

		final String sig = Util.hmacSign(key, u_prefix + "|" + u_b64);
		if (!Util.hmacSign(key, sig).equals(Util.hmacSign(key, u_sig))) {
			throw new DuoWebException("Invalid response");
		}

		if (!u_prefix.equals(prefix)) {
			throw new DuoWebException("Invalid response");
		}

		final byte[] decoded = Base64.decode(u_b64);
		final String cookie = new String(decoded);

		final String[] cookie_parts = cookie.split("\\|");
		if (cookie_parts.length != 3) {
			throw new DuoWebException("Invalid response");
		}
		final String username = cookie_parts[0];
		final String u_ikey = cookie_parts[1];
		final String expire = cookie_parts[2];

		if (!u_ikey.equals(ikey)) {
			throw new DuoWebException("Invalid response");
		}

		final long expire_ts = Long.parseLong(expire);
		if (time >= expire_ts) {
			throw new DuoWebException("Transaction has expired. Please check that the system time is correct.");
		}

		return username;
	}
}
