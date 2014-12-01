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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.support.saml.authentication.principal.SamlService;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

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

            if (samlService.getRequestID() != null) {
                samlResponse.setInResponseTo(samlService.getRequestID());
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
        if (StringUtils.hasText(statusMessage)) {
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
     * Deflate the given bytes using zlib.
     *
     * @param bytes the bytes
     * @return the converted string
     */
    private static String zlibDeflate(final byte[] bytes) {
        final int bufferLength = 1024;
        final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final InflaterInputStream iis = new InflaterInputStream(bais);
        final byte[] buf = new byte[bufferLength];

        try {
            int count = iis.read(buf);
            while (count != -1) {
                baos.write(buf, 0, count);
                count = iis.read(buf);
            }
            return new String(baos.toByteArray());
        } catch (final Exception e) {
            return null;
        } finally {
            IOUtils.closeQuietly(iis);
        }
    }

    /**
     * Base64 decode.
     *
     * @param xml the xml
     * @return the byte[]
     */
    private static byte[] base64Decode(final String xml) {
        try {
            final byte[] xmlBytes = xml.getBytes("UTF-8");
            return Base64.decodeBase64(xmlBytes);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Inflate the given byte array.
     *
     * @param bytes the bytes
     * @return the string
     */
    private static String inflate(final byte[] bytes) {
        final int bufferLength = 10000;
        final Inflater inflater = new Inflater(true);
        final byte[] xmlMessageBytes = new byte[bufferLength];

        final byte[] extendedBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, extendedBytes, 0, bytes.length);
        extendedBytes[bytes.length] = 0;

        inflater.setInput(extendedBytes);

        try {
            final int resultLength = inflater.inflate(xmlMessageBytes);
            inflater.end();

            if (!inflater.finished()) {
                throw new RuntimeException("buffer not large enough.");
            }

            inflater.end();
            return new String(xmlMessageBytes, 0, resultLength, "UTF-8");
        } catch (final DataFormatException e) {
            return null;
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot find encoding: UTF-8", e);
        }
    }



    /**
     * Decode authn request xml.
     *
     * @param encodedRequestXmlString the encoded request xml string
     * @return the request
     */
    public String decodeSamlAuthnRequest(final String encodedRequestXmlString) {
        if (encodedRequestXmlString == null) {
            return null;
        }

        final byte[] decodedBytes = base64Decode(encodedRequestXmlString);
        if (decodedBytes == null) {
            return null;
        }

        final String inflated = inflate(decodedBytes);
        if (inflated != null) {
            return inflated;
        }

        return zlibDeflate(decodedBytes);
    }
}
