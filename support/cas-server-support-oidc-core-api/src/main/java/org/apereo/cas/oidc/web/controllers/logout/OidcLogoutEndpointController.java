package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link OidcLogoutEndpointController}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class OidcLogoutEndpointController extends BaseOidcController {

    private final UrlValidator urlValidator;

    private final OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher;

    public OidcLogoutEndpointController(final OidcConfigurationContext context,
                                        final OidcPostLogoutRedirectUrlMatcher postLogoutRedirectUrlMatcher,
                                        final UrlValidator urlValidator) {
        super(context);
        this.urlValidator = urlValidator;
        this.postLogoutRedirectUrlMatcher = postLogoutRedirectUrlMatcher;
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
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.LOGOUT_URL,
        '/' + OidcConstants.BASE_OIDC_URL + "/logout",
        "/**/" + OidcConstants.LOGOUT_URL
    })
    @SneakyThrows
    public ResponseEntity<HttpStatus> handleRequestInternal(
        @RequestParam(value = "post_logout_redirect_uri", required = false) final String postLogoutRedirectUrl,
        @RequestParam(value = "state", required = false) final String state,
        @RequestParam(value = "id_token_hint", required = false) final String idToken,
        final HttpServletRequest request, final HttpServletResponse response) {

        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getOidcRequestSupport().isValidIssuerForEndpoint(webContext, OidcConstants.LOGOUT_URL)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        String clientId = null;

        if (StringUtils.isNotBlank(idToken)) {
            LOGGER.trace("Decoding logout id token [{}]", idToken);
            val configContext = getConfigurationContext();
            val claims = configContext.getIdTokenSigningAndEncryptionService().decode(idToken, Optional.empty());
            clientId = claims.getStringClaimValue(OAuth20Constants.CLIENT_ID);
            LOGGER.debug("Client id retrieved from id token is [{}]", clientId);

            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configContext.getServicesManager(), clientId);
            LOGGER.debug("Located registered service [{}]", registeredService);
            val service = configContext.getWebApplicationServiceServiceFactory().createService(clientId);
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = configContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            WebUtils.putRegisteredService(request, Objects.requireNonNull(registeredService));
            val urls = configContext.getSingleLogoutServiceLogoutUrlBuilder()
                .determineLogoutUrl(registeredService, service, Optional.of(request))
                    .stream().map(SingleLogoutUrl::getUrl).collect(Collectors.toList());
            LOGGER.debug("Logout urls assigned to registered service are [{}]", urls);
            if (StringUtils.isNotBlank(postLogoutRedirectUrl) && registeredService.getMatchingStrategy() != null) {
                val matchResult = registeredService.matches(postLogoutRedirectUrl)
                    || urls.stream().anyMatch(url -> postLogoutRedirectUrlMatcher.matches(postLogoutRedirectUrl, url));
                if (matchResult) {
                    LOGGER.debug("Requested logout URL [{}] is authorized for redirects", postLogoutRedirectUrl);
                    return new ResponseEntity<>(executeLogoutRedirect(Optional.ofNullable(StringUtils.trimToNull(state)),
                        Optional.of(postLogoutRedirectUrl), Optional.of(clientId),
                        request, response));
                }
            }
            val validURL = urls.stream().filter(urlValidator::isValid).findFirst();
            if (validURL.isPresent()) {
                return new ResponseEntity<>(executeLogoutRedirect(Optional.ofNullable(StringUtils.trimToNull(state)),
                        validURL, Optional.of(clientId), request, response));
            }
            LOGGER.debug("No logout urls could be determined for registered service [{}]", registeredService.getName());
        }
        return new ResponseEntity<>(executeLogoutRedirect(Optional.ofNullable(StringUtils.trimToNull(state)),
            Optional.empty(), Optional.ofNullable(clientId), request, response));
    }

    /**
     * Gets logout redirect view.
     *
     * @param state       the state
     * @param redirectUrl the redirect url
     * @param clientId    the client id
     * @param request     the request
     * @param response    the response
     * @return the logout redirect view
     */
    @SneakyThrows
    protected HttpStatus executeLogoutRedirect(final Optional<String> state,
                                               final Optional<String> redirectUrl,
                                               final Optional<String> clientId,
                                               final HttpServletRequest request,
                                               final HttpServletResponse response) {
        redirectUrl.ifPresent(url -> {
            val builder = UriComponentsBuilder.fromHttpUrl(url);
            state.ifPresent(st -> builder.queryParam(OAuth20Constants.STATE, st));
            clientId.ifPresent(id -> builder.queryParam(OAuth20Constants.CLIENT_ID, id));
            val logoutUrl = builder.build().toUriString();
            LOGGER.debug("Final logout redirect URL is [{}]", logoutUrl);
            WebUtils.putLogoutRedirectUrl(request, logoutUrl);
        });
        request.getServletContext()
            .getRequestDispatcher(CasProtocolConstants.ENDPOINT_LOGOUT)
            .forward(request, response);
        return HttpStatus.PERMANENT_REDIRECT;
    }
}
