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
package org.jasig.cas.support.saml.authentication.principal;


import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.ResponseBuilder;
import org.jasig.cas.authentication.principal.WebApplicationService;
/**
 * Implementation of a Service that supports Google Accounts (eventually a more
 * generic SAML2 support will come).
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class GoogleAccountsService extends AbstractWebApplicationService {

    private static final long serialVersionUID = 6678711809842282833L;

    private final String relayState;

    private final String requestId;

    /**
     * Instantiates a new google accounts service.
     *
     * @param id the id
     * @param relayState the relay state
     * @param requestId the request id
     * @param responseBuilder the response builder
     */
    protected GoogleAccountsService(final String id, final String relayState, final String requestId,
                                    final ResponseBuilder<WebApplicationService> responseBuilder) {
        super(id, id, null, responseBuilder);
        this.relayState = relayState;
        this.requestId = requestId;
    }

    /**
     * Return true if the service is already logged out.
     *
     * @return true if the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready() {
        return true;
    }


    public String getRelayState() {
        return relayState;
    }

    public String getRequestId() {
        return requestId;
    }
}
