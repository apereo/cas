package org.apereo.cas.support.saml.util;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.ElementExtensibleXMLObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedID;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.RequesterID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.SessionIndex;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.StatusMessage;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.soap.soap11.ActorBearing;
import java.net.InetAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link AbstractSaml20ObjectBuilder}.
 * to build saml2 objects.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public abstract class AbstractSaml20ObjectBuilder extends AbstractSamlObjectBuilder implements Saml20ObjectBuilder {

    protected AbstractSaml20ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    private static void configureAttributeNameFormat(final Attribute attribute, final String nameFormat) {
        LOGGER.trace("Configuring Attribute's: [{}] nameFormat: [{}]", attribute, nameFormat);
        if (StringUtils.isBlank(nameFormat)) {
            return;
        }

        val compareFormat = nameFormat.trim().toLowerCase(Locale.ENGLISH);
        switch (compareFormat) {
            case "basic", Attribute.BASIC -> attribute.setNameFormat(Attribute.BASIC);
            case "uri", Attribute.URI_REFERENCE -> attribute.setNameFormat(Attribute.URI_REFERENCE);
            case "unspecified", Attribute.UNSPECIFIED -> attribute.setNameFormat(Attribute.UNSPECIFIED);
            default -> attribute.setNameFormat(nameFormat);
        }
    }

    @Override
    public NameID newNameID(final String nameIdFormat, final String nameIdValue) {
        val nameId = newSamlObject(NameID.class);
        nameId.setFormat(nameIdFormat);
        nameId.setValue(nameIdValue);
        return nameId;
    }

    /**
     * Create a new SAML ECP response object.
     *
     * @param assertionConsumerUrl the assertion consumer url
     * @return the response
     */
    public org.opensaml.saml.saml2.ecp.Response newEcpResponse(final String assertionConsumerUrl) {
        val samlResponse = newSamlObject(org.opensaml.saml.saml2.ecp.Response.class);
        samlResponse.setSOAP11MustUnderstand(Boolean.TRUE);
        samlResponse.setSOAP11Actor(ActorBearing.SOAP11_ACTOR_NEXT);
        samlResponse.setAssertionConsumerServiceURL(assertionConsumerUrl);
        return samlResponse;
    }

    /**
     * Create a new SAML response object.
     *
     * @param id           the id
     * @param issueInstant the issue instant
     * @param recipient    the recipient
     * @param service      the service
     * @return the response
     */
    public Response newResponse(final String id, final ZonedDateTime issueInstant,
                                final String recipient, final WebApplicationService service) {

        LOGGER.trace("Creating Response instance for id: [{}], issueInstant: [{}]], recipient: [{}], service: [{}]",
            id, issueInstant, recipient, service);
        val samlResponse = newSamlObject(Response.class);
        samlResponse.setID(id);
        samlResponse.setIssueInstant(issueInstant.toInstant());
        samlResponse.setVersion(SAMLVersion.VERSION_20);
        if (StringUtils.isNotBlank(recipient)) {
            LOGGER.debug("Setting provided RequestId [{}] as InResponseTo", recipient);
            samlResponse.setInResponseTo(recipient);
        } else {
            LOGGER.debug("No recipient is provided. Skipping InResponseTo");
        }
        return samlResponse;
    }

    @Override
    public Status newStatus(final String codeValue, final String statusMessage) {
        LOGGER.trace("Creating new SAML Status for code value: [{}], status message: [{}]", codeValue, statusMessage);
        val status = newSamlObject(Status.class);
        val statusCode = newSamlObject(StatusCode.class);
        statusCode.setValue(codeValue);
        status.setStatusCode(statusCode);
        if (StringUtils.isNotBlank(statusMessage)) {
            val message = newSamlObject(StatusMessage.class);
            message.setValue(statusMessage);
            status.setStatusMessage(message);
        }
        return status;
    }

    /**
     * Create a new SAML1 response object.
     *
     * @param authnStatement the authn statement
     * @param issuer         the issuer
     * @param issuedAt       the issued at
     * @param id             the id
     * @return the assertion
     */
    public Assertion newAssertion(final List<Statement> authnStatement, final String issuer,
                                  final ZonedDateTime issuedAt, final String id) {
        LOGGER.trace("Creating new SAML Assertion with id: [{}], for issuer: [{}], issued at: [{}]", id, issuer, issuedAt);
        val assertion = newSamlObject(Assertion.class);
        assertion.setID(id);
        assertion.setIssueInstant(issuedAt.toInstant());
        assertion.setIssuer(newIssuer(issuer));
        assertion.getStatements().addAll(authnStatement);
        return assertion;
    }

    @Override
    public LogoutResponse newLogoutResponse(final String id, final String destination,
                                            final Issuer issuer, final Status status,
                                            final String recipient) {
        val logoutResponse = newSamlObject(LogoutResponse.class);
        logoutResponse.setIssuer(issuer);
        logoutResponse.setIssueInstant(Instant.now(Clock.systemUTC()));
        logoutResponse.setID(id);
        logoutResponse.setDestination(destination);
        logoutResponse.setInResponseTo(recipient);
        logoutResponse.setStatus(status);
        logoutResponse.setVersion(SAMLVersion.VERSION_20);
        return logoutResponse;
    }
    
    @Override
    public LogoutRequest newLogoutRequest(final String id, final ZonedDateTime issueInstant,
                                          final String destination, final Issuer issuer,
                                          final String sessionIndex, final NameID nameId) {

        LOGGER.trace("Creating new SAML LogoutRequest with id: [{}], for issuer: [{}], for destination: [{}], for NameID: [{}],issued at: [{}]",
            id, issuer, destination, nameId, issueInstant);
        val request = newSamlObject(LogoutRequest.class);
        request.setID(id);
        request.setVersion(SAMLVersion.VERSION_20);
        request.setIssueInstant(issueInstant.toInstant());
        request.setIssuer(issuer);
        request.setDestination(destination);

        if (StringUtils.isNotBlank(sessionIndex)) {
            val sessionIdx = newSamlObject(SessionIndex.class);
            sessionIdx.setValue(sessionIndex);
            request.getSessionIndexes().add(sessionIdx);
        }

        if (nameId != null) {
            request.setNameID(nameId);
        }
        return request;
    }

    /**
     * New issuer.
     *
     * @param issuerValue the issuer
     * @return the issuer
     */
    @Override
    public Issuer newIssuer(final String issuerValue) {
        val issuer = newSamlObject(Issuer.class);
        issuer.setValue(issuerValue);
        return issuer;
    }

    /**
     * Add saml2 attribute values for attribute.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @param valueType      the value type
     * @param attributeList  the attribute list
     */
    public void addAttributeValuesToSaml2Attribute(final String attributeName,
                                                   final Object attributeValue,
                                                   final String valueType,
                                                   final List<XMLObject> attributeList) {
        addAttributeValuesToSamlAttribute(attributeName, attributeValue, valueType,
            attributeList, AttributeValue.DEFAULT_ELEMENT_NAME);
    }

    /**
     * New authn statement authn statement.
     *
     * @param context      the context
     * @param authnInstant the authn instant
     * @param sessionIndex the session index
     * @return the authn statement
     */
    public AuthnStatement newAuthnStatement(final AuthnContext context,
                                            final ZonedDateTime authnInstant,
                                            final String sessionIndex) {
        LOGGER.trace("Building authentication statement with context class ref [{}] @ [{}] with index [{}]",
            context, authnInstant, sessionIndex);
        val stmt = newSamlObject(AuthnStatement.class);
        stmt.setAuthnContext(context);
        stmt.setAuthnInstant(authnInstant.toInstant());
        stmt.setSessionIndex(sessionIndex);
        return stmt;
    }

    /**
     * New conditions element.
     *
     * @param notBefore    the not before
     * @param notOnOrAfter the not on or after
     * @param audienceUri  the service id
     * @return the conditions
     */
    public Conditions newConditions(final ZonedDateTime notBefore, final ZonedDateTime notOnOrAfter, final String... audienceUri) {
        LOGGER.debug("Building conditions for audience [{}] that enforce not-before [{}] and not-after [{}]", audienceUri, notBefore, notOnOrAfter);
        val conditions = newSamlObject(Conditions.class);
        conditions.setNotBefore(notBefore.toInstant());
        conditions.setNotOnOrAfter(notOnOrAfter.toInstant());

        val audienceRestriction = newSamlObject(AudienceRestriction.class);
        Arrays.stream(audienceUri).forEach(audienceEntry -> {
            val audience = newSamlObject(Audience.class);
            audience.setURI(audienceEntry);
            audienceRestriction.getAudiences().add(audience);
        });
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

    /**
     * New subject confirmation.
     *
     * @param recipient    the recipient
     * @param notOnOrAfter the not on or after
     * @param inResponseTo the in response to
     * @param notBefore    the not before
     * @param address      the address
     * @return the subject confirmation
     */
    public SubjectConfirmation newSubjectConfirmation(final String recipient, final ZonedDateTime notOnOrAfter,
                                                      final String inResponseTo, final ZonedDateTime notBefore,
                                                      final InetAddress address) {
        LOGGER.debug("Building subject confirmation for recipient [{}], in response to [{}]", recipient, inResponseTo);
        val confirmation = newSamlObject(SubjectConfirmation.class);
        confirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        val data = newSamlObject(SubjectConfirmationData.class);
        FunctionUtils.doIfNotBlank(recipient, data::setRecipient);
        FunctionUtils.doIfNotBlank(inResponseTo, data::setInResponseTo);
        FunctionUtils.doIfNotNull(address, __ -> data.setAddress(address.getHostAddress()));
        FunctionUtils.doIfNotNull(notOnOrAfter, __ -> data.setNotOnOrAfter(notOnOrAfter.toInstant()));
        FunctionUtils.doIfNotNull(notBefore, __ -> data.setNotBefore(notBefore.toInstant()));
        confirmation.setSubjectConfirmationData(data);
        return confirmation;
    }

    /**
     * New subject element.
     *
     * @param nameId              the nameId
     * @param subjectConfNameId   the subject conf name id
     * @param subjectConfirmation the subject confirmation
     * @return the subject
     */
    public Subject newSubject(final SAMLObject nameId,
                              final SAMLObject subjectConfNameId,
                              final SubjectConfirmation subjectConfirmation) {

        LOGGER.debug("Building subject for NameID [{}]", nameId);
        val subject = newSamlObject(Subject.class);
        subject.setNameID(null);
        subject.getSubjectConfirmations().forEach(confirmation -> confirmation.setNameID(null));

        if (nameId instanceof final NameID instance) {
            subject.setNameID(instance);
            subject.setEncryptedID(null);
        }
        if (nameId instanceof final EncryptedID instance) {
            subject.setNameID(null);
            subject.setEncryptedID(instance);
        }
        if (subjectConfNameId instanceof final NameID instance) {
            subjectConfirmation.setNameID(instance);
            subjectConfirmation.setEncryptedID(null);
        }
        if (subjectConfNameId instanceof final EncryptedID instance) {
            subjectConfirmation.setNameID(null);
            subjectConfirmation.setEncryptedID(instance);
        }
        subject.getSubjectConfirmations().add(subjectConfirmation);
        LOGGER.debug("Built subject [{}]", subject);
        return subject;
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

        val decodedBytes = EncodingUtils.decodeBase64(encodedRequestXmlString);
        return inflateAuthnRequest(decodedBytes);
    }

    @Override
    public <T extends SAMLObject> T newSamlObject(final Class<T> objectType) {
        val qName = getSamlObjectQName(objectType);
        return SamlUtils.newSamlObject(objectType, qName);
    }


    /**
     * New attribute.
     *
     * @param attributeFriendlyName the attribute friendly name
     * @param attributeName         the attribute name
     * @param attributeValue        the attribute value
     * @param configuredNameFormats the configured name formats. If an attribute is
     *                              found in this collection, the linked name format will be used.
     * @param defaultNameFormat     the default name format
     * @param attributeValueTypes   the attribute value types
     * @return the attribute
     */
    protected Attribute newAttribute(final String attributeFriendlyName,
                                     final String attributeName,
                                     final Object attributeValue,
                                     final Map<String, String> configuredNameFormats,
                                     final String defaultNameFormat,
                                     final Map<String, String> attributeValueTypes) {
        val attribute = newSamlObject(Attribute.class);
        attribute.setName(attributeName);

        if (StringUtils.isNotBlank(attributeFriendlyName)) {
            attribute.setFriendlyName(attributeFriendlyName);
        } else {
            attribute.setFriendlyName(attributeName);
        }

        val valueType = attributeValueTypes.get(attributeName);
        addAttributeValuesToSaml2Attribute(attributeName, attributeValue, valueType, attribute.getAttributeValues());

        if (!configuredNameFormats.isEmpty() && configuredNameFormats.containsKey(attribute.getName())) {
            val nameFormat = configuredNameFormats.get(attribute.getName());
            LOGGER.debug("Found name format [{}] for attribute [{}]", nameFormat, attribute.getName());
            configureAttributeNameFormat(attribute, nameFormat);
            LOGGER.debug("Attribute [{}] is assigned the name format of [{}]", attribute.getName(), attribute.getNameFormat());
        } else {
            LOGGER.debug("Skipped name format, as no name formats are defined or none is found for attribute [{}]", attribute.getName());
            configureAttributeNameFormat(attribute, defaultNameFormat);
        }

        LOGGER.debug("Attribute [{}] has [{}] value(s)", attribute.getName(), attribute.getAttributeValues().size());
        return attribute;
    }

    protected String inflateAuthnRequest(final byte[] decodedBytes) {
        val inflated = CompressionUtils.inflate(decodedBytes);
        if (!StringUtils.isEmpty(inflated)) {
            return inflated;
        }
        return CompressionUtils.inflateToString(decodedBytes);
    }

    protected String getInResponseTo(final RequestAbstractType request, final String entityId, final boolean skipInResponseTo) {
        var generateInResponseTo = !skipInResponseTo && StringUtils.isNotBlank(request.getID());
        if (generateInResponseTo && request.getExtensions() != null) {
            val extensions = Optional.ofNullable(request.getExtensions())
                .map(ElementExtensibleXMLObject::getUnknownXMLObjects).orElseGet(List::of);
            generateInResponseTo = extensions
                .stream()
                .filter(RequesterID.class::isInstance)
                .map(RequesterID.class::cast)
                .noneMatch(info -> entityId.equalsIgnoreCase(info.getURI()));
        }
        return generateInResponseTo ? request.getID() : null;
    }
}
