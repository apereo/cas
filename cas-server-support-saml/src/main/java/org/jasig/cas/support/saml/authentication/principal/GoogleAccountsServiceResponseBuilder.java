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

import org.jasig.cas.authentication.principal.AbstractServiceResponseBuilder;
import org.jasig.cas.authentication.principal.Response;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jasig.cas.util.ISOStandardDateFormat;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;

import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds the google accounts service response.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceResponseBuilder extends AbstractServiceResponseBuilder {

    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private final GoogleSaml20ObjectBuilder samlObjectBuilder;
    private final ServicesManager servicesManager;

    /**
     * Instantiates a new Google accounts service response builder.
     *
     * @param privateKey the private key
     * @param publicKey the public key
     * @param samlObjectBuilder the saml object builder
     * @param servicesManager the services manager
     */
    public GoogleAccountsServiceResponseBuilder(final PrivateKey privateKey,
                                                final PublicKey publicKey,
                                                final GoogleSaml20ObjectBuilder samlObjectBuilder,
                                                final ServicesManager servicesManager) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.samlObjectBuilder = samlObjectBuilder;
        this.servicesManager = servicesManager;
    }

    @Override
    public Response build(final WebApplicationService webApplicationService, final String ticketId) {
        final GoogleAccountsService service = (GoogleAccountsService) webApplicationService;

        final Map<String, String> parameters = new HashMap<>();
        final String samlResponse = constructSamlResponse(service);
        final String signedResponse = samlObjectBuilder.signSamlResponse(samlResponse,
            this.privateKey, this.publicKey);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RESPONSE, signedResponse);
        parameters.put(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE, service.getRelayState());

        return buildPost(service, parameters);
    }

    /**
     * Construct SAML response.
     * <a href="http://bit.ly/1uI8Ggu">See this reference for more info.</a>
     * @param service the service
     * @return the SAML response
     */
    protected String constructSamlResponse(final GoogleAccountsService service) {
        final DateTime currentDateTime = DateTime.parse(new ISOStandardDateFormat().getCurrentDateAndTime());
        final DateTime notBeforeIssueInstant = DateTime.parse("2003-04-17T00:46:02Z");

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        final String userId = registeredService.getUsernameAttributeProvider()
                    .resolveUsername(service.getPrincipal(), service);

        final org.opensaml.saml.saml2.core.Response response = samlObjectBuilder.newResponse(
            samlObjectBuilder.generateSecureRandomId(), currentDateTime, service.getId(), service);
        response.setStatus(samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));

        final AuthnStatement authnStatement = samlObjectBuilder.newAuthnStatement(
            AuthnContext.PASSWORD_AUTHN_CTX, currentDateTime);
        final Assertion assertion = samlObjectBuilder.newAssertion(authnStatement,
            "https://www.opensaml.org/IDP",
            notBeforeIssueInstant, samlObjectBuilder.generateSecureRandomId());

        final Conditions conditions = samlObjectBuilder.newConditions(notBeforeIssueInstant,
            currentDateTime, service.getId());
        assertion.setConditions(conditions);

        final Subject subject = samlObjectBuilder.newSubject(NameID.EMAIL, userId,
            service.getId(), currentDateTime, service.getRequestId());
        assertion.setSubject(subject);

        response.getAssertions().add(assertion);

        final StringWriter writer = new StringWriter();
        samlObjectBuilder.marshalSamlXmlObject(response, writer);

        final String result = writer.toString();
        logger.debug("Generated Google SAML response: {}", result);
        return result;
    }
}
