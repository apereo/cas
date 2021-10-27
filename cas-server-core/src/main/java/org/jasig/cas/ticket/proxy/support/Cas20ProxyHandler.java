/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HttpBasedServiceCredential;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.HttpClient;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

/**
 * Proxy Handler to handle the default callback functionality of CAS 2.0.
 * <p>
 * The default behavior as defined in the CAS 2 Specification is to callback the
 * URL provided and give it a pgtIou and a pgtId.
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0
 */
public final class Cas20ProxyHandler implements ProxyHandler {

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** The PGTIOU ticket prefix. */
    private static final String PGTIOU_PREFIX = "PGTIOU";

    /** The proxy granting ticket identifier parameter. */
    private static final String PARAMETER_PROXY_GRANTING_TICKET_IOU = "pgtIou";
    
    /** The Constant proxy granting ticket parameter. */
    private static final String PARAMETER_PROXY_GRANTING_TICKET_ID = "pgtId";
    
    /** Generate unique ids. */
    @NotNull
    private UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** Instance of Apache Commons HttpClient. */
    @NotNull
    private HttpClient httpClient;

    public String handle(final Credential credential, final String proxyGrantingTicketId) {
        final HttpBasedServiceCredential serviceCredentials = (HttpBasedServiceCredential) credential;
        final String proxyIou = this.uniqueTicketIdGenerator.getNewTicketId(PGTIOU_PREFIX);
        final String serviceCredentialsAsString = serviceCredentials.getCallbackUrl().toExternalForm();
        final int bufferLength = serviceCredentialsAsString.length() + proxyIou.length() + proxyGrantingTicketId.length() + 15;
        final StringBuilder stringBuffer = new StringBuilder(bufferLength);

        stringBuffer.append(serviceCredentialsAsString);

        if (serviceCredentials.getCallbackUrl().getQuery() != null) {
            stringBuffer.append("&");
        } else {
            stringBuffer.append("?");
        }

        stringBuffer.append(PARAMETER_PROXY_GRANTING_TICKET_IOU);
        stringBuffer.append("=");
        stringBuffer.append(proxyIou);
        stringBuffer.append("&");
        stringBuffer.append(PARAMETER_PROXY_GRANTING_TICKET_ID);
        stringBuffer.append("=");
        stringBuffer.append(proxyGrantingTicketId);

        if (this.httpClient.isValidEndPoint(stringBuffer.toString())) {
            logger.debug("Sent ProxyIou of {} for service: {}", proxyIou, serviceCredentials.toString());
            return proxyIou;
        }

        logger.debug("Failed to send ProxyIou of {} for service: {}", proxyIou, serviceCredentials.toString());
        return null;
    }

    /**
     * @param uniqueTicketIdGenerator The uniqueTicketIdGenerator to set.
     */
    public void setUniqueTicketIdGenerator(final UniqueTicketIdGenerator uniqueTicketIdGenerator) {
        this.uniqueTicketIdGenerator = uniqueTicketIdGenerator;
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public boolean canHandle(final Credential credential) {
        return true;
    }
}
