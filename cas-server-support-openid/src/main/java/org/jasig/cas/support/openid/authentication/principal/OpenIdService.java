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
package org.jasig.cas.support.openid.authentication.principal;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.AbstractWebApplicationService;
import org.jasig.cas.authentication.principal.DefaultResponse;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.support.openid.OpenIdConstants;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.util.ApplicationContextProvider;
import org.jasig.cas.validation.Assertion;
import org.openid4java.association.Association;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.ParameterList;
import org.openid4java.server.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public final class OpenIdService extends AbstractWebApplicationService {

    /** The Constant LOGGER. */
    protected static final Logger LOGGER = LoggerFactory.getLogger(OpenIdService.class);

    private static final long serialVersionUID = 5776500133123291301L;

    private final String identity;

    private final String artifactId;

    private final ParameterList requestParameters;

    private final String openIdPrefixUrl;

    /**
     * Instantiates a new OpenID service.
     *
     * @param id the id
     * @param originalUrl the original url
     * @param artifactId the artifact id
     * @param openIdIdentity the OpenID identity
     * @param signature the signature
     * @param parameterList the parameter list
     * @param openIdPrefixUrl the prefix url for OpenID
     */
    protected OpenIdService(final String id, final String originalUrl,
            final String artifactId, final String openIdIdentity,
            final String signature, final ParameterList parameterList,
                            final String openIdPrefixUrl) {
        super(id, originalUrl, artifactId);
        this.identity = openIdIdentity;
        this.artifactId = artifactId;
        this.requestParameters = parameterList;
        this.openIdPrefixUrl = openIdPrefixUrl;
    }

    /**
     * Generates an Openid response.
     * If no ticketId is found, response is negative.
     * If we have a ticket id, then we check if we have an association.
     * If so, we ask OpenId server manager to generate the answer according with the existing association.
     * If not, we send back an answer with the ticket id as association handle.
     * This will force the consumer to ask a verification, which will validate the service ticket.
     * @param ticketId the service ticket to provide to the service.
     * @return the generated authentication answer
     */
    @Override
    public Response getResponse(final String ticketId) {
        final Map<String, String> parameters = new HashMap<>();
        if (ticketId != null) {

            final ServerManager manager = (ServerManager) ApplicationContextProvider.getApplicationContext().getBean("serverManager");
            final CentralAuthenticationService cas = ApplicationContextProvider.getApplicationContext()
                                                .getBean("centralAuthenticationService", CentralAuthenticationService.class);
            boolean associated = false;
            boolean associationValid = true;
            try {
                final AuthRequest authReq = AuthRequest.createAuthRequest(requestParameters, manager.getRealmVerifier());
                final Map parameterMap = authReq.getParameterMap();
                if (parameterMap != null && parameterMap.size() > 0) {
                    final String assocHandle = (String) parameterMap.get(OpenIdConstants.OPENID_ASSOCHANDLE);
                    if (assocHandle != null) {
                        final Association association = manager.getSharedAssociations().load(assocHandle);
                        if (association != null) {
                            associated = true;
                            if (association.hasExpired()) {
                                associationValid = false;
                            }
                        }

                    }
                }
            } catch (final MessageException me) {
                LOGGER.error("Message exception : {}", me.getMessage(), me);
            }

            boolean successFullAuthentication = true;
            Assertion assertion = null;
            try {
                if (associated) {
                    if (associationValid) {
                        assertion = cas.validateServiceTicket(ticketId, this);
                        LOGGER.info("Validated openid ticket");
                    } else {
                        successFullAuthentication = false;
                    }
                }
            } catch (final TicketException te) {
                LOGGER.error("Could not validate ticket : {}", te.getMessage(), te);
                successFullAuthentication = false;
            }

            final String id;
            if (assertion != null && OpenIdConstants.OPENID_IDENTIFIERSELECT.equals(this.identity)) {
                id = this.openIdPrefixUrl + '/' + assertion.getPrimaryAuthentication().getPrincipal().getId();
            } else {
                id = this.identity;
            }
            // We sign directly (final 'true') because we don't add extensions
            // response message can be either a DirectError or an AuthSuccess here.
            // Anyway, handling is the same : send the response message
            final Message response = manager.authResponse(requestParameters,
                    id,
                    id,
                    successFullAuthentication,
                    true);
            parameters.putAll(response.getParameterMap());
            if (!associated) {
                parameters.put(OpenIdConstants.OPENID_ASSOCHANDLE, ticketId);
            }
        } else {
            parameters.put(OpenIdConstants.OPENID_MODE, OpenIdConstants.CANCEL);
        }
        return DefaultResponse.getRedirectResponse(getOriginalUrl(), parameters);
    }

    /**
     * Return that the service is already logged out.
     *
     * @return that the service is already logged out.
     */
    @Override
    public boolean isLoggedOutAlready() {
        return true;
    }

    /**
     * Creates the service from the request.
     *
     * @param request the request
     * @param openIdPrefixUrl the prefix url for OpenID
     * @return the OpenID service
     */
    public static OpenIdService createServiceFrom(
            final HttpServletRequest request, final String openIdPrefixUrl) {
        final String service = request.getParameter(OpenIdConstants.OPENID_RETURNTO);
        final String openIdIdentity = request.getParameter(OpenIdConstants.OPENID_IDENTITY);
        final String signature = request.getParameter(OpenIdConstants.OPENID_SIG);

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter(OpenIdConstants.OPENID_ASSOCHANDLE);
        final ParameterList paramList = new ParameterList(request.getParameterMap());

        return new OpenIdService(id, service, artifactId, openIdIdentity,
                signature, paramList, openIdPrefixUrl);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.identity)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OpenIdService other = (OpenIdService) obj;
        if (this.identity == null) {
            if (other.identity != null) {
                return false;
            }
        } else if (!this.identity.equals(other.identity)) {
            return false;
        }
        return true;
    }

    public String getIdentity() {
        return this.identity;
    }
}
