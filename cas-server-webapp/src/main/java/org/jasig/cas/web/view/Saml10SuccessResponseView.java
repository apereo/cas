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
package org.jasig.cas.web.view;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.SamlAuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.principal.RememberMeCredentials;
import org.jasig.cas.authentication.principal.Service;
import org.joda.time.DateTime;
import org.opensaml.saml1.core.Assertion;
import org.opensaml.saml1.core.Attribute;
import org.opensaml.saml1.core.AttributeStatement;
import org.opensaml.saml1.core.AttributeValue;
import org.opensaml.saml1.core.Audience;
import org.opensaml.saml1.core.AudienceRestrictionCondition;
import org.opensaml.saml1.core.AuthenticationStatement;
import org.opensaml.saml1.core.Conditions;
import org.opensaml.saml1.core.ConfirmationMethod;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml1.core.Response;
import org.opensaml.saml1.core.StatusCode;
import org.opensaml.saml1.core.Subject;
import org.opensaml.saml1.core.SubjectConfirmation;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;

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
 * Note: This class currently expects a bean called "ServiceRegistry" to exist.
 * 
 * @author Scott Battaglia
 * @author Marvin S. Addison
 * @since 3.1
 */
public final class Saml10SuccessResponseView extends AbstractSaml10ResponseView {

    /** Namespace for custom attributes. */
    private static final String NAMESPACE = "http://www.ja-sig.org/products/cas/";

    private static final String REMEMBER_ME_ATTRIBUTE_NAME = "longTermAuthenticationRequestTokenUsed";

    private static final String REMEMBER_ME_ATTRIBUTE_VALUE = "true";

    private static final String CONFIRMATION_METHOD = "urn:oasis:names:tc:SAML:1.0:cm:artifact";

    private final XSStringBuilder attrValueBuilder = new XSStringBuilder();

    /** The issuer, generally the hostname. */
    @NotNull
    private String issuer;

    /** The amount of time in milliseconds this is valid for. */
    @Min(1000)
    private long issueLength = 30000;

    @NotNull
    private String rememberMeAttributeName = REMEMBER_ME_ATTRIBUTE_NAME;

    @Override
    protected void prepareResponse(final Response response, final Map<String, Object> model) {
        final Authentication authentication = getAssertionFrom(model).getChainedAuthentications().get(0);
        final DateTime issuedAt = response.getIssueInstant();
        final Service service = getAssertionFrom(model).getService();
        final boolean isRemembered = (
                authentication.getAttributes().get(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME) == Boolean.TRUE
                        && !getAssertionFrom(model).isFromNewLogin());

        // Build up the SAML assertion containing AuthenticationStatement and AttributeStatement
        final Assertion assertion = newSamlObject(Assertion.class);
        assertion.setID(generateId());
        assertion.setIssueInstant(issuedAt);
        assertion.setIssuer(this.issuer);
        assertion.setConditions(newConditions(issuedAt, service.getId()));
        final AuthenticationStatement authnStatement = newAuthenticationStatement(authentication);
        assertion.getAuthenticationStatements().add(authnStatement);
        final Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
        if (!attributes.isEmpty() || isRemembered) {
            assertion.getAttributeStatements().add(
                    newAttributeStatement(newSubject(authentication.getPrincipal().getId()), attributes, isRemembered));
        }
        response.setStatus(newStatus(StatusCode.SUCCESS, null));
        response.getAssertions().add(assertion);
    }

    private Conditions newConditions(final DateTime issuedAt, final String serviceId) {
        final Conditions conditions = newSamlObject(Conditions.class);
        conditions.setNotBefore(issuedAt);
        conditions.setNotOnOrAfter(issuedAt.plus(this.issueLength));
        final AudienceRestrictionCondition audienceRestriction = newSamlObject(AudienceRestrictionCondition.class);
        final Audience audience = newSamlObject(Audience.class);
        audience.setUri(serviceId);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictionConditions().add(audienceRestriction);
        return conditions;
    }

    private Subject newSubject(final String identifier) {
        final SubjectConfirmation confirmation = newSamlObject(SubjectConfirmation.class);
        final ConfirmationMethod method = newSamlObject(ConfirmationMethod.class);
        method.setConfirmationMethod(CONFIRMATION_METHOD);
        confirmation.getConfirmationMethods().add(method);
        final NameIdentifier nameIdentifier = newSamlObject(NameIdentifier.class);
        nameIdentifier.setNameIdentifier(identifier);
        final Subject subject = newSamlObject(Subject.class);
        subject.setNameIdentifier(nameIdentifier);
        subject.setSubjectConfirmation(confirmation);
        return subject;
    }

    private AuthenticationStatement newAuthenticationStatement(final Authentication authentication) {
        final String authenticationMethod = (String) authentication.getAttributes().get(
                SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD);
        final AuthenticationStatement authnStatement = newSamlObject(AuthenticationStatement.class);
        authnStatement.setAuthenticationInstant(new DateTime(authentication.getAuthenticatedDate()));
        authnStatement.setAuthenticationMethod(
                authenticationMethod != null
                        ? authenticationMethod
                        : SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_UNSPECIFIED);
        authnStatement.setSubject(newSubject(authentication.getPrincipal().getId()));
        return authnStatement;
    }

    private AttributeStatement newAttributeStatement(
            final Subject subject, final Map<String, Object> attributes, final boolean isRemembered) {

        final AttributeStatement attrStatement = newSamlObject(AttributeStatement.class);
        attrStatement.setSubject(subject);
        for (final Entry<String, Object> e : attributes.entrySet()) {
            if (e.getValue() instanceof Collection<?> && ((Collection<?>) e.getValue()).isEmpty()) {
                // bnoordhuis: don't add the attribute, it causes a org.opensaml.MalformedException
                log.info("Skipping attribute {} because it does not have any values.", e.getKey());
                continue;
            }
            final Attribute attribute = newSamlObject(Attribute.class);
            attribute.setAttributeName(e.getKey());
            attribute.setAttributeNamespace(NAMESPACE);
            if (e.getValue() instanceof Collection<?>) {
                final Collection<?> c = (Collection<?>) e.getValue();
                for (final Object value : c) {
                    attribute.getAttributeValues().add(newAttributeValue(value));
                }
            } else {
                attribute.getAttributeValues().add(newAttributeValue(e.getValue()));
            }
            attrStatement.getAttributes().add(attribute);
        }

        if (isRemembered) {
            final Attribute attribute = newSamlObject(Attribute.class);
            attribute.setAttributeName(this.rememberMeAttributeName);
            attribute.setAttributeNamespace(NAMESPACE);
            attribute.getAttributeValues().add(newAttributeValue(REMEMBER_ME_ATTRIBUTE_VALUE));
            attrStatement.getAttributes().add(attribute);
        }
        return attrStatement;
    }

    private XSString newAttributeValue(final Object value) {
        final XSString stringValue = this.attrValueBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        if (value instanceof String) {
            stringValue.setValue((String) value);
        } else {
            stringValue.setValue(value.toString());
        }
        return stringValue;
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
