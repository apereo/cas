package org.apereo.cas.support.saml.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.util.Saml10ObjectBuilder;
import org.apereo.cas.support.saml.util.Saml20HexRandomIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.saml1.core.Response;
import org.opensaml.saml.saml1.core.StatusCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    private final String issueLength;

    private final String skewAllowance;

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
        val skew = Beans.newDuration(skewAllowance).toSeconds();
        return samlObjectBuilder.newResponse(
            Saml20HexRandomIdGenerator.INSTANCE.getNewString(),
            ZonedDateTime.now(ZoneOffset.UTC).minusSeconds(skew), serviceId, service);
    }

    /**
     * Sets status request denied.
     *
     * @param response    the response
     * @param description the description
     */
    public void setStatusRequestDenied(final Response response, final String description) {
        response.setStatus(samlObjectBuilder.newStatus(StatusCode.REQUEST_DENIED, description));
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
    public void prepareSuccessfulResponse(final Map<String, Object> model,
                                          final Response response,
                                          final Service service,
                                          final Authentication authentication,
                                          final Principal principal,
                                          final Map<String, List<Object>> authnAttributes,
                                          final Map<String, List<Object>> principalAttributes) {

        val issuedAt = DateTimeUtils.zonedDateTimeOf(response.getIssueInstant());
        LOGGER.debug("Preparing SAML response for service [{}] issued at [{}]", service, issuedAt);

        val authnMethods = CollectionUtils.toCollection(authentication.getAttributes()
            .get(SamlAuthenticationMetaDataPopulator.ATTRIBUTE_AUTHENTICATION_METHOD));
        LOGGER.debug("Authentication methods found are [{}]", authnMethods);

        val authnStatement = samlObjectBuilder.newAuthenticationStatement(
            authentication.getAuthenticationDate(), authnMethods, principal.getId());
        LOGGER.debug("Built authentication statement for [{}] dated at [{}]", principal, authentication.getAuthenticationDate());

        val assertion = samlObjectBuilder.newAssertion(authnStatement, issuer, issuedAt,
            Saml20HexRandomIdGenerator.INSTANCE.getNewString());
        LOGGER.debug("Built assertion for issuer [{}] dated at [{}]", issuer, issuedAt);

        val skewIssueInSeconds = Beans.newDuration(issueLength).toSeconds();
        val conditions = samlObjectBuilder.newConditions(issuedAt, service.getId(), skewIssueInSeconds);
        assertion.setConditions(conditions);
        LOGGER.debug("Built assertion conditions for issuer [{}] and service [{}] ", issuer, service.getId());

        val subject = samlObjectBuilder.newSubject(principal.getId());
        LOGGER.debug("Built subject for principal [{}]", subject);

        val attributesToSend = prepareSamlAttributes(model, service, authnAttributes, principalAttributes);
        LOGGER.debug("Authentication statement shall include these attributes [{}]", attributesToSend);

        if (!attributesToSend.isEmpty()) {
            assertion.getAttributeStatements().add(samlObjectBuilder.newAttributeStatement(
                subject, attributesToSend, defaultAttributeNamespace));
        }

        response.setStatus(samlObjectBuilder.newStatus(StatusCode.SUCCESS, null));
        LOGGER.debug("Set response status code to [{}]", response.getStatus());

        response.getAssertions().add(assertion);
    }

    /**
     * Encode saml response.
     *
     * @param samlResponse the saml response
     * @param request      the request
     * @param response     the response
     * @throws Exception the exception
     */
    public void encodeSamlResponse(final Response samlResponse, final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        samlObjectBuilder.encodeSamlResponse(response, request, samlResponse);
    }

    private Map<String, Object> prepareSamlAttributes(final Map<String, Object> model, final Service service,
                                                      final Map<String, List<Object>> authnAttributes,
                                                      final Map<String, List<Object>> principalAttributes) {
        val registeredService = servicesManager.findServiceBy(service);
        LOGGER.debug("Retrieved authentication attributes [{}] from the model", authnAttributes);

        val attributesToReturn = new HashMap<String, Object>();
        attributesToReturn.putAll(principalAttributes);
        attributesToReturn.putAll(authnAttributes);

        LOGGER.debug("Beginning to encode attributes [{}] for service [{}]", attributesToReturn, registeredService.getServiceId());
        val finalAttributes = protocolAttributeEncoder.encodeAttributes(model, attributesToReturn, registeredService, service);
        LOGGER.debug("Final collection of attributes are [{}]", finalAttributes);

        return finalAttributes;
    }
}
