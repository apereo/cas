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

import org.apache.commons.lang3.NotImplementedException;
import org.jasig.cas.authentication.principal.AbstractServiceFactory;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.saml.SamlProtocolConstants;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.util.GoogleSaml20ObjectBuilder;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Builds {@link GoogleAccountsService} objects.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class GoogleAccountsServiceFactory extends AbstractServiceFactory<GoogleAccountsService> {

    private static final GoogleSaml20ObjectBuilder BUILDER = new GoogleSaml20ObjectBuilder();

    @NotNull
    private final PublicKey publicKey;

    @NotNull
    private final PrivateKey privateKey;

    @NotNull
    private final ServicesManager servicesManager;

    /**
     * Instantiates a new Google accounts service factory.
     *
     * @param publicKey the public key
     * @param privateKey the private key
     * @param servicesManager the services manager
     */
    public GoogleAccountsServiceFactory(final PublicKey publicKey, final PrivateKey privateKey,
                                        final ServicesManager servicesManager) {

        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.servicesManager = servicesManager;
    }

    @Override
    public GoogleAccountsService createService(final HttpServletRequest request) {
        final String relayState = request.getParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE);

        final String xmlRequest = BUILDER.decodeSamlAuthnRequest(
                request.getParameter(SamlProtocolConstants.PARAMETER_SAML_REQUEST));

        if (!StringUtils.hasText(xmlRequest)) {
            return null;
        }

        final Document document = AbstractSaml20ObjectBuilder.constructDocumentFromXml(xmlRequest);

        if (document == null) {
            return null;
        }

        final Element root = document.getRootElement();
        final String assertionConsumerServiceUrl = root.getAttributeValue("AssertionConsumerServiceURL");
        final String requestId = root.getAttributeValue("ID");

        return new GoogleAccountsService(assertionConsumerServiceUrl,
                relayState, requestId, privateKey, publicKey, servicesManager);
    }

    @Override
    public GoogleAccountsService createService(final String id) {
        throw new NotImplementedException("This operation is not supported. ");
    }
}
