package org.apereo.cas.support.saml.util;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;
import org.apereo.cas.util.DateTimeUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml1.binding.encoding.impl.HTTPSOAP11Encoder;
import org.opensaml.saml.saml1.core.Assertion;
import org.opensaml.saml.saml1.core.Attribute;
import org.opensaml.saml.saml1.core.AttributeStatement;
import org.opensaml.saml.saml1.core.AttributeValue;
import org.opensaml.saml.saml1.core.Audience;
import org.opensaml.saml.saml1.core.AudienceRestrictionCondition;
import org.opensaml.saml.saml1.core.AuthenticationStatement;
import org.opensaml.saml.saml1.core.Conditions;
import org.opensaml.saml.saml1.core.ConfirmationMethod;
import org.opensaml.saml.saml1.core.NameIdentifier;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.Status;
import org.opensaml.saml.saml1.core.StatusCode;
import org.opensaml.saml.saml1.core.StatusMessage;
import org.opensaml.saml.saml1.core.Subject;
import org.opensaml.saml.saml1.core.SubjectConfirmation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is the response builder for Saml1 Protocol.
 *
 * @author Misagh Moayyed mmoayyed@unicon.net
 * @since 4.1
 */
public class Saml10ObjectBuilder extends AbstractSamlObjectBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(Saml10ObjectBuilder.class);

    private static final String CONFIRMATION_METHOD = "urn:oasis:names:tc:SAML:1.0:cm:artifact";
    private static final long serialVersionUID = -4711012620700270554L;

    public Saml10ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
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

        final Response samlResponse = newSamlObject(Response.class);
        samlResponse.setID(id);
        samlResponse.setIssueInstant(DateTimeUtils.dateTimeOf(issueInstant));
        samlResponse.setVersion(SAMLVersion.VERSION_11);
        samlResponse.setInResponseTo(recipient);
        setInResponseToForSamlResponseIfNeeded(service, samlResponse);
        return samlResponse;
    }

    /**
     * Sets in response to for saml 1 response.
     *
     * @param service      the service
     * @param samlResponse the saml 1 response
     */
    private static void setInResponseToForSamlResponseIfNeeded(final Service service, final Response samlResponse) {
        if (service instanceof SamlService) {
            final SamlService samlService = (SamlService) service;
            final String requestId = samlService.getRequestID();
            if (StringUtils.isNotBlank(requestId)) {
                samlResponse.setInResponseTo(requestId);
            }
        }
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
    public Assertion newAssertion(final AuthenticationStatement authnStatement, final String issuer,
                                  final ZonedDateTime issuedAt, final String id) {
        final Assertion assertion = newSamlObject(Assertion.class);

        assertion.setID(id);
        assertion.setIssueInstant(DateTimeUtils.dateTimeOf(issuedAt));
        assertion.setIssuer(issuer);
        assertion.getAuthenticationStatements().add(authnStatement);
        return assertion;
    }

    /**
     * New conditions element.
     *
     * @param issuedAt    the issued at
     * @param audienceUri the service id
     * @param issueLength the issue length
     * @return the conditions
     */
    public Conditions newConditions(final ZonedDateTime issuedAt, final String audienceUri, final long issueLength) {
        final Conditions conditions = newSamlObject(Conditions.class);
        conditions.setNotBefore(DateTimeUtils.dateTimeOf(issuedAt));
        conditions.setNotOnOrAfter(DateTimeUtils.dateTimeOf(issuedAt.plus(issueLength, ChronoUnit.SECONDS)));
        final AudienceRestrictionCondition audienceRestriction = newSamlObject(AudienceRestrictionCondition.class);
        final Audience audience = newSamlObject(Audience.class);
        audience.setUri(audienceUri);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictionConditions().add(audienceRestriction);
        return conditions;
    }

    /**
     * New status status.
     *
     * @param codeValue the code value
     * @return the status
     */
    public Status newStatus(final QName codeValue) {
        return newStatus(codeValue, StringUtils.EMPTY);
    }

    /**
     * Create a new SAML status object.
     *
     * @param codeValue     the code value
     * @param statusMessage the status message
     * @return the status
     */
    public Status newStatus(final QName codeValue, final String statusMessage) {
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
     * New authentication statement.
     *
     * @param authenticationDate   the authentication date
     * @param authenticationMethod the authentication method
     * @param subjectId            the subject id
     * @return the authentication statement
     */
    public AuthenticationStatement newAuthenticationStatement(final ZonedDateTime authenticationDate,
                                                              final Collection<Object> authenticationMethod,
                                                              final String subjectId) {

        final AuthenticationStatement authnStatement = newSamlObject(AuthenticationStatement.class);
        authnStatement.setAuthenticationInstant(DateTimeUtils.dateTimeOf(authenticationDate));

        authnStatement.setAuthenticationMethod(
                authenticationMethod != null && !authenticationMethod.isEmpty()
                        ? authenticationMethod.iterator().next().toString()
                        : SamlAuthenticationMetaDataPopulator.AUTHN_METHOD_UNSPECIFIED);
        authnStatement.setSubject(newSubject(subjectId));
        return authnStatement;
    }

    /**
     * New subject element that uses the confirmation method
     * {@link #CONFIRMATION_METHOD}.
     *
     * @param identifier the identifier
     * @return the subject
     */
    public Subject newSubject(final String identifier) {
        return newSubject(identifier, CONFIRMATION_METHOD);
    }

    /**
     * New subject element with given confirmation method.
     *
     * @param identifier         the identifier
     * @param confirmationMethod the confirmation method
     * @return the subject
     */
    public Subject newSubject(final String identifier, final String confirmationMethod) {
        final SubjectConfirmation confirmation = newSamlObject(SubjectConfirmation.class);
        final ConfirmationMethod method = newSamlObject(ConfirmationMethod.class);
        method.setConfirmationMethod(confirmationMethod);
        confirmation.getConfirmationMethods().add(method);
        final NameIdentifier nameIdentifier = newSamlObject(NameIdentifier.class);
        nameIdentifier.setNameIdentifier(identifier);
        final Subject subject = newSamlObject(Subject.class);
        subject.setNameIdentifier(nameIdentifier);
        subject.setSubjectConfirmation(confirmation);
        return subject;
    }

    /**
     * Add saml1 attribute values for attribute.
     *
     * @param attributeName  the attribute name
     * @param attributeValue the attribute value
     * @param attributeList  the attribute list
     */
    public void addAttributeValuesToSaml1Attribute(final String attributeName,
                                                   final Object attributeValue,
                                                   final List<XMLObject> attributeList) {
        addAttributeValuesToSamlAttribute(attributeName, attributeValue, attributeList, AttributeValue.DEFAULT_ELEMENT_NAME);
    }

    /**
     * New attribute statement.
     *
     * @param subject            the subject
     * @param attributes         the attributes
     * @param attributeNamespace the attribute namespace
     * @return the attribute statement
     */
    public AttributeStatement newAttributeStatement(final Subject subject,
                                                    final Map<String, Object> attributes,
                                                    final String attributeNamespace) {

        final AttributeStatement attrStatement = newSamlObject(AttributeStatement.class);
        attrStatement.setSubject(subject);
        for (final Map.Entry<String, Object> e : attributes.entrySet()) {
            if (e.getValue() instanceof Collection<?> && ((Collection<?>) e.getValue()).isEmpty()) {
                LOGGER.info("Skipping attribute [{}] because it does not have any values.", e.getKey());
                continue;
            }
            final Attribute attribute = newSamlObject(Attribute.class);
            attribute.setAttributeName(e.getKey());

            if (StringUtils.isNotBlank(attributeNamespace)) {
                attribute.setAttributeNamespace(attributeNamespace);
            }

            addAttributeValuesToSaml1Attribute(e.getKey(), e.getValue(), attribute.getAttributeValues());
            attrStatement.getAttributes().add(attribute);
        }

        return attrStatement;
    }

    /**
     * Encode response and pass it onto the outbound transport.
     * Uses {@link CasHttpSoap11Encoder} to handle encoding.
     *
     * @param httpResponse the http response
     * @param httpRequest  the http request
     * @param samlMessage  the saml response
     * @throws Exception the exception in case encoding fails.
     */
    public void encodeSamlResponse(final HttpServletResponse httpResponse,
                                   final HttpServletRequest httpRequest,
                                   final Response samlMessage) throws Exception {

        SamlUtils.logSamlObject(this.configBean, samlMessage);

        final HTTPSOAP11Encoder encoder = new CasHttpSoap11Encoder();
        final MessageContext<SAMLObject> context = new MessageContext();
        context.setMessage(samlMessage);
        encoder.setHttpServletResponse(httpResponse);
        encoder.setMessageContext(context);
        encoder.initialize();
        encoder.prepareContext();
        encoder.encode();
    }
}
