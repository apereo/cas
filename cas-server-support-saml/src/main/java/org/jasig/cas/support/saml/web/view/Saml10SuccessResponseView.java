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
package org.jasig.cas.support.saml.web.view;

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.RememberMeCredential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.joda.time.DateTime;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.AuthenticationStatement;
import org.opensaml.saml.saml1.core.Conditions;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;
import org.opensaml.saml.saml1.core.Subject;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of a view to return a SAML SOAP response and assertion, based on
 * the SAML 1.1 specification.
 * <p>
 * If an AttributePrincipal is supplied, then the assertion will include the
 * attributes from it (assuming a String key/Object value pair). The only
 * Authentication attribute it will look at is the authMethod (if supplied).
 * <p>
 * Note that this class will currently not handle proxy authentication.
 * <p>
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public final class Saml10SuccessResponseView extends AbstractSaml10ResponseView {
    /** Namespace for custom attributes in the saml validation payload. */
    private static final String VALIDATION_SAML_ATTRIBUTE_NAMESPACE = "http://www.ja-sig.org/products/cas/";

    private static final int DEFAULT_ISSUE_LENGTH = 30000;

    /** The issuer, generally the hostname. */
    @NotNull
    private String issuer;

    /**
     * The amount of time in milliseconds this is valid for.
     * Defaults to {@value}.
     **/
    @Min(1000)
    private long issueLength = DEFAULT_ISSUE_LENGTH;

    @NotNull
    private String rememberMeAttributeName = CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME;

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {

        final DateTime issuedAt = response.getIssueInstant();
        final Service service = getAssertionFrom(model).getService();

        final Authentication authentication = getPrimaryAuthenticationFrom(model);
        final String authenticationMethod = (String) authentication.getAttributes().get(
                SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD);

        final AuthenticationStatement authnStatement = this.samlObjectBuilder.newAuthenticationStatement(
                authentication.getAuthenticationDate(), authenticationMethod, getPrincipal(model).getId());

        final Assertion assertion = this.samlObjectBuilder.newAssertion(authnStatement, this.issuer, issuedAt,
                this.samlObjectBuilder.generateSecureRandomId());
        final Conditions conditions = this.samlObjectBuilder.newConditions(issuedAt, service.getId(), this.issueLength);
        assertion.setConditions(conditions);

        final Subject subject = this.samlObjectBuilder.newSubject(getPrincipal(model).getId());
        final Map<String, Object> attributesToSend = prepareSamlAttributes(model, service);

        if (!attributesToSend.isEmpty()) {
            assertion.getAttributeStatements().add(this.samlObjectBuilder.newAttributeStatement(
                    subject, attributesToSend, VALIDATION_SAML_ATTRIBUTE_NAMESPACE));
        }

        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));
        response.getAssertions().add(assertion);
    }


    /**
     * Prepare saml attributes. Combines both principal and authentication
     * attributes. If the authentication is to be remembered, uses {@link #setRememberMeAttributeName(String)}
     * for the remember-me attribute name.
     *
     * @param model   the model
     * @param service the service
     * @return the final map
     * @since 4.1.0
     */
    private Map<String, Object> prepareSamlAttributes(final Map<String, Object> model, final Service service) {
        final Map<String, Object> authnAttributes = new HashMap<>(getAuthenticationAttributesAsMultiValuedAttributes(model));
        if (isRememberMeAuthentication(model)) {
            authnAttributes.remove(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME);
            authnAttributes.put(this.rememberMeAttributeName, Boolean.TRUE.toString());
        }

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final Map<String, Object> attributesToReturn = new HashMap<>();
        attributesToReturn.putAll(getPrincipalAttributesAsMultiValuedAttributes(model));
        attributesToReturn.putAll(authnAttributes);

        decideIfCredentialPasswordShouldBeReleasedAsAttribute(attributesToReturn, model, registeredService);
        decideIfProxyGrantingTicketShouldBeReleasedAsAttribute(attributesToReturn, model, registeredService);

        final Map<String, Object> finalAttributes = this.casAttributeEncoder.encodeAttributes(attributesToReturn, service);
        return finalAttributes;
    }

    public void setIssueLength(final long issueLength) {
        this.issueLength = issueLength;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public void setRememberMeAttributeName(final String rememberMeAttributeName) {
        this.rememberMeAttributeName = rememberMeAttributeName;
    }
}
