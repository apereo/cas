package org.jasig.cas.client.validation.support;

import java.net.URL;

import org.jasig.cas.client.receipt.CasReceipt;
import org.jasig.cas.client.validation.ServiceTicketValidator;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public abstract class AbstractServiceTicketValidator implements ServiceTicketValidator, InitializingBean {

    private URL casValidateUrl;

    private URL proxyCallbackUrl;

    private boolean renew = false;

    private URL service;

    /**
     * @see org.jasig.cas.client.validation.TicketValidator#validate(java.lang.String)
     */
    public final CasReceipt validate(String ticketId) {
        return this.validateInternal(ticketId);
    }

    public abstract CasReceipt validateInternal(String ticketId);

    /**
     * @param casValidateUrl The casValidateUrl to set.
     */
    public void setCasValidateUrl(URL casValidateUrl) {
        this.casValidateUrl = casValidateUrl;
    }

    /**
     * @param proxyCallbackUrl The proxyCallbackUrl to set.
     */
    public void setProxyCallbackUrl(URL proxyCallbackUrl) {
        this.proxyCallbackUrl = proxyCallbackUrl;
    }

    /**
     * @param renew The renew to set.
     */
    public void setRenew(boolean renew) {
        this.renew = renew;
    }

    /**
     * @param service The service to set.
     */
    public void setService(URL service) {
        this.service = service;
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        if (this.casValidateUrl == null || !this.casValidateUrl.getProtocol().startsWith("https"))
            throw new IllegalStateException("casValidateUrl must be set (and secure) on " + this.getClass().getName());

        if (this.proxyCallbackUrl != null && !this.proxyCallbackUrl.getProtocol().startsWith("https"))
            throw new IllegalStateException("The ProxyCallbackUrl must be a secure URL");

        if (this.service == null || !this.service.getProtocol().startsWith("https")) {
            throw new IllegalStateException("Service URL must be set and be secure!");
        }
    }

    /**
     * @return Returns the casValidateUrl.
     */
    public URL getCasValidateUrl() {
        return this.casValidateUrl;
    }

    /**
     * @return Returns the proxyCallbackUrl.
     */
    public URL getProxyCallbackUrl() {
        return this.proxyCallbackUrl;
    }

    /**
     * @return Returns the renew.
     */
    public boolean isRenew() {
        return this.renew;
    }

    /**
     * @return Returns the service.
     */
    public URL getService() {
        return this.service;
    }
}