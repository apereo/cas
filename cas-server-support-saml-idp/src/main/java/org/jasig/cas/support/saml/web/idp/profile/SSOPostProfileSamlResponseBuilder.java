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

package org.jasig.cas.support.saml.web.idp.profile;

import net.shibboleth.idp.attribute.IdPAttribute;
import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.StringAttributeValue;
import net.shibboleth.idp.saml.attribute.encoding.impl.SAML2StringNameIDEncoder;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.jasig.cas.support.saml.util.AbstractSaml20ObjectBuilder;
import org.jasig.cas.support.saml.web.idp.SamlResponseBuilder;
import org.joda.time.DateTime;
import org.opensaml.saml.common.SAMLException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectLocality;
import org.opensaml.security.credential.Credential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link SSOPostProfileSamlResponseBuilder} is responsible for...
 *
 * @author Misagh Moayyed
 */
@Component("ssoPostProfileSamlResponseBuilder")
public class SSOPostProfileSamlResponseBuilder extends AbstractSaml20ObjectBuilder implements SamlResponseBuilder {
    private static final long serialVersionUID = -1891703354216174875L;

    @Value("${cas.samlidp.entityid:}")
    private String entityId;

    private SubjectLocality buildSubjectLocality(final AuthnRequest authnRequest) throws Exception {
        final SubjectLocality subjectLocality = newSamlObject(SubjectLocality.class);
        final InetAddress address = InetAddress.getByName(
                new URL(authnRequest.getIssuer().getValue()).getHost());
        subjectLocality.setAddress(address.getHostAddress());
        return subjectLocality;
    }

    /**
     * Creates an authentication statement for the current request.
     *
     * @return constructed authentication statement
     */
    private AuthnStatement buildAuthnStatement(final Assertion assertion, final AuthnRequest authnRequest)
            throws Exception {
        final AuthnStatement statement = newAuthnStatement(getAuthenticationMethodFromAssertion(assertion),
                new DateTime(assertion.getAuthenticationDate());
        if (assertion.getValidUntilDate() != null) {
            statement.setSessionNotOnOrAfter(new DateTime(assertion.getValidUntilDate()));
        }
        statement.setSubjectLocality(buildSubjectLocality(authnRequest));
        return statement;
    }

    private String getAuthenticationMethodFromAssertion(final Assertion assertion) {
        return "";
    }

    private AttributeStatement buildAttributeStatement(final Assertion assertion, final AuthnRequest authnRequest)
            throws SAMLException {
        final Map<String, Object> attributes = new HashMap<>(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());
        return newAttributeStatement(attributes);
    }

    @Override
    public Response build(final AuthnRequest authnRequest, final HttpServletRequest request,
                          final HttpServletResponse response, final Assertion assertion,
                          final SamlRegisteredService service) throws Exception {

        final List<Statement> statements = new ArrayList<>();
        statements.add(buildAuthnStatement(assertion, authnRequest));
        final AttributeStatement attributeStatement = buildAttributeStatement(assertion, authnRequest);
        if (attributeStatement != null) {
            statements.add(attributeStatement);
        }
        return buildResponse(authnRequest, statements, assertion, service);
    }

    private Issuer buildEntityIssuer() {
        final Issuer issuer = newIssuer(this.entityId);
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

    private NameID buildNameId(final AuthnRequest authnRequest, final Assertion assertion,
                               final SamlRegisteredService service)
            throws SAMLException {

        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            if (requiredNameFormat != null  && (requiredNameFormat.equals(NameID.ENCRYPTED)
                    || requiredNameFormat.equals(NameID.UNSPECIFIED))) {
                requiredNameFormat = null;
            }
        }

        final List<String> supportedNameFormats = service.getSupportedNameFormats();
        if (requiredNameFormat != null) {
            supportedNameFormats.clear();
            supportedNameFormats.add(requiredNameFormat);
        }

        final Map<String, Object> principalAttributes = assertion.getPrincipal().getAttributes();
        if (principalAttributes == null || principalAttributes.isEmpty()) {
            if (requiredNameFormat != null) {
                throw new SAMLException("No attributes for principal, so NameID format required is not supported");
            }
            logger.debug("No attributes for principal {}, no name identifier will be created.",
                    assertion.getPrincipal().getName());
            return null;
        }

        if (!supportedNameFormats.isEmpty()) {
            logger.debug("SP-supported name formats: {}", supportedNameFormats);
        } else {
            logger.debug("SP indicated no preferred name formats.");
        }

        for (final String nameFormat : supportedNameFormats) {
            final SAML2StringNameIDEncoder encoder = new SAML2StringNameIDEncoder();
            encoder.setNameFormat(nameFormat);
            if (authnRequest.getNameIDPolicy() != null) {
                encoder.setNameQualifier(authnRequest.getNameIDPolicy().getSPNameQualifier());
            }
            final IdPAttribute attribute = new IdPAttribute(AttributePrincipal.class.getName());
            final IdPAttributeValue<String> value = new StringAttributeValue(assertion.getPrincipal().getName());
            attribute.setValues(Arrays.asList(value));
            try {
                return encoder.encode(attribute);
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private Subject buildSubject(final AuthnRequest authnRequest, final Assertion assertion,
                                   final SamlRegisteredService service) throws SAMLException {
        final NameID nameID = buildNameId(authnRequest, assertion, service);
        if (nameID == null) {
            throw new SAMLException("NameID cannot be determined for authN request");
        }

        final Subject subject = newSubject(nameID.getFormat(), nameID.getValue(),
                authnRequest.getAssertionConsumerServiceURL(),
                new DateTime(assertion.getValidFromDate()),
                authnRequest.getID());
        subject.setNameID(nameID);
        return subject;
    }

    private Response buildResponse(final AuthnRequest authnRequest,
                                   final List<Statement> statements,
                                   final Assertion casAssertion,
                                   final SamlRegisteredService service) throws Exception {

        final String id = String.valueOf(new SecureRandom().nextLong());
        final Response samlResponse = newResponse(id, new DateTime(), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());

        org.opensaml.saml.saml2.core.Assertion assertion = null;
        if (statements != null && !statements.isEmpty()) {
            assertion = newAssertion(statements, this.entityId, new DateTime(), id);


            assertion.setSubject(buildSubject(authnRequest, casAssertion, service));

            signAssertion(authnRequest, casAssertion, service);
            samlResponse.getAssertions().add(assertion);
        }

        final Status status = newStatus(StatusCode.SUCCESS, StatusCode.SUCCESS);
        samlResponse.setStatus(status);

        return samlResponse;
    }

    private void signAssertion(final AuthnRequest authnRequest,
                               final List<Statement> statements,
                               final Assertion casAssertion,
                               final SamlRegisteredService service)
            throws SAMLException {
        logger.debug("Determining if SAML assertion to {} should be signed", service.getServiceId());
        if (!service.isSignAssertions()) {
            return;
        }

        logger.debug("Determining signing credential for assertion to relying party {}", service.getServiceId());
        final Credential signatureCredential = service.getSigningCredential();

        if (signatureCredential == null) {
            throw new SAMLException("No signing credential is specified for relying party configuration");
        }

        logger.debug("Signing assertion to relying party {}", service.getServiceId());
        Signature signature = signatureBuilder.buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(signatureCredential);
        try {
            // TODO pull SecurityConfiguration from SAMLMessageContext? needs to be added
            // TODO how to pull what keyInfoGenName to use?
            SecurityHelper.prepareSignatureParams(signature, signatureCredential, null, null);
        } catch (SecurityException e) {
            throw new ProfileException("Error preparing signature for signing", e);
        }

        assertion.setSignature(signature);

        Marshaller assertionMarshaller = Configuration.getMarshallerFactory().getMarshaller(assertion);
        try {
            assertionMarshaller.marshall(assertion);
            Signer.signObject(signature);
        } catch (final Exception e) {
            logger.error("Unable to marshall assertion for signing", e);
            throw new SAMLException("Unable to marshall assertion for signing", e);
        }
    }

}
