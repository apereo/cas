package org.jasig.cas.authentication.principal;

import java.net.URL;
import org.jasig.cas.authentication.Service;

/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class HttpBasedServiceCredentials implements Credentials {
	final private URL callbackUrl;
	final private String proxyIou;
	final private Service service;
	
	public HttpBasedServiceCredentials(URL callbackUrl, String proxyIou, Service service) {
		this.callbackUrl = callbackUrl;
		this.proxyIou    = proxyIou;
		this.service     = service;
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

	/**
	 * @return Returns the service.
	 */
	public Service getService() {
		return this.service;
	}
}
