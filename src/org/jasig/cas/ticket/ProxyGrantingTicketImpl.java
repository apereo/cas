/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Ticket representing a proxy granting ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class ProxyGrantingTicketImpl extends TicketGrantingTicketImpl implements ProxyGrantingTicket {

    private static final long serialVersionUID = 7880535073035146734L;

    final private ServiceTicket parent;

    final private URL proxyId;

    final private String pgtIou;

    /**
     * @param id
     * @param parent
     * @param proxyId
     * @param pgtIou
     * @param policy
     */
    public ProxyGrantingTicketImpl(final String id, final ServiceTicket parent, final URL proxyId, final String pgtIou, final ExpirationPolicy policy) {
        super(id, parent.getGrantor().getPrincipal(), policy);

        if (parent == null || proxyId == null || pgtIou == null)
            throw new IllegalArgumentException("parent, pgtIou and proxyId are required parameters.");

        if (!proxyId.getProtocol().equals("https"))
            throw new IllegalArgumentException("ProxyId can only handle HTTPS urls");

        this.parent = parent;
        this.proxyId = proxyId;
        this.pgtIou = pgtIou;
    }

    /**
     * @return Returns the parent.
     */
    public ServiceTicket getParent() {
        return this.parent;
    }

    /**
     * @return Returns the proxyId.
     */
    public URL getProxyId() {
        return this.proxyId;
    }

    public List getProxies() {
        List list = new ArrayList();
        list.add(this.getProxyId());

        if (this.parent.getGrantor().getClass().isAssignableFrom(ProxyGrantingTicket.class))
            list.addAll(((ProxyGrantingTicket)this.parent.getGrantor()).getProxies());

        return Collections.unmodifiableList(list);
    }

    /**
     * @see org.jasig.cas.ticket.Ticket#isExpired()
     */
    public boolean isExpired() {
        return super.isExpired() || this.parent.isExpired();
    }

    /**
     * @see org.jasig.cas.ticket.ProxyGrantingTicket#getProxyIou()
     */
    public String getProxyIou() {
        HttpClient httpClient;
        GetMethod getMethod;
        String response = null;
        String callbackUrl = this.getProxyId().toString();

        if (this.getProxyId().getQuery() != null)
            callbackUrl += "&" + "pgtIou=" + this.pgtIou + "&pgtId=" + this.getId();
        else
            callbackUrl += "?" + "pgtIou=" + this.pgtIou + "&pgtId=" + this.getId();

        httpClient = new HttpClient();
        getMethod = new GetMethod(callbackUrl);
        try {
            httpClient.executeMethod(getMethod);
            response = getMethod.getResponseBodyAsString();
        }
        catch (IOException ioe) {
        }
        finally {
            getMethod.releaseConnection();
        }
        if (response != null)
            return this.pgtIou;
        else
            return null;
    }
}
