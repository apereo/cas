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

package org.jasig.cas.support.saml.util;


import org.apache.commons.lang.StringUtils;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.jasig.cas.util.CompressionUtils;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;

import java.security.SecureRandom;

/**
 * This is {@link AbstractSaml20ObjectBuilder}.
 * to build saml2 objects.
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public abstract class AbstractSaml20ObjectBuilder extends AbstractSamlObjectBuilder {
    private static final int HEX_HIGH_BITS_BITWISE_FLAG = 0x0f;

    /**
     * Gets name id.
     *
     * @param nameIdFormat the name id format
     * @param nameIdValue the name id value
     * @return the name iD
     */
    protected NameID getNameID(final String nameIdFormat, final String nameIdValue) {
        final NameID nameId = newSamlObject(NameID.class);
        nameId.setFormat(nameIdFormat);
        nameId.setValue(nameIdValue);
        return nameId;
    }

    /**
     * Create a new SAML response object.
     * @param id the id
     * @param issueInstant the issue instant
     * @param recipient the recipient
     * @param service the service
     * @return the response
     */
    public Response newResponse(final String id, final DateTime issueInstant,
                                final String recipient, final WebApplicationService service) {

        final Response samlResponse = newSamlObject(Response.class);
        samlResponse.setID(id);
        samlResponse.setIssueInstant(issueInstant);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        if (service instanceof SamlService) {
            final SamlService samlService = (SamlService) service;

            final String requestId = samlService.getRequestID();
            if (StringUtils.isNotBlank(requestId)) {
                samlResponse.setInResponseTo(requestId);
            }
        }
        return samlResponse;
    }

    /**
     * Create a new SAML status object.
     *
     * @param codeValue the code value
     * @param statusMessage the status message
     * @return the status
     */
    public Status newStatus(final String codeValue, final String statusMessage) {
        final Status status = newSamlObject(Status.class);
        final StatusCode code = newSamlObject(StatusCode.class);
        code.setValue(codeValue);
        status.setStatusCode(code);
        if (StringUtils.isNotBlank(statusMessage)) {
            final StatusMessage message = newSamlObject(StatusMessage.class);
            message.setMessage(statusMessage);
            status.setStatusMessage(message);
        }
        return status;
    }

    /**
     * Create a new SAML1 response object.
     *
     * @param authnStatement the authn statement
     * @param issuer the issuer
     * @param issuedAt the issued at
     * @param id the id
     * @return the assertion
     */
    public Assertion newAssertion(final AuthnStatement authnStatement, final String issuer,
                                  final DateTime issuedAt, final String id) {
        final Assertion assertion = newSamlObject(Assertion.class);
        assertion.setID(id);
        assertion.setIssueInstant(issuedAt);
        assertion.setIssuer(newIssuer(issuer));
        assertion.getAuthnStatements().add(authnStatement);
        return assertion;
    }

    /**
     * New issuer.
     *
     * @param issuerValue the issuer
     * @return the issuer
     */
    public Issuer newIssuer(final String issuerValue) {
        final Issuer issuer = newSamlObject(Issuer.class);
        issuer.setValue(issuerValue);
        return issuer;
    }

    /**
     * New authn statement.
     *
     * @param contextClassRef the context class ref such as {@link AuthnContext#PASSWORD_AUTHN_CTX}
     * @param authnInstant the authn instant
     * @return the authn statement
     */
    public AuthnStatement newAuthnStatement(final String contextClassRef, final DateTime authnInstant) {
        final AuthnStatement stmt = newSamlObject(AuthnStatement.class);
        final AuthnContext ctx = newSamlObject(AuthnContext.class);

        final AuthnContextClassRef classRef = newSamlObject(AuthnContextClassRef.class);
        classRef.setAuthnContextClassRef(contextClassRef);

        ctx.setAuthnContextClassRef(classRef);
        stmt.setAuthnContext(ctx);
        stmt.setAuthnInstant(authnInstant);

        return stmt;
    }

    /**
     * New conditions element.
     *
     * @param notBefore the not before
     * @param notOnOrAfter the not on or after
     * @param audienceUri the service id
     * @return the conditions
     */
    public Conditions newConditions(final DateTime notBefore, final DateTime notOnOrAfter, final String audienceUri) {
        final Conditions conditions = newSamlObject(Conditions.class);
        conditions.setNotBefore(notBefore);
        conditions.setNotOnOrAfter(notOnOrAfter);

        final AudienceRestriction audienceRestriction = newSamlObject(AudienceRestriction.class);
        final Audience audience = newSamlObject(Audience.class);
        audience.setAudienceURI(audienceUri);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    /**
     * New subject element.
     *
     * @param nameIdFormat the name id format
     * @param nameIdValue the name id value
     * @param recipient the recipient
     * @param notOnOrAfter the not on or after
     * @param inResponseTo the in response to
     * @return the subject
     */
    public Subject newSubject(final String nameIdFormat, final String nameIdValue,
                              final String recipient, final DateTime notOnOrAfter,
                              final String inResponseTo) {

        final SubjectConfirmation confirmation = newSamlObject(SubjectConfirmation.class);
        confirmation.setMethod(SubjectConfirmation.METHOD_BEARER);

        final SubjectConfirmationData data = newSamlObject(SubjectConfirmationData.class);
        data.setRecipient(recipient);
        data.setNotOnOrAfter(notOnOrAfter);
        data.setInResponseTo(inResponseTo);

        confirmation.setSubjectConfirmationData(data);

        final Subject subject = newSamlObject(Subject.class);
        subject.setNameID(getNameID(nameIdFormat, nameIdValue));
        subject.getSubjectConfirmations().add(confirmation);
        return subject;
    }

    @Override
    public String generateSecureRandomId() {
        final SecureRandom generator = new SecureRandom();
        final char[] charMappings = {
                'a', 'b', 'c', 'd', 'e', 'f', 'g',
                'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                'p'};

        final int charsLength = 40;
        final int generatorBytesLength = 20;
        final int shiftLength = 4;

        // 160 bits
        final byte[] bytes = new byte[generatorBytesLength];
        generator.nextBytes(bytes);

        final char[] chars = new char[charsLength];
        for (int i = 0; i < bytes.length; i++) {
            final int left = bytes[i] >> shiftLength & HEX_HIGH_BITS_BITWISE_FLAG;
            final int right = bytes[i] & HEX_HIGH_BITS_BITWISE_FLAG;
            chars[i * 2] = charMappings[left];
            chars[i * 2 + 1] = charMappings[right];
        }
        return String.valueOf(chars);
    }

    /**
     * Decode authn request xml.
     *
     * @param encodedRequestXmlString the encoded request xml string
     * @return the request
     */
    public String decodeSamlAuthnRequest(final String encodedRequestXmlString) {
        if (StringUtils.isEmpty(encodedRequestXmlString)) {
            return null;
        }

        logger.debug("Decoding SAML authN request: {}", encodedRequestXmlString);

        final byte[] decodedBytes = CompressionUtils.decodeBase64ToByteArray(encodedRequestXmlString);
        if (decodedBytes == null) {
            return null;
        }

        final String inflated = CompressionUtils.inflate(decodedBytes);
        logger.debug("Inflated SAML authN request: {}", inflated);

        if (!StringUtils.isEmpty(inflated)) {
            return inflated;
        }

        return CompressionUtils.decodeByteArrayToString(decodedBytes);
    }
}
