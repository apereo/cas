package org.apereo.cas.oidc.web.controllers.dynareg;

import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.web.controllers.BaseOidcController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
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
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.service.InMemoryProfileService;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * This is {@link OidcInitialAccessTokenController}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Slf4j
@Tag(name = "OpenID Connect")
public class OidcInitialAccessTokenController extends BaseOidcController {
    private final BaseClient accessTokenClient = new HeaderClient();

    public OidcInitialAccessTokenController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
        val casProperties = getConfigurationContext().getCasProperties();
        val oidcProperties = casProperties.getAuthn().getOidc();

        if (oidcProperties.getRegistration().getDynamicClientRegistrationMode().isProtected()) {
            val authProfile = new CommonProfile();
            val registration = getConfigurationContext().getCasProperties().getAuthn().getOidc().getRegistration();
            authProfile.setId(StringUtils.defaultIfBlank(registration.getInitialAccessTokenUser(),
                RandomUtils.randomAlphabetic(8)));
            authProfile.addAttribute(Pac4jConstants.USERNAME, authProfile.getId());
            accessTokenClient.setCredentialsExtractor(new BasicAuthExtractor());
            val authenticator = new InMemoryProfileService<>(objects -> authProfile);
            authenticator.setPasswordEncoder(new SpringSecurityPasswordEncoder(NoOpPasswordEncoder.getInstance()));
            authenticator.init();
            authenticator.create(authProfile, StringUtils.defaultIfBlank(registration.getInitialAccessTokenPassword(),
                RandomUtils.randomAlphabetic(8)));
            accessTokenClient.setAuthenticator(authenticator);
            accessTokenClient.setName(UUID.randomUUID().toString());
            accessTokenClient.init();
        }
    }

    /**
     * Handle request..
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
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)) {
            val body = OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer");
            val modelAndView = new ModelAndView(new MappingJackson2JsonView(), body);
            modelAndView.setStatus(HttpStatus.BAD_REQUEST);
            return modelAndView;
        }
        val casProperties = getConfigurationContext().getCasProperties();
        val oidcProperties = casProperties.getAuthn().getOidc();

        if (!oidcProperties.getRegistration().getDynamicClientRegistrationMode().isProtected()) {
            LOGGER.warn("Dynamic client registration mode is not configured as protected.");
            return getBadRequestResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        val callContext = new CallContext(webContext, getConfigurationContext().getSessionStore(),
            getConfigurationContext().getOauthConfig().getProfileManagerFactory());
        return accessTokenClient.getCredentials(callContext)
            .map(credentials -> accessTokenClient.validateCredentials(callContext, credentials))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(credentials -> {
                val principal = FunctionUtils.doUnchecked(() -> PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(credentials.getUserProfile().getId()));
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
                            .userProfile(credentials.getUserProfile())
                            .build();

                        return getConfigurationContext().getAccessTokenResponseGenerator().generate(tokenResult);
                    })
                    .orElseGet(() -> getBadRequestResponseEntity(HttpStatus.BAD_REQUEST));
            })
            .orElseGet(() -> getBadRequestResponseEntity(HttpStatus.UNAUTHORIZED));
    }

    protected ModelAndView getBadRequestResponseEntity(final HttpStatus status) {
        val mv = new ModelAndView(new MappingJackson2JsonView());
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
