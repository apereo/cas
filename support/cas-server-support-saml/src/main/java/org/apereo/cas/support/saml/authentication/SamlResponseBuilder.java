package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SamlResponseBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlResponseBuilder {
    private final Saml10ObjectBuilder samlObjectBuilder;
    private final String issuer;
    private final String defaultAttributeNamespace;
    private final int issueLength;
    private final int skewAllowance;
    private final ProtocolAttributeEncoder protocolAttributeEncoder;
    private final ServicesManager servicesManager;

    /**
     * Create response.
     *
     * @param serviceId the service id
     * @param service   the service
     * @return the response
     */
    public Response createResponse(final String serviceId, final WebApplicationService service) {
        return this.samlObjectBuilder.newResponse(
            this.samlObjectBuilder.generateSecureRandomId(),
            ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(this.skewAllowance), serviceId, service);
    }

    /**
     * Sets status request denied.
     *
     * @param response    the response
     * @param description the description
     */
    public void setStatusRequestDenied(final Response response, final String description) {
        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.REQUEST_DENIED, description));
    }

    /**
     * Prepare successful response.
     *
     * @param response            the response
     * @param service             the service
     * @param authentication      the authentication
     * @param principal           the principal
     * @param authnAttributes     the authn attributes
     * @param principalAttributes the principal attributes
     */
    public void prepareSuccessfulResponse(final Response response, final Service service,
                                          final Authentication authentication, final Principal principal,
                                          final Map<String, List<Object>> authnAttributes,
                                          final Map<String, List<Object>> principalAttributes) {

        val issuedAt = DateTimeUtils.zonedDateTimeOf(response.getIssueInstant());
        LOGGER.debug("Preparing SAML response for service [{}]", service);

        final Collection<Object> authnMethods = CollectionUtils.toCollection(authentication.getAttributes()
            .get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
        LOGGER.debug("Authentication methods found are [{}]", authnMethods);

        val authnStatement = this.samlObjectBuilder.newAuthenticationStatement(
            authentication.getAuthenticationDate(), authnMethods, principal.getId());
        LOGGER.debug("Built authentication statement for [{}] dated at [{}]", principal, authentication.getAuthenticationDate());

        val assertion = this.samlObjectBuilder.newAssertion(authnStatement, this.issuer, issuedAt,
            this.samlObjectBuilder.generateSecureRandomId());
        LOGGER.debug("Built assertion for issuer [{}] dated at [{}]", this.issuer, issuedAt);

        val conditions = this.samlObjectBuilder.newConditions(issuedAt, service.getId(), this.issueLength);
        assertion.setConditions(conditions);
        LOGGER.debug("Built assertion conditions for issuer [{}] and service [{}] ", this.issuer, service.getId());

        val subject = this.samlObjectBuilder.newSubject(principal.getId());
        LOGGER.debug("Built subject for principal [{}]", subject);

        val attributesToSend = prepareSamlAttributes(service, authnAttributes, principalAttributes);
        LOGGER.debug("Authentication statement shall include these attributes [{}]", attributesToSend);

        if (!attributesToSend.isEmpty()) {
            assertion.getAttributeStatements().add(this.samlObjectBuilder.newAttributeStatement(
                subject, attributesToSend, this.defaultAttributeNamespace));
        }

        response.setStatus(this.samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));
        LOGGER.debug("Set response status code to [{}]", response.getStatus());

        response.getAssertions().add(assertion);
    }

    private Map<String, Object> prepareSamlAttributes(final Service service,
                                                      final Map<String, List<Object>> authnAttributes,
                                                      final Map<String, List<Object>> principalAttributes) {
        val registeredService = this.servicesManager.findServiceBy(service);

        LOGGER.debug("Retrieved authentication attributes [{}] from the model", authnAttributes);

        val attributesToReturn = new HashMap<String, Object>();
        attributesToReturn.putAll(principalAttributes);
        attributesToReturn.putAll(authnAttributes);

        LOGGER.debug("Beginning to encode attributes [{}] for service [{}]", attributesToReturn, registeredService.getServiceId());
        val finalAttributes = this.protocolAttributeEncoder.encodeAttributes(attributesToReturn, registeredService);
        LOGGER.debug("Final collection of attributes are [{}]", finalAttributes);

        return finalAttributes;
    }

    /**
     * Encode saml response.
     *
     * @param samlResponse the saml response
     * @param request      the request
     * @param response     the response
     * @throws Exception the exception
     */
    public void encodeSamlResponse(final Response samlResponse, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        this.samlObjectBuilder.encodeSamlResponse(response, request, samlResponse);
    }
}
