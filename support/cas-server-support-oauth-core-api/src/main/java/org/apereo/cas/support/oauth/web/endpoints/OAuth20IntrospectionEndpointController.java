package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link OAuth20IntrospectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OAuth20IntrospectionEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {

    public OAuth20IntrospectionEndpointController(final T oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Build unauthorized response entity.
     *
     * @param code the code
     * @return the response entity
     */
    private static ResponseEntity<OAuth20IntrospectionAccessTokenResponse> buildUnauthorizedResponseEntity(final String code,
                                                                                                           final boolean isAuthenticationFailure) {
        val map = new LinkedMultiValueMap<String, String>(1);
        map.add(OAuth20Constants.ERROR, code);
        val value = OAuth20Utils.toJson(map);
        val headers = new LinkedMultiValueMap<String, String>();
        if (isAuthenticationFailure) {
            headers.add(HttpHeaders.WWW_AUTHENTICATE, "Basic");
        }
        return new ResponseEntity(value, headers, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Build bad request response entity.
     *
     * @param code the code
     * @return the response entity
     */
    private static ResponseEntity<OAuth20IntrospectionAccessTokenResponse> buildBadRequestResponseEntity(final String code) {
        val map = new LinkedMultiValueMap<String, String>(1);
        map.add(OAuth20Constants.ERROR, code);
        val value = OAuth20Utils.toJson(map);
        return (ResponseEntity<OAuth20IntrospectionAccessTokenResponse>) new ResponseEntity(value, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        value = '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL)
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handleRequest(final HttpServletRequest request,
                                                                                 final HttpServletResponse response) {
        return handlePostRequest(request, response);
    }

    /**
     * Handle post request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE,
        value = '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL)
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handlePostRequest(final HttpServletRequest request,
                                                                                     final HttpServletResponse response) {
        ResponseEntity<OAuth20IntrospectionAccessTokenResponse> result;
        try {
            val authExtractor = new BasicAuthExtractor();

            val context = new JEEContext(request, response);
            val credentialsResult = authExtractor.extract(context, getConfigurationContext().getSessionStore());

            if (credentialsResult.isEmpty()) {
                LOGGER.warn("Unable to locate and extract credentials from the request");
                return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_CLIENT, true);
            }

            val credentials = (UsernamePasswordCredentials) credentialsResult.get();
            val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getConfigurationContext().getServicesManager(), credentials.getUsername());
            if (service == null) {
                LOGGER.warn("Unable to locate service definition by client id [{}]", credentials.getUsername());
                return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_CLIENT, true);
            }

            val validationError = validateIntrospectionRequest(service, credentials, request);
            if (validationError.isPresent()) {
                result = validationError.get();
            } else {
                val accessToken = StringUtils.defaultIfBlank(request.getParameter(OAuth20Constants.TOKEN),
                    request.getParameter(OAuth20Constants.ACCESS_TOKEN));

                LOGGER.debug("Located access token [{}] in the request", accessToken);
                var ticket = (OAuth20AccessToken) null;
                try {
                    val token = extractAccessTokenFrom(accessToken);
                    ticket = getConfigurationContext().getCentralAuthenticationService().getTicket(token, OAuth20AccessToken.class);
                } catch (final InvalidTicketException e) {
                    LOGGER.trace(e.getMessage(), e);
                    LOGGER.info("Unable to fetch access token [{}]: [{}]", accessToken, e.getMessage());
                }
                val introspect = createIntrospectionValidResponse(ticket);
                result = new ResponseEntity<>(introspect, HttpStatus.OK);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            result = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    /**
     * Create introspection response OAuth introspection access token response.
     *
     * @param ticket the ticket
     * @return the OAuth introspection access token response
     */
    protected OAuth20IntrospectionAccessTokenResponse createIntrospectionValidResponse(final OAuth20AccessToken ticket) {
        val introspect = new OAuth20IntrospectionAccessTokenResponse();
        introspect.setScope("CAS");

        if (ticket != null) {
            introspect.setClientId(ticket.getClientId());
            introspect.setAud(ticket.getService().getId());
            introspect.setActive(true);
            val authentication = ticket.getAuthentication();
            val subject = authentication.getPrincipal().getId();
            introspect.setSub(subject);
            introspect.setUniqueSecurityName(subject);
            introspect.setIat(ticket.getCreationTime().toInstant().getEpochSecond());
            introspect.setExp(introspect.getIat() + ticket.getExpirationPolicy().getTimeToLive());

            val methods = authentication.getAttributes().get(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
            val realmNames = CollectionUtils.toCollection(methods)
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

            introspect.setRealmName(realmNames);
            introspect.setTokenType(OAuth20Constants.TOKEN_TYPE_BEARER);

            val grant = authentication.getAttributes().getOrDefault(OAuth20Constants.GRANT_TYPE, new ArrayList<>(0));
            if (!grant.isEmpty()) {
                introspect.setGrantType(grant.get(0).toString().toLowerCase());
            }
        } else {
            introspect.setActive(false);
        }
        return introspect;
    }

    private Optional<ResponseEntity<OAuth20IntrospectionAccessTokenResponse>> validateIntrospectionRequest(
        final OAuthRegisteredService registeredService,
        final UsernamePasswordCredentials credentials,
        final HttpServletRequest request) {
        val tokenExists = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.TOKEN)
            || HttpRequestUtils.doesParameterExist(request, OAuth20Constants.ACCESS_TOKEN);

        if (!tokenExists) {
            LOGGER.warn("Access token cannot be found in the request");
            return Optional.of(buildBadRequestResponseEntity(OAuth20Constants.MISSING_ACCESS_TOKEN));
        }

        if (OAuth20Utils.checkClientSecret(registeredService, credentials.getPassword(),
            getConfigurationContext().getRegisteredServiceCipherExecutor())) {
            val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(registeredService.getServiceId());
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            return accessResult.isExecutionFailure()
                ? Optional.of(buildUnauthorizedResponseEntity(OAuth20Constants.UNAUTHORIZED_CLIENT, false))
                : Optional.empty();
        }
        LOGGER.warn("Unable to match client secret for registered service [{}] with client id [{}]",
            registeredService.getName(), registeredService.getClientId());
        return Optional.of(buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_CLIENT, true));
    }
}
