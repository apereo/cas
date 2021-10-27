/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.jasig.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * Proxy Handler to handle the default callback functionality of CAS 2.0.
 * <p>
 * The default behavior as defined in the CAS 2 Specification is to callback the
 * URL provided and give it a pgtIou and a pgtId.
 * </p>
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class Cas20ProxyHandler implements ProxyHandler {
    private static final int BUFFER_LENGTH_ADDITIONAL_CHARGE = 15;

    /** The proxy granting ticket identifier parameter. */
    private static final String PARAMETER_PROXY_GRANTING_TICKET_IOU = "pgtIou";

    /** The Constant proxy granting ticket parameter. */
    private static final String PARAMETER_PROXY_GRANTING_TICKET_ID = "pgtId";

    /** The Commons Logging instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Generate unique ids. */
    @NotNull
    private UniqueTicketIdGenerator uniqueTicketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /** Instance of Apache Commons HttpClient. */
    @NotNull
    private HttpClient httpClient;

    @Override
    public String handle(final Credential credential, final TicketGrantingTicket proxyGrantingTicketId) {
        final HttpBasedServiceCredential serviceCredentials = (HttpBasedServiceCredential) credential;
        final String proxyIou = this.uniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PROXY_GRANTING_TICKET_IOU_PREFIX);

        final URL callbackUrl = serviceCredentials.getCallbackUrl();
        final String serviceCredentialsAsString = callbackUrl.toExternalForm();
        final int bufferLength = serviceCredentialsAsString.length() + proxyIou.length()
                + proxyGrantingTicketId.getId().length() + BUFFER_LENGTH_ADDITIONAL_CHARGE;
        final StringBuilder stringBuffer = new StringBuilder(bufferLength);

        stringBuffer.append(serviceCredentialsAsString);

        if (callbackUrl.getQuery() != null) {
            stringBuffer.append('&');
        } else {
            stringBuffer.append('?');
        }

        stringBuffer.append(PARAMETER_PROXY_GRANTING_TICKET_IOU);
        stringBuffer.append('=');
        stringBuffer.append(proxyIou);
        stringBuffer.append('&');
        stringBuffer.append(PARAMETER_PROXY_GRANTING_TICKET_ID);
        stringBuffer.append('=');
        stringBuffer.append(proxyGrantingTicketId);

        if (this.httpClient.isValidEndPoint(stringBuffer.toString())) {
            logger.debug("Sent ProxyIou of {} for service: {}", proxyIou, serviceCredentials);
            return proxyIou;
        }

        logger.debug("Failed to send ProxyIou of {} for service: {}", proxyIou, serviceCredentials);
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
