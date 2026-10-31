package org.apereo.cas.oidc.web.controllers.dynareg;

import module java.base;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20ClientAuthenticationInterceptor;
import org.apereo.cas.support.oauth.web.response.accesstoken.OAuth20TokenGeneratedResult;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20AccessTokenResponseResult;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OidcInitialAccessTokenController}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcInitialAccessTokenController extends BaseOidcController {
    private final @Nullable String initialAccessTokenUser;

    private final @Nullable String initialAccessTokenPassword;

    public OidcInitialAccessTokenController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
        val registration = configurationContext.getCasProperties().getAuthn().getOidc().getRegistration();
        if (registration.getDynamicClientRegistrationMode().isProtected()) {
            this.initialAccessTokenUser = StringUtils.defaultIfBlank(registration.getInitialAccessTokenUser(), RandomUtils.randomAlphabetic(8));
            this.initialAccessTokenPassword = StringUtils.defaultIfBlank(registration.getInitialAccessTokenPassword(), RandomUtils.randomAlphabetic(8));
        } else {
            this.initialAccessTokenUser = null;
            this.initialAccessTokenPassword = null;
        }
    }

    /**
     * Handle request. A request without the credentials of the user configured for initial access tokens is answered with
     * {@code 401} and a {@code Basic} challenge.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL,
        "/**/" + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Handle OIDC initial access token request")
    public ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response) {   
        if (!getConfigurationContext().getIssuerService().validateIssuer(request, List.of(OidcConstants.REGISTRATION_INITIAL_TOKEN_URL))) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            val modelAndView = new ModelAndView(new JacksonJsonView(), body);
            modelAndView.setStatus(HttpStatus.BAD_REQUEST);
            return modelAndView;
        }
        val casProperties = getConfigurationContext().getCasProperties();
        val oidcProperties = casProperties.getAuthn().getOidc();

        if (!oidcProperties.getRegistration().getDynamicClientRegistrationMode().isProtected()) {
            LOGGER.warn("Dynamic client registration mode is not configured as protected.");
            return getBadRequestResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        return authenticate(request)
            .map(userProfile -> {
                val principal = FunctionUtils.doUnchecked(() -> PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(userProfile.getId()));
                val service = getConfigurationContext().getWebApplicationServiceServiceFactory()
                    .createService(casProperties.getServer().getPrefix());

                val tokenRequestContext = AccessTokenRequestContext
                    .builder()
                    .authentication(DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build())
                    .service(service)
                    .grantType(OAuth20GrantTypes.NONE)
                    .responseType(OAuth20ResponseTypes.NONE)
                    .scopes(Set.of(OidcConstants.StandardScopes.OPENID.getScope(), OidcConstants.CLIENT_REGISTRATION_SCOPE))
                    .build();

                return generateInitialAccessToken(tokenRequestContext)
                    .map(this::resolveAccessToken)
                    .map(accessToken -> {
                        val accessTokenResult = OAuth20TokenGeneratedResult
                            .builder()
                            .registeredService(tokenRequestContext.getRegisteredService())
                            .accessToken(accessToken)
                            .grantType(tokenRequestContext.getGrantType())
                            .responseType(tokenRequestContext.getResponseType())
                            .build();

                        val tokenResult = OAuth20AccessTokenResponseResult.builder()
                            .registeredService(tokenRequestContext.getRegisteredService())
                            .service(tokenRequestContext.getService())
                            .accessTokenTimeout(accessToken.getExpiresIn())
                            .responseType(accessToken.getResponseType())
                            .casProperties(getConfigurationContext().getCasProperties())
                            .generatedToken(accessTokenResult)
                            .grantType(accessToken.getGrantType())
                            .userProfile(userProfile)
                            .build();

                        return getConfigurationContext().getAccessTokenResponseGenerator().generate(tokenResult);
                    })
                    .orElseGet(() -> getBadRequestResponseEntity(HttpStatus.BAD_REQUEST));
            })
            .orElseGet(() -> {
                response.setHeader(HttpHeaders.WWW_AUTHENTICATE, OAuth20ClientAuthenticationInterceptor.BASIC_AUTHENTICATION_CHALLENGE);
                return getBadRequestResponseEntity(HttpStatus.UNAUTHORIZED);
            });
    }

    /**
     * Authenticate the request with the HTTP Basic credentials of the user configured for initial access tokens. Both the
     * user and the password are compared in constant time. A request without credentials, with a malformed header or with
     * other credentials is not authenticated.
     *
     * @param request the request
     * @return the profile of the user, if authenticated
     */
    protected Optional<OAuth20UserProfile> authenticate(final HttpServletRequest request) {
        val user = initialAccessTokenUser;
        val password = initialAccessTokenPassword;
        if (user == null || password == null) {
            return Optional.empty();
        }
        try {
            return OAuth20Utils.readBasicAuthorization(request)
                .filter(credentials -> {
                    val userMatches = isEqual(credentials.getKey(), user);
                    val passwordMatches = isEqual(credentials.getValue(), password);
                    return userMatches && passwordMatches;
                })
                .map(credentials -> {
                    val profile = new DefaultOAuth20UserProfile(user, OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
                    profile.addAttribute("username", user);
                    return profile;
                });
        } catch (final BadCredentialsException e) {
            LOGGER.debug("Unable to read the credentials of the initial access token request: [{}]", e.getMessage());
            return Optional.empty();
        }
    }

    private static boolean isEqual(final String given, final String expected) {
        return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
    }

    protected ModelAndView getBadRequestResponseEntity(final HttpStatus status) {
        val mv = new ModelAndView(new JacksonJsonView());
        mv.setStatus(status);
        return mv;
    }

    protected Optional<Ticket> generateInitialAccessToken(final AccessTokenRequestContext holder) {
        return FunctionUtils.doAndHandle(() -> {
            val accessTokenResult = getConfigurationContext().getAccessTokenGenerator().generate(holder);
            val accessToken = accessTokenResult.getAccessToken().orElseThrow();
            return Optional.of(getConfigurationContext().getTicketRegistry().addTicket(accessToken));
        }, e -> Optional.<Ticket>empty()).get();
    }
}
