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

import org.jasig.cas.client.validation.Assertion;
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
import org.opensaml.saml.saml2.metadata.AttributeAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.AuthnAuthorityDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.PDPDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SSODescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
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
                          final HttpServletResponse response, final Assertion assertion) throws Exception {

        final List<Statement> statements = new ArrayList<>();
        statements.add(buildAuthnStatement(assertion, authnRequest));
        final AttributeStatement attributeStatement = buildAttributeStatement(assertion, authnRequest);
        if (attributeStatement != null) {
            statements.add(attributeStatement);
        }
        return buildResponse(authnRequest, statements);
    }

    private Issuer buildEntityIssuer() {
        final Issuer issuer = newIssuer(this.entityId);
        issuer.setFormat(Issuer.ENTITY);
        return issuer;
    }

    private NameID buildNameId(final AuthnRequest authnRequest) throws SAMLException {

        String requiredNameFormat = null;
        if (authnRequest.getNameIDPolicy() != null) {
            requiredNameFormat = authnRequest.getNameIDPolicy().getFormat();
            if (requiredNameFormat != null
                    && (requiredNameFormat.equals("urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted")
                    || requiredNameFormat.equals(NameID.UNSPECIFIED))) {
                requiredNameFormat = null;
            }
        }

        final List<String> supportedNameFormats = getNameFormats(authnRequest);
        if (requiredNameFormat != null) {
            supportedNameFormats.clear();
            supportedNameFormats.add(requiredNameFormat);
        }

        Map<String, BaseAttribute> principalAttributes = requestContext.getAttributes();
        if (principalAttributes == null || principalAttributes.isEmpty()) {
            if (requiredNameFormat != null) {
                requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI,
                        StatusCode.INVALID_NAMEID_POLICY_URI, "Format not supported: " + requiredNameFormat));
                throw new SAMLException(
                        "No attributes for principal, so NameID format required is not supported");
            }
            logger.debug("No attributes for principal {}, no name identifier will be created.", requestContext
                    .getPrincipalName());
            return null;
        }

        if (!supportedNameFormats.isEmpty()) {
            logger.debug("SP-supported name formats: {}", supportedNameFormats);
        } else {
            logger.debug("SP indicated no preferred name formats.");
        }

        try {
            SAML2NameIDEncoder nameIdEncoder;
            for (BaseAttribute<?> attribute : principalAttributes.values()) {
                for (AttributeEncoder encoder : attribute.getEncoders()) {
                    if (encoder instanceof SAML2NameIDEncoder) {
                        nameIdEncoder = (SAML2NameIDEncoder) encoder;
                        if (supportedNameFormats.isEmpty()
                                || supportedNameFormats.contains(nameIdEncoder.getNameFormat())) {
                            log.debug("Using attribute {} supporting NameID format {} to create the NameID.", attribute
                                    .getId(), nameIdEncoder.getNameFormat());
                            return nameIdEncoder.encode(attribute);
                        }
                    }
                }
            }

            if (requiredNameFormat != null) {
                requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI,
                        StatusCode.INVALID_NAMEID_POLICY_URI, "Format not supported: " + requiredNameFormat));
                throw new ProfileException(
                        "No attributes for principal support NameID format required by relying party");
            }
            logger.debug("No attributes for principal {} support an encoding into a supported name ID format.",
                    requestContext.getPrincipalName());
            return null;
        } catch (AttributeEncodingException e) {
            logger.error("Unable to encode NameID attribute", e);
            requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI, null, "Unable to construct NameID"));
            throw new SAMLException("Unable to encode NameID attribute", e);
        }
    }

    protected List<String> getNameFormats(final AuthnRequest authnRequest) throws SAMLException {
        final List<String> nameFormats = new ArrayList<>();

        final RoleDescriptor relyingPartyRole = requestContext.getPeerEntityRoleMetadata();
        if (relyingPartyRole != null) {
            final List<String> relyingPartySupportedFormats = getEntitySupportedFormats(relyingPartyRole);
            if (relyingPartySupportedFormats != null && !relyingPartySupportedFormats.isEmpty()) {
                nameFormats.addAll(relyingPartySupportedFormats);
            }
        }
        if (nameFormats.contains(NameID.UNSPECIFIED)) {
            nameFormats.clear();
        }

        return nameFormats;
    }

    private List<String> getEntitySupportedFormats(final RoleDescriptor role) {
        List<NameIDFormat> nameIDFormats = null;

        if (role instanceof SSODescriptor) {
            nameIDFormats = ((SSODescriptor) role).getNameIDFormats();
        } else if (role instanceof AuthnAuthorityDescriptor) {
            nameIDFormats = ((AuthnAuthorityDescriptor) role).getNameIDFormats();
        } else if (role instanceof PDPDescriptor) {
            nameIDFormats = ((PDPDescriptor) role).getNameIDFormats();
        } else if (role instanceof AttributeAuthorityDescriptor) {
            nameIDFormats = ((AttributeAuthorityDescriptor) role).getNameIDFormats();
        }

        final List<String> supportedFormats = new ArrayList<>();
        if (nameIDFormats != null) {
            for (final NameIDFormat format : nameIDFormats) {
                supportedFormats.add(format.getFormat());
            }
        }

        return supportedFormats;
    }


    protected Subject buildSubject(final AuthnRequest authnRequest, final String confirmationMethod,
                                   final DateTime issueInstant) throws SAMLException {
        final Subject subject = newSubj
        subject.getSubjectConfirmations().add(
                buildSubjectConfirmation(requestContext, confirmationMethod, issueInstant));

        NameID nameID = buildNameId(requestContext);
        if (nameID == null) {
            return subject;
        }

        boolean nameIdEncRequiredByAuthnRequest = false;
        if (requestContext.getInboundSAMLMessage() instanceof AuthnRequest) {
            AuthnRequest authnRequest = (AuthnRequest) requestContext.getInboundSAMLMessage();
            if (DatatypeHelper.safeEquals(DatatypeHelper.safeTrimOrNullString(authnRequest.getNameIDPolicy()
                    .getFormat()), NameID.ENCRYPTED)) {
                nameIdEncRequiredByAuthnRequest = true;
            }
        }

        SAMLMessageEncoder encoder = getMessageEncoders().get(requestContext.getPeerEntityEndpoint().getBinding());
        try {
            if (nameIdEncRequiredByAuthnRequest
                    || requestContext.getProfileConfiguration().getEncryptNameID() == CryptoOperationRequirementLevel.always
                    || (requestContext.getProfileConfiguration().getEncryptNameID() == CryptoOperationRequirementLevel.conditional && !encoder
                    .providesMessageConfidentiality(requestContext))) {
                log.debug("Attempting to encrypt NameID to relying party {}", requestContext.getInboundMessageIssuer());
                try {
                    Encrypter encrypter = getEncrypter(requestContext.getInboundMessageIssuer());
                    subject.setEncryptedID(encrypter.encrypt(nameID));
                } catch (SecurityException e) {
                    log.error("Unable to construct encrypter", e);
                    requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI, null,
                            "Unable to construct NameID"));
                    throw new ProfileException("Unable to construct encrypter", e);
                } catch (EncryptionException e) {
                    log.error("Unable to encrypt NameID", e);
                    requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI, null,
                            "Unable to construct NameID"));
                    throw new ProfileException("Unable to encrypt NameID", e);
                }
            } else {
                subject.setNameID(nameID);
            }
        } catch (MessageEncodingException e) {
            log.error("Unable to determine if outbound encoding {} can provide confidentiality", encoder
                    .getBindingURI());
            throw new ProfileException("Unable to determine if assertions should be encrypted");
        }

        return subject;
    }

    protected Response buildResponse(final AuthnRequest authnRequest,
                                     final List<Statement> statements) throws Exception {

        final String id = String.valueOf(new SecureRandom().nextLong());
        final Response samlResponse = newResponse(id, new DateTime(), authnRequest.getID(), null);
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        samlResponse.setIssuer(buildEntityIssuer());

        org.opensaml.saml.saml2.core.Assertion assertion = null;
        if (statements != null && !statements.isEmpty()) {
            assertion = newAssertion(statements, this.entityId, new DateTime(), id);


            assertion.setSubject(buildSubject(requestContext,  "urn:oasis:names:tc:SAML:2.0:cm:bearer", issueInstant));

            signAssertion(requestContext, assertion);

            SAMLMessageEncoder encoder = getMessageEncoders().get(requestContext.getPeerEntityEndpoint().getBinding());
            try {
                if (requestContext.getProfileConfiguration().getEncryptAssertion() == CryptoOperationRequirementLevel.always
                        || (requestContext.getProfileConfiguration().getEncryptAssertion() == CryptoOperationRequirementLevel.conditional && !encoder
                        .providesMessageConfidentiality(requestContext))) {
                    log.debug("Attempting to encrypt assertion to relying party {}", requestContext
                            .getInboundMessageIssuer());
                    try {
                        Encrypter encrypter = getEncrypter(requestContext.getInboundMessageIssuer());
                        samlResponse.getEncryptedAssertions().add(encrypter.encrypt(assertion));
                    } catch (SecurityException e) {
                        log.error("Unable to construct encrypter", e);
                        requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI, null,
                                "Unable to encrypt assertion"));
                        throw new ProfileException("Unable to construct encrypter", e);
                    } catch (EncryptionException e) {
                        log.error("Unable to encrypt assertion", e);
                        requestContext.setFailureStatus(buildStatus(StatusCode.RESPONDER_URI, null,
                                "Unable to encrypt assertion"));
                        throw new ProfileException("Unable to encrypt assertion", e);
                    }
                } else {
                    samlResponse.getAssertions().add(assertion);
                }
            } catch (MessageEncodingException e) {
                log.error("Unable to determine if outbound encoding {} can provide confidentiality", encoder
                        .getBindingURI());
                throw new ProfileException("Unable to determine if assertions should be encrypted");
            }
        }

        final Status status = newStatus(StatusCode., null, null);
        samlResponse.setStatus(status);

        return samlResponse;
    }

}
