package org.jasig.cas.authentication.principal;

import java.net.URL;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class HttpBasedServiceCredentials implements Credentials {
	final private URL callbackUrl;
	final private String proxyIou;
	
	public HttpBasedServiceCredentials(URL callbackUrl, String proxyIou) {
		this.callbackUrl = callbackUrl;
		this.proxyIou    = proxyIou;
	}

	/**
	 * @return Returns the callbackUrl.
	 */
	public URL getCallbackUrl() {
		return this.callbackUrl;
	}

	/**
	 * @return Returns the proxyIou.
	 */
	public String getProxyIou() {
		return this.proxyIou;
	}
}
