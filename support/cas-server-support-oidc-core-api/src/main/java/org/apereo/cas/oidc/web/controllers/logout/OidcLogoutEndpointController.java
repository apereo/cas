package org.apereo.cas.oidc.web.controllers.logout;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.logout.slo.SingleLogoutUrl;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.WebUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
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
@Tag(name = "OpenID Connect")
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
     * @param idToken               the ID token
     * @param request               the request
     * @param response              the response
     * @return the response entity
     * @throws Exception the exception
     */
    @GetMapping({
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.LOGOUT_URL,
        '/' + OidcConstants.BASE_OIDC_URL + "/logout",
        "/**/" + OidcConstants.LOGOUT_URL
    })
    @Operation(summary = "Handle OIDC logout request",
        parameters = {
            @Parameter(name = OidcConstants.POST_LOGOUT_REDIRECT_URI,
                description = "Post logout redirect URL", in = ParameterIn.QUERY, required = false),
            @Parameter(name = OAuth20Constants.STATE,
                description = "State", in = ParameterIn.QUERY, required = false),
            @Parameter(name = OAuth20Constants.CLIENT_ID,
                description = "Client ID", in = ParameterIn.QUERY, required = false),
            @Parameter(name = OidcConstants.ID_TOKEN_HINT,
                description = "ID Token hint", in = ParameterIn.QUERY, required = false)
        })
    public ResponseEntity handleRequestInternal(
        @RequestParam(value = OidcConstants.POST_LOGOUT_REDIRECT_URI, required = false)
        final String postLogoutRedirectUrl,
        @RequestParam(value = OAuth20Constants.STATE, required = false)
        final String state,
        @RequestParam(value = OAuth20Constants.CLIENT_ID, required = false)
        final String givenClientId,
        @RequestParam(value = OidcConstants.ID_TOKEN_HINT, required = false)
        final String idToken,
        final HttpServletRequest request, final HttpServletResponse response) throws Throwable {

        if (StringUtils.isNotBlank(idToken)) {
            LOGGER.trace("Decoding logout ID token [{}]", idToken);

            val clientIdInIdToken = OAuth20Utils.extractClientIdFromToken(idToken);
            LOGGER.debug("Client id retrieved from ID token is [{}]", clientIdInIdToken);

            if (StringUtils.isNotBlank(givenClientId) && !Strings.CI.equals(givenClientId, clientIdInIdToken)) {
                LOGGER.warn("Client id [{}] in logout request does not match client id [{}] in ID token", givenClientId, clientIdInIdToken);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    configurationContext.getMessageSource().getMessage("screen.oidc.issuer.invalid", ArrayUtils.EMPTY_OBJECT_ARRAY, request.getLocale()));
            }
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
                getConfigurationContext().getServicesManager(), clientIdInIdToken, OidcRegisteredService.class);
            val idTokenClaims = getConfigurationContext().getIdTokenSigningAndEncryptionService().decode(idToken, Optional.of(registeredService));
            Assert.isTrue(idTokenClaims.getClaimValueAsString(OAuth20Constants.CLIENT_ID).equalsIgnoreCase(registeredService.getClientId()),
                "Client id in ID token does not match client id in registered service");
            Assert.isTrue(idTokenClaims.hasClaim(OidcConstants.AUD), "Audience claim is not present");
            Assert.isTrue(idTokenClaims.hasClaim(OAuth20Constants.CLAIM_SUB), "Subject claim is not present");

            LOGGER.debug("Located registered service [{}]", registeredService);
            val service = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(clientIdInIdToken);
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            accessResult.throwExceptionIfNeeded();

            if (!enforceIssuer(request, response, registeredService)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    configurationContext.getMessageSource().getMessage("screen.oidc.issuer.invalid", ArrayUtils.EMPTY_OBJECT_ARRAY, request.getLocale()));
            }

            WebUtils.putRegisteredService(request, Objects.requireNonNull(registeredService));
            val urls = getConfigurationContext().getSingleLogoutServiceLogoutUrlBuilder()
                .determineLogoutUrl(registeredService, service, Optional.of(request))
                .stream().map(SingleLogoutUrl::getUrl).collect(Collectors.toList());
            LOGGER.debug("Logout urls assigned to registered service are [{}]", urls);
            if (StringUtils.isNotBlank(postLogoutRedirectUrl) && registeredService.getMatchingStrategy() != null) {
                val postLogoutService = getConfigurationContext().getWebApplicationServiceServiceFactory().createService(postLogoutRedirectUrl);
                val matchResult = registeredService.matches(postLogoutRedirectUrl)
                    || urls.stream().anyMatch(url -> postLogoutRedirectUrlMatcher.matches(postLogoutRedirectUrl, url))
                    || getConfigurationContext().getServicesManager().findServiceBy(postLogoutService) != null;

                if (matchResult) {
                    LOGGER.debug("Requested logout URL [{}] is authorized for redirects", postLogoutRedirectUrl);
                    return executeLogoutRedirect(Optional.ofNullable(StringUtils.trimToNull(state)),
                        Optional.of(postLogoutRedirectUrl), Optional.of(clientIdInIdToken), request, response);
                }
            }
            val validURL = urls.stream().filter(urlValidator::isValid).findFirst();
            if (validURL.isPresent()) {
                return executeLogoutRedirect(Optional.ofNullable(StringUtils.trimToNull(state)),
                    validURL, Optional.of(clientIdInIdToken), request, response);
            }
            LOGGER.debug("No logout urls could be determined for registered service [{}]", registeredService.getName());
        }

        val registeredService = StringUtils.isNotBlank(givenClientId)
            ? OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), givenClientId, OidcRegisteredService.class)
            : null;

        if (!enforceIssuer(request, response, registeredService)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                configurationContext.getMessageSource().getMessage("screen.oidc.issuer.invalid", ArrayUtils.EMPTY_OBJECT_ARRAY, request.getLocale()));
        }
        return executeLogoutRedirect(Optional.ofNullable(StringUtils.trimToNull(state)),
            Optional.empty(), Optional.ofNullable(givenClientId), request, response);
    }

    private boolean enforceIssuer(final HttpServletRequest request, final HttpServletResponse response,
                                  final OidcRegisteredService registeredService) {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, List.of(OidcConstants.LOGOUT_URL, "logout"), registeredService)) {
            val issuer = getConfigurationContext().getIssuerService().determineIssuer(Optional.ofNullable(registeredService));
            LOGGER.warn("Logout request is not issued by a trusted issuer: [{}]", issuer);
            return false;
        }
        return true;
    }

    protected ResponseEntity executeLogoutRedirect(final Optional<String> state,
                                                   final Optional<String> redirectUrl,
                                                   final Optional<String> clientId,
                                                   final HttpServletRequest request,
                                                   final HttpServletResponse response) throws Exception {
        redirectUrl.ifPresent(url -> {
            val builder = UriComponentsBuilder.fromUriString(url);
            state.ifPresent(st -> builder.queryParam(OAuth20Constants.STATE, st));
            clientId.ifPresent(id -> builder.queryParam(OAuth20Constants.CLIENT_ID, id));
            val logoutUrl = builder.build().toUriString();
            LOGGER.debug("Final logout redirect URL is [{}]", logoutUrl);
            WebUtils.putLogoutRedirectUrl(request, logoutUrl);
        });
        request.setAttribute("status", HttpStatus.PERMANENT_REDIRECT);
        request.getServletContext()
            .getRequestDispatcher(CasProtocolConstants.ENDPOINT_LOGOUT)
            .forward(request, response);
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT).build();
    }
}
