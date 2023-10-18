package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.introspection.BaseOAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenFailureResponse;
import org.apereo.cas.support.oauth.web.response.introspection.success.OAuth20IntrospectionAccessTokenSuccessResponse;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
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

    private static ResponseEntity<OAuth20IntrospectionAccessTokenFailureResponse> buildUnauthorizedResponseEntity(
        final String code, final boolean isAuthenticationFailure) {
        val response = new OAuth20IntrospectionAccessTokenFailureResponse();
        response.setError(code);
        val headers = new LinkedMultiValueMap<String, String>();
        if (isAuthenticationFailure) {
            headers.add(HttpHeaders.WWW_AUTHENTICATE, "Basic");
        }
        return new ResponseEntity<>(response, headers, HttpStatus.UNAUTHORIZED);
    }

    private static ResponseEntity<OAuth20IntrospectionAccessTokenFailureResponse> buildBadRequestResponseEntity(final String code) {
        val response = new OAuth20IntrospectionAccessTokenFailureResponse();
        response.setError(code);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @GetMapping(value = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<? extends BaseOAuth20IntrospectionAccessTokenResponse> handleRequest(
        final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        return handlePostRequest(request, response);
    }

    /**
     * Handle post request.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     * @throws Throwable the throwable
     */
    @PostMapping(value = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handlePostRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        try {
            val context = new JEEContext(request, response);
            val credentialsResult = extractCredentials(context);
            if (credentialsResult.isEmpty()) {
                LOGGER.warn("Unable to locate and extract credentials from the request");
                return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_CLIENT, true);
            }

            val credentials = (UsernamePasswordCredentials) credentialsResult.get();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getConfigurationContext().getServicesManager(), credentials.getUsername());
            if (registeredService == null) {
                LOGGER.warn("Unable to locate service definition by client id [{}]", credentials.getUsername());
                return buildUnauthorizedResponseEntity(OAuth20Constants.INVALID_CLIENT, true);
            }

            val validationError = validateIntrospectionRequest(registeredService, credentials, request);
            if (validationError.isPresent()) {
                return validationError.get();
            }

            val accessToken = StringUtils.defaultIfBlank(request.getParameter(OAuth20Constants.TOKEN),
                request.getParameter(OAuth20Constants.ACCESS_TOKEN));

            LOGGER.debug("Located access token [{}] in the request", accessToken);

            val protocolMap = CollectionUtils.<String, Object>wrap(
                "Token", accessToken,
                "Client ID", registeredService.getClientId(),
                "Service", registeredService.getName());
            LoggingUtils.protocolMessage("OpenID Connect Introspection Request", protocolMap);

            OAuth20Token ticket = null;
            try {
                val token = extractAccessTokenFrom(accessToken);
                ticket = getConfigurationContext().getTicketRegistry().getTicket(token, OAuth20Token.class);
            } catch (final InvalidTicketException e) {
                LOGGER.trace(e.getMessage(), e);
                LOGGER.info("Unable to fetch access token [{}]: [{}]", accessToken, e.getMessage());
            }
            val introspect = createIntrospectionValidResponse(accessToken, ticket);

            return buildIntrospectionEntityResponse(context, introspect);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return buildBadRequestResponseEntity(OAuth20Constants.INVALID_REQUEST);
    }

    protected Optional<Credentials> extractCredentials(final JEEContext context) {
        val authExtractor = new BasicAuthExtractor();
        val callContext = new CallContext(context, getConfigurationContext().getSessionStore(),
            getConfigurationContext().getOauthConfig().getProfileManagerFactory());
        return authExtractor.extract(callContext);
    }
    
    protected OAuth20IntrospectionAccessTokenSuccessResponse createIntrospectionValidResponse(
        final String accessTokenId, final OAuth20Token ticket) {
        val introspect = new OAuth20IntrospectionAccessTokenSuccessResponse();
        introspect.setScope("CAS");
        introspect.setActive(ticket != null && !ticket.isExpired());
        
        if (ticket != null) {
            introspect.setClientId(ticket.getClientId());
            introspect.setAud(ticket.getService().getId());
            introspect.setToken(accessTokenId);

            val authentication = ticket.getAuthentication();
            val attributes = new LinkedHashMap<String, Object>(authentication.getAttributes());
            attributes.putAll(authentication.getPrincipal().getAttributes());

            val subject = authentication.getPrincipal().getId();
            introspect.setSub(subject);
            introspect.setUniqueSecurityName(subject);
            introspect.setIat(ticket.getCreationTime().toInstant().getEpochSecond());
            introspect.setExp(introspect.getIat() + ticket.getExpirationPolicy().getTimeToLive());

            val methods = attributes.get(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
            val realmNames = CollectionUtils.toCollection(methods)
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

            introspect.setRealmName(realmNames);
            val tokenType = attributes.containsKey(OAuth20Constants.DPOP_CONFIRMATION)
                ? OAuth20Constants.TOKEN_TYPE_DPOP
                : OAuth20Constants.TOKEN_TYPE_BEARER;
            introspect.setTokenType(tokenType);

            CollectionUtils.firstElement(attributes.get(OAuth20Constants.GRANT_TYPE))
                .ifPresent(type -> introspect.setGrantType(type.toString()));
            CollectionUtils.firstElement(attributes.get(OAuth20Constants.X509_CERTIFICATE_DIGEST))
                .ifPresent(digest -> introspect.getConfirmation().setX5t(digest.toString()));
        }
        return introspect;
    }

    private Optional<ResponseEntity<? extends BaseOAuth20IntrospectionAccessTokenResponse>> validateIntrospectionRequest(
        final OAuthRegisteredService registeredService, final UsernamePasswordCredentials credentials,
        final HttpServletRequest request) throws Throwable {
        val tokenExists = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.TOKEN)
            || HttpRequestUtils.doesParameterExist(request, OAuth20Constants.ACCESS_TOKEN);

        if (!tokenExists) {
            LOGGER.warn("Access token cannot be found in the request");
            return Optional.of(buildBadRequestResponseEntity(OAuth20Constants.MISSING_ACCESS_TOKEN));
        }

        if (getConfigurationContext().getClientSecretValidator().validate(registeredService, credentials.getPassword())) {
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

    protected ResponseEntity buildIntrospectionEntityResponse(
        final WebContext context, final OAuth20IntrospectionAccessTokenSuccessResponse introspect) {
        return new ResponseEntity<>(introspect, HttpStatus.OK);
    }
}
