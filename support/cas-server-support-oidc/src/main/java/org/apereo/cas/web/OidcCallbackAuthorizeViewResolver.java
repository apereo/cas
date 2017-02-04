package org.apereo.cas.web;

import com.google.common.base.Throwables;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.OidcConstants;
import org.apereo.cas.OidcServerDiscoverySettings;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.OAuthResponseTypes;
import org.apereo.cas.support.oauth.util.OAuthUtils;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OidcCallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcCallbackAuthorizeViewResolver.class);

    private final OidcAuthorizationRequestSupport authorizationRequestSupport;
    private final ServicesManager servicesManager;
    private final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    public OidcCallbackAuthorizeViewResolver(final OidcAuthorizationRequestSupport authorizationRequestSupport,
                                             final ServicesManager servicesManager,
                                             final OidcServerDiscoverySettings oidcServerDiscoverySettings) {
        this.authorizationRequestSupport = authorizationRequestSupport;
        this.servicesManager = servicesManager;
        this.oidcServerDiscoverySettings = oidcServerDiscoverySettings;
    }

    @Override
    public ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String resolvedUrl) {
        final Set<String> prompt = authorizationRequestSupport.getOidcPromptFromAuthorizationRequest(resolvedUrl);

        final String url;
        if (isRespondingToImplicit(ctx, manager, resolvedUrl)) {
            url = buildRedirectUrl(ctx, manager, resolvedUrl);
        } else {
            url = resolvedUrl;
        }

        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
            if (manager.get(true) != null) {
                return new ModelAndView(url);
            }
            final Map<String, String> model = new HashMap<>();
            model.put(OAuthConstants.ERROR, OidcConstants.LOGIN_REQUIRED);
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }
        return new ModelAndView(new RedirectView(url));
    }

    private boolean isRespondingToImplicit(final J2EContext ctx,
                                           final ProfileManager manager,
                                           final String resolvedUrl) {
        try {
            final URIBuilder builder = new URIBuilder(resolvedUrl);
            final boolean foundMatch = builder.getQueryParams().stream().anyMatch(p -> p.getName().equals(OAuthConstants.RESPONSE_TYPE)
                    && p.getValue().equalsIgnoreCase(OAuthResponseTypes.IDTOKEN_TOKEN.getType()));

            final OidcRegisteredService service = getOidcRegisteredService(ctx);
            return service != null && service.isImplicit() && foundMatch;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private OidcRegisteredService getOidcRegisteredService(final J2EContext ctx) {
        final String clientId = ctx.getRequestParameter(OAuthConstants.CLIENT_ID);
        return (OidcRegisteredService) OAuthUtils.getRegisteredOAuthService(this.servicesManager, clientId);
    }

    private String getAccessToken(final J2EContext ctx, final ProfileManager manager) {
        try {
            final OidcRegisteredService service = getOidcRegisteredService(ctx);
            final String authCode = ctx.getRequestParameter(OAuthConstants.CODE);
            final String redirectURI = ctx.getRequestParameter(OAuthConstants.REDIRECT_URI);

            final TokenRequest tokenReq = new TokenRequest(new URI(oidcServerDiscoverySettings.getTokenEndpoint()),
                    new ClientSecretBasic(new ClientID(service.getClientId()), new Secret(service.getClientSecret())),
                    new AuthorizationCodeGrant(new AuthorizationCode(authCode), new URI(redirectURI)));

            final HTTPResponse tokenHTTPResp = tokenReq.toHTTPRequest().send();
            final TokenResponse tokenResponse = OIDCTokenResponseParser.parse(tokenHTTPResp);

            if (tokenResponse instanceof TokenErrorResponse) {
                final ErrorObject error = ((TokenErrorResponse) tokenResponse).getErrorObject();
            }

            final OIDCTokenResponse accessTokenResponse = (OIDCTokenResponse) tokenResponse;


        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private String buildRedirectUrl(final J2EContext ctx, final ProfileManager manager, final String url) {
        return null;
    }
}
