package org.apereo.cas.support.saml.util;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.authentication.SamlAuthenticationMetaDataPopulator;
import org.apereo.cas.support.saml.authentication.principal.SamlService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.SAMLVersion;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This is the response builder for Saml1 Protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class Saml10ObjectBuilder extends AbstractSamlObjectBuilder {
    private static final String CONFIRMATION_METHOD = "urn:oasis:names:tc:SAML:1.0:cm:artifact";

    public Saml10ObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }

    /**
     * Sets in response to for saml 1 response.
     *
     * @param service      the service
     * @param samlResponse the saml 1 response
     */
    private static void setInResponseToForSamlResponseIfNeeded(final Service service, final Response samlResponse) {
        if (service instanceof final SamlService samlService) {
            val requestId = samlService.getRequestId();
            if (StringUtils.isNotBlank(requestId)) {
                samlResponse.setInResponseTo(requestId);
            }
        }
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

        val samlResponse = SamlUtils.newSamlObject(Response.class);
        samlResponse.setID(id);
        samlResponse.setIssueInstant(issueInstant.toInstant());
        samlResponse.setVersion(SAMLVersion.VERSION_11);
        samlResponse.setInResponseTo(recipient);
        setInResponseToForSamlResponseIfNeeded(service, samlResponse);
        return samlResponse;
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
        val assertion = SamlUtils.newSamlObject(Assertion.class);

        assertion.setID(id);
        assertion.setIssueInstant(issuedAt.toInstant());
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
        val conditions = SamlUtils.newSamlObject(Conditions.class);
        conditions.setNotBefore(issuedAt.toInstant());

        val notOnOrAfter = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(issueLength);
        conditions.setNotOnOrAfter(notOnOrAfter.toInstant());

        val audienceRestriction = SamlUtils.newSamlObject(AudienceRestrictionCondition.class);
        val audience = SamlUtils.newSamlObject(Audience.class);
        audience.setURI(audienceUri);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictionConditions().add(audienceRestriction);
        return conditions;
    }

    /**
     * New status.
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
        val status = SamlUtils.newSamlObject(Status.class);
        val code = SamlUtils.newSamlObject(StatusCode.class);
        code.setValue(codeValue);
        status.setStatusCode(code);
        if (StringUtils.isNotBlank(statusMessage)) {
            val message = SamlUtils.newSamlObject(StatusMessage.class);
            message.setValue(statusMessage);
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

        val authnStatement = SamlUtils.newSamlObject(AuthenticationStatement.class);
        authnStatement.setAuthenticationInstant(authenticationDate.toInstant());

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
        val confirmation = SamlUtils.newSamlObject(SubjectConfirmation.class);
        val method = SamlUtils.newSamlObject(ConfirmationMethod.class);
        method.setURI(confirmationMethod);
        confirmation.getConfirmationMethods().add(method);
        val nameIdentifier = SamlUtils.newSamlObject(NameIdentifier.class);
        nameIdentifier.setValue(identifier);
        val subject = SamlUtils.newSamlObject(Subject.class);
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
        addAttributeValuesToSamlAttribute(attributeName, attributeValue, StringUtils.EMPTY,
            attributeList, AttributeValue.DEFAULT_ELEMENT_NAME);
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

        val attrStatement = SamlUtils.newSamlObject(AttributeStatement.class);
        attrStatement.setSubject(subject);
        for (val e : attributes.entrySet()) {
            if (e.getValue() instanceof Collection<?> && ((Collection<?>) e.getValue()).isEmpty()) {
                LOGGER.info("Skipping attribute [{}] because it does not have any values.", e.getKey());
                continue;
            }
            val attribute = SamlUtils.newSamlObject(Attribute.class);
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
        openSamlConfigBean.logObject(samlMessage);
        val encoder = new CasHttpSoap11Encoder();
        val context = new MessageContext();
        context.setMessage(samlMessage);
        encoder.setHttpServletResponseSupplier(() -> httpResponse);
        encoder.setMessageContext(context);
        encoder.initialize();
        encoder.prepareContext();
        encoder.encode();
    }
}
