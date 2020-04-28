package org.apereo.cas.support.saml.util;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.RandomUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameID;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AbstractSaml20ObjectBuilder}.
 * to build saml2 objects.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public abstract class AbstractSaml20ObjectBuilder extends AbstractSamlObjectBuilder {
    private static final long serialVersionUID = -4325127376598205277L;

    public AbstractSaml20ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    private static void configureAttributeNameFormat(final Attribute attribute, final String nameFormat) {
        LOGGER.trace("Configuring Attribute's: [{}] nameFormat: [{}]", attribute, nameFormat);
        if (StringUtils.isBlank(nameFormat)) {
            return;
        }

        val compareFormat = nameFormat.trim().toLowerCase();
        switch (compareFormat) {
            case "basic":
            case Attribute.BASIC:
                attribute.setNameFormat(Attribute.BASIC);
                break;
            case "uri":
            case Attribute.URI_REFERENCE:
                attribute.setNameFormat(Attribute.URI_REFERENCE);
                break;
            case "unspecified":
            case Attribute.UNSPECIFIED:
                attribute.setNameFormat(Attribute.UNSPECIFIED);
                break;
            default:
                attribute.setNameFormat(nameFormat);
                break;
        }
    }

    /**
     * Gets name id.
     *
     * @param nameIdFormat the name id format
     * @param nameIdValue  the name id value
     * @return the name iD
     */
    public NameID getNameID(final String nameIdFormat, final String nameIdValue) {
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

        LOGGER.trace("Creating Response instance for id: [{}], issueInstant: [{}}], recipient: [{}], service: [{}]",
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

    /**
     * Create a new SAML status object.
     *
     * @param codeValue     the code value
     * @param statusMessage the status message
     * @return the status
     */
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
     * Create a new SAML2 Assertion object.
     *
     * @param authnStatement the authn statement
     * @param issuer         the issuer
     * @param issuedAt       the issued at
     * @param id             the id
     * @return the assertion
     */
    public Assertion newAssertion(final AuthnStatement authnStatement, final String issuer,
                                  final ZonedDateTime issuedAt, final String id) {
        val list = new ArrayList<Statement>(1);
        list.add(authnStatement);
        return newAssertion(list, issuer, issuedAt, id);
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

    /**
     * New saml2 logout request.
     *
     * @param id           the id
     * @param issueInstant the issue instant
     * @param destination  the destination
     * @param issuer       the issuer
     * @param sessionIndex the session index
     * @param nameId       the name id
     * @return the logout request
     */
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
     * New attribute.
     *
     * @param attributeFriendlyName the attribute friendly name
     * @param attributeName         the attribute name
     * @param attributeValue        the attribute value
     * @param configuredNameFormats the configured name formats. If an attribute is found in this collection, the linked name format will be used.
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

    /**
     * New authn statement.
     *
     * @param contextClassRef the context class ref such as {@link AuthnContext#PASSWORD_AUTHN_CTX}
     * @param authnInstant    the authn instant
     * @param sessionIndex    the session index
     * @return the authn statement
     */
    public AuthnStatement newAuthnStatement(final String contextClassRef, final ZonedDateTime authnInstant,
                                            final String sessionIndex) {
        LOGGER.trace("Building authentication statement with context class ref [{}] @ [{}] with index [{}]",
            contextClassRef, authnInstant, sessionIndex);
        val stmt = newSamlObject(AuthnStatement.class);
        val ctx = newSamlObject(AuthnContext.class);

        val classRef = newSamlObject(AuthnContextClassRef.class);
        classRef.setURI(contextClassRef);

        ctx.setAuthnContextClassRef(classRef);
        stmt.setAuthnContext(ctx);
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
     * New subject subject.
     *
     * @param nameIdFormat the name id format
     * @param nameIdValue  the name id value
     * @param recipient    the recipient
     * @param notOnOrAfter the not on or after
     * @param inResponseTo the in response to
     * @param notBefore    the not before
     * @return the subject
     */
    public Subject newSubject(final String nameIdFormat, final String nameIdValue,
                              final String recipient, final ZonedDateTime notOnOrAfter,
                              final String inResponseTo, final ZonedDateTime notBefore) {
        val nameID = getNameID(nameIdFormat, nameIdValue);
        return newSubject(nameID, null, recipient, notOnOrAfter, inResponseTo, notBefore);
    }

    /**
     * New subject element.
     *
     * @param nameId            the nameId
     * @param subjectConfNameId the subject conf name id
     * @param recipient         the recipient
     * @param notOnOrAfter      the not on or after
     * @param inResponseTo      the in response to
     * @param notBefore         the not before
     * @return the subject
     */
    public Subject newSubject(final NameID nameId,
                              final NameID subjectConfNameId,
                              final String recipient,
                              final ZonedDateTime notOnOrAfter,
                              final String inResponseTo,
                              final ZonedDateTime notBefore) {

        LOGGER.debug("Building subject for NameID [{}] and recipient [{}], in response to [{}]", nameId, recipient, inResponseTo);
        val confirmation = newSamlObject(SubjectConfirmation.class);
        confirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        val data = newSamlObject(SubjectConfirmationData.class);

        if (StringUtils.isNotBlank(recipient)) {
            data.setRecipient(recipient);
        }

        if (notOnOrAfter != null) {
            data.setNotOnOrAfter(notOnOrAfter.toInstant());
        }

        if (StringUtils.isNotBlank(inResponseTo)) {
            data.setInResponseTo(inResponseTo);

            val ip = InetAddressUtils.getByName(inResponseTo);
            if (ip != null) {
                data.setAddress(ip.getHostName());
            }

        }

        if (notBefore != null) {
            data.setNotBefore(notBefore.toInstant());
        }

        confirmation.setSubjectConfirmationData(data);

        val subject = newSamlObject(Subject.class);
        if (nameId != null) {
            subject.setNameID(nameId);

            if (subjectConfNameId != null) {
                confirmation.setNameID(subjectConfNameId);
            }
            subject.setEncryptedID(null);
            confirmation.setEncryptedID(null);
        }
        subject.getSubjectConfirmations().add(confirmation);

        LOGGER.debug("Built subject [{}]", subject);
        return subject;
    }

    @Override
    public String generateSecureRandomId() {
        return RandomUtils.generateSecureRandomId();
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

    /**
     * Inflate authn request string.
     *
     * @param decodedBytes the decoded bytes
     * @return the string
     */
    protected String inflateAuthnRequest(final byte[] decodedBytes) {
        val inflated = CompressionUtils.inflate(decodedBytes);
        if (!StringUtils.isEmpty(inflated)) {
            return inflated;
        }
        return CompressionUtils.decodeByteArrayToString(decodedBytes);
    }
}
