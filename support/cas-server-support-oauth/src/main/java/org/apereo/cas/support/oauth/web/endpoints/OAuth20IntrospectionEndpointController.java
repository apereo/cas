package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.introspection.OAuth20IntrospectionAccessTokenResponse;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * This is {@link OAuth20IntrospectionEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OAuth20IntrospectionEndpointController extends BaseOAuth20Controller {

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    public OAuth20IntrospectionEndpointController(final ServicesManager servicesManager,
                                                  final TicketRegistry ticketRegistry,
                                                  final AccessTokenFactory accessTokenFactory,
                                                  final PrincipalFactory principalFactory,
                                                  final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                  final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                                  final CasConfigurationProperties casProperties,
                                                  final CookieRetrievingCookieGenerator cookieGenerator,
                                                  final CentralAuthenticationService centralAuthenticationService,
                                                  final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, ticketRegistry, accessTokenFactory, principalFactory,
            webApplicationServiceServiceFactory, scopeToAttributesFilter, casProperties, cookieGenerator);
        this.centralAuthenticationService = centralAuthenticationService;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
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
        value = {'/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL})
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
        value = {'/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.INTROSPECTION_URL})
    public ResponseEntity<OAuth20IntrospectionAccessTokenResponse> handlePostRequest(final HttpServletRequest request,
                                                                                     final HttpServletResponse response) {
        try {
            val authExtractor = new BasicAuthExtractor();
            val credentials = authExtractor.extract(Pac4jUtils.getPac4jJ2EContext(request, response));
            if (credentials == null) {
                throw new IllegalArgumentException("No credentials are provided to verify introspection on the access token");
            }

            val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, credentials.getUsername());
            if (validateIntrospectionRequest(service, credentials, request)) {
                val accessToken = StringUtils.defaultIfBlank(request.getParameter(OAuth20Constants.ACCESS_TOKEN),
                    request.getParameter(OAuth20Constants.TOKEN));

                LOGGER.debug("Located access token [{}] in the request", accessToken);
                val ticket = this.centralAuthenticationService.getTicket(accessToken, AccessToken.class);
                if (ticket != null) {
                    val introspect = createIntrospectionValidResponse(service, ticket);
                    return new ResponseEntity<>(introspect, HttpStatus.OK);
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        val introspect = createIntrospectionInvalidResponse();
        return new ResponseEntity<>(introspect, HttpStatus.UNAUTHORIZED);
    }

    private boolean validateIntrospectionRequest(final OAuthRegisteredService registeredService,
                                                 final UsernamePasswordCredentials credentials,
                                                 final HttpServletRequest request) {
        val tokenExists = HttpRequestUtils.doesParameterExist(request, OAuth20Constants.ACCESS_TOKEN)
            || HttpRequestUtils.doesParameterExist(request, OAuth20Constants.TOKEN);

        if (tokenExists && OAuth20Utils.checkClientSecret(registeredService, credentials.getPassword())) {
            val service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            return !accessResult.isExecutionFailure();
        }
        return false;
    }

    /**
     * Create introspection response OAuth introspection access token response.
     *
     * @param service the service
     * @param ticket  the ticket
     * @return the OAuth introspection access token response
     */
    protected OAuth20IntrospectionAccessTokenResponse createIntrospectionValidResponse(final OAuthRegisteredService service, final AccessToken ticket) {
        val introspect = new OAuth20IntrospectionAccessTokenResponse();
        introspect.setActive(true);
        introspect.setClientId(service.getClientId());
        val authentication = ticket.getAuthentication();
        val subject = authentication.getPrincipal().getId();
        introspect.setSub(subject);
        introspect.setUniqueSecurityName(subject);
        introspect.setExp(ticket.getExpirationPolicy().getTimeToLive());
        introspect.setIat(ticket.getCreationTime().toInstant().getEpochSecond());

        val methods = authentication.getAttributes().get(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
        val realmNames = CollectionUtils.toCollection(methods)
            .stream()
            .map(Object::toString)
            .collect(Collectors.joining(","));

        introspect.setRealmName(realmNames);
        introspect.setTokenType(OAuth20Constants.TOKEN_TYPE_BEARER);

        val grant = authentication.getAttributes().getOrDefault(OAuth20Constants.GRANT_TYPE, StringUtils.EMPTY).toString().toLowerCase();
        introspect.setGrantType(grant);
        introspect.setScope("CAS");
        introspect.setAud(service.getServiceId());
        introspect.setIss(casProperties.getAuthn().getOidc().getIssuer());
        return introspect;
    }

    /**
     * Create introspection invalid response.
     *
     * @return the o auth 20 introspection access token response
     */
    protected OAuth20IntrospectionAccessTokenResponse createIntrospectionInvalidResponse() {
        val introspect = new OAuth20IntrospectionAccessTokenResponse();
        introspect.setActive(false);
        return introspect;
    }
}
