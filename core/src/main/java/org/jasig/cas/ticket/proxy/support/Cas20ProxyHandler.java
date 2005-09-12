/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentials;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.util.UrlUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * Proxy Handler to handle the default callback functionality of CAS 2.0.
 * <p>
 * The default behavior as defined in the CAS 2 Specification is to callback the
 * URL provided and give it a pgtIou and a pgtId.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas20ProxyHandler implements ProxyHandler, InitializingBean {

    /** The Commons Logging instance. */
    private final Log log = LogFactory.getLog(getClass());

    /** The PGTIOU ticket prefix. */
    private static final String PGTIOU_PREFIX = "PGTIOU";

    /** Generate unique ids. */
    private UniqueTicketIdGenerator uniqueTicketIdGenerator;

    public String handle(final Credentials credentials,
        final String proxyGrantingTicketId) {
        final HttpBasedServiceCredentials serviceCredentials = (HttpBasedServiceCredentials) credentials;
        final String proxyIou = this.uniqueTicketIdGenerator
            .getNewTicketId(PGTIOU_PREFIX);
        final StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(serviceCredentials.getCallbackUrl()
            .toExternalForm());

        if (serviceCredentials.getCallbackUrl().getQuery() != null) {
            stringBuffer.append("&");
        } else {
            stringBuffer.append("?");
        }

        stringBuffer.append("pgtIou=");
        stringBuffer.append(proxyIou);
        stringBuffer.append("&pgtId=");
        stringBuffer.append(proxyGrantingTicketId);

        UrlUtils.getResponseCodeFromString(stringBuffer.toString());

        log.debug("Sent ProxyIou of " + proxyIou + " for service: "
            + serviceCredentials.getCallbackUrl());
        return proxyIou;
    }

    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(
        final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }

    public void afterPropertiesSet() throws Exception {
        if (this.uniqueTicketIdGenerator == null) {
            this.uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();
            log.info("No UniqueTicketIdGenerator specified for "
                + this.getClass().getName() + ".  Using "
                + this.uniqueTicketIdGenerator.getClass().getName());
        }
    }
}
