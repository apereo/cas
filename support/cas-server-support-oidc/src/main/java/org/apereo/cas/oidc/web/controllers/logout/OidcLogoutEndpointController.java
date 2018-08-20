package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.BaseOAuth20Controller;
import org.apereo.cas.ticket.IdTokenSigningAndEncryptionService;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcLogoutEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OidcLogoutEndpointController extends BaseOAuth20Controller {
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;
    private final IdTokenSigningAndEncryptionService idTokenSigningAndEncryptionService;
    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    public OidcLogoutEndpointController(final ServicesManager servicesManager, final TicketRegistry ticketRegistry,
                                        final AccessTokenFactory accessTokenFactory,
                                        final PrincipalFactory principalFactory,
                                        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                        final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                        final CasConfigurationProperties casProperties,
                                        final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                        final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                        final IdTokenSigningAndEncryptionService idTokenSigningAndEncryptionService,
                                        final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder) {
        super(servicesManager, ticketRegistry, accessTokenFactory, principalFactory, webApplicationServiceServiceFactory,
            scopeToAttributesFilter, casProperties, ticketGrantingTicketCookieGenerator);
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.idTokenSigningAndEncryptionService = idTokenSigningAndEncryptionService;
        this.singleLogoutServiceLogoutUrlBuilder = singleLogoutServiceLogoutUrlBuilder;
    }

    /**
     * Handle request.
     *
     * @param postLogoutRedirectUrl the post logout redirect url
     * @param state                 the state
     * @param idToken               the id token
     * @param request               the request
     * @param response              the response
     * @return the response entity
     */
    @GetMapping(value = '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.LOGOUT_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    @SneakyThrows
    public View handleRequestInternal(@RequestParam(value = "post_logout_redirect_uri", required = false) final String postLogoutRedirectUrl,
                                      @RequestParam(value = "state", required = false) final String state,
                                      @RequestParam(value = "id_token_hint", required = false) final String idToken,
                                      final HttpServletRequest request, final HttpServletResponse response) {

        if (StringUtils.isNotBlank(idToken)) {
            val claims = this.idTokenSigningAndEncryptionService.validate(idToken);

            val clientId = claims.getStringClaimValue(OAuth20Constants.CLIENT_ID);

            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
            val service = webApplicationServiceServiceFactory.createService(clientId);

            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .retrievePrincipalAttributesFromReleasePolicy(Boolean.FALSE)
                .build();
            val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            val urls = singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, service);
            if (StringUtils.isNotBlank(postLogoutRedirectUrl)) {
                val matchResult = urls.stream().anyMatch(url -> url.toExternalForm().equalsIgnoreCase(postLogoutRedirectUrl));
                if (matchResult) {
                    return getLogoutRedirectView(state, postLogoutRedirectUrl);
                }
            }

            if (urls.isEmpty()) {
                return getLogoutRedirectView(state, null);
            }
            return getLogoutRedirectView(state, urls.toArray()[0].toString());
        }

        return getLogoutRedirectView(state, null);
    }

    private View getLogoutRedirectView(final String state, final String redirectUrl) {
        val builder = UriComponentsBuilder.fromHttpUrl(casProperties.getServer().getLogoutUrl());
        if (StringUtils.isNotBlank(redirectUrl)) {
            builder.queryParam(casProperties.getLogout().getRedirectParameter(), redirectUrl);
        }
        if (StringUtils.isNotBlank(state)) {
            builder.queryParam(OAuth20Constants.STATE, redirectUrl);
        }
        val logoutUrl = builder.build().toUriString();
        return new RedirectView(logoutUrl);
    }
}
