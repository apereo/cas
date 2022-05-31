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
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;
import org.pac4j.core.credentials.password.SpringSecurityPasswordEncoder;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.service.InMemoryProfileService;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.http.client.direct.HeaderClient;
import org.pac4j.jee.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class OidcInitialAccessTokenController extends BaseOidcController {
    private final BaseClient accessTokenClient = new HeaderClient();

    public OidcInitialAccessTokenController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
        val casProperties = getConfigurationContext().getCasProperties();
        val oidcProperties = casProperties.getAuthn().getOidc();
        if (oidcProperties.getRegistration().getDynamicClientRegistrationMode().isProtected()) {
            val authProfile = new CommonProfile();
            val registration = getConfigurationContext().getCasProperties().getAuthn().getOidc().getRegistration();
            authProfile.setId(registration.getInitialAccessTokenUser());
            authProfile.addAttribute(Pac4jConstants.USERNAME, authProfile.getId());
            accessTokenClient.setCredentialsExtractor(new BasicAuthExtractor());
            val authenticator = new InMemoryProfileService<>(objects -> authProfile);
            authenticator.setPasswordEncoder(new SpringSecurityPasswordEncoder(NoOpPasswordEncoder.getInstance()));
            authenticator.create(authProfile, registration.getInitialAccessTokenPassword());
            accessTokenClient.setAuthenticator(authenticator);
            accessTokenClient.setName(UUID.randomUUID().toString());
            accessTokenClient.init();
        }
    }

    @GetMapping(value = {
        '/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL,
        "/**/" + OidcConstants.REGISTRATION_INITIAL_TOKEN_URL
    }, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        val webContext = new JEEContext(request, response);
        if (!getConfigurationContext().getIssuerService().validateIssuer(webContext, OidcConstants.REGISTRATION_INITIAL_TOKEN_URL)) {
            val body = OAuth20Utils.toJson(OAuth20Utils.getErrorResponseBody(OAuth20Constants.INVALID_REQUEST, "Invalid issuer"));
            return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }
        val casProperties = getConfigurationContext().getCasProperties();
        val oidcProperties = casProperties.getAuthn().getOidc();

        if (!oidcProperties.getRegistration().getDynamicClientRegistrationMode().isProtected()) {
            LOGGER.warn("Dynamic client registration mode is not configured as protected.");
            return getBadRequestResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        val results = accessTokenClient.getCredentials(webContext, getConfigurationContext().getSessionStore());
        return results.map(profile -> {
                val principal = PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(profile.getUserProfile().getId());
                val service = getConfigurationContext().getWebApplicationServiceServiceFactory()
                    .createService(casProperties.getServer().getPrefix());

                val holder = AccessTokenRequestContext.builder()
                    .authentication(DefaultAuthenticationBuilder.newInstance().setPrincipal(principal).build())
                    .service(service)
                    .grantType(OAuth20GrantTypes.NONE)
                    .responseType(OAuth20ResponseTypes.NONE)
                    .scopes(Set.of(OidcConstants.StandardScopes.OPENID.getScope(),
                        OidcConstants.CLIENT_REGISTRATION_SCOPE))
                    .build();
                return generateInitialAccessToken(holder)
                    .map(accessToken -> {
                        val encodedAccessToken = OAuth20JwtAccessTokenEncoder.builder()
                            .accessToken(accessToken)
                            .registeredService(holder.getRegisteredService())
                            .service(holder.getService())
                            .accessTokenJwtBuilder(getConfigurationContext().getAccessTokenJwtBuilder())
                            .casProperties(casProperties)
                            .build()
                            .encode();
                        return new ResponseEntity<>(encodedAccessToken, HttpStatus.CREATED);
                    })
                    .orElseGet(() -> getBadRequestResponseEntity(HttpStatus.BAD_REQUEST));
            })
            .orElseGet(() -> getBadRequestResponseEntity(HttpStatus.UNAUTHORIZED));
    }

    protected ResponseEntity<String> getBadRequestResponseEntity(final HttpStatus status) {
        return new ResponseEntity<>(status);
    }

    protected Optional<OAuth20AccessToken> generateInitialAccessToken(final AccessTokenRequestContext holder) {
        return FunctionUtils.doAndHandle(() -> {
            val accessTokenResult = getConfigurationContext().getAccessTokenGenerator().generate(holder);
            val accessToken = accessTokenResult.getAccessToken().get();
            getConfigurationContext().getCentralAuthenticationService().addTicket(accessToken);
            return Optional.of(accessToken);
        }, e -> Optional.<OAuth20AccessToken>empty()).get();
    }
}
