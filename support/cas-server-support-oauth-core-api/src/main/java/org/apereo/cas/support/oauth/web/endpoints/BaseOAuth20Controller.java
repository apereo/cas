package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.stereotype.Controller;

import jakarta.servlet.http.HttpServletRequest;


/**
 * This controller is the base controller for wrapping OAuth protocol in CAS.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Controller
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Slf4j
public abstract class BaseOAuth20Controller<T extends OAuth20ConfigurationContext> {
    protected final T configurationContext;

    protected OAuth20AccessToken resolveAccessToken(final Ticket givenAccessToken) {
        return resolveToken(givenAccessToken, OAuth20AccessToken.class);
    }

    protected <U extends Ticket> U resolveToken(final Ticket token, final Class<U> clazz) {
        return token.isStateless()
            ? configurationContext.getTicketRegistry().getTicket(token.getId(), clazz)
            : clazz.cast(token);
    }

    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.toDecodableCipher(getConfigurationContext().getAccessTokenJwtBuilder()).decode(token);
    }

    protected void ensureSessionReplicationIsAutoconfiguredIfNeedBe(final HttpServletRequest request) {
        val replicationProps = getConfigurationContext().getCasProperties().getAuthn().getPac4j().getCore().getSessionReplication();
        val cookieAutoconfigured = replicationProps.getCookie().isAutoConfigureCookiePath();
        if (replicationProps.isReplicateSessions() && cookieAutoconfigured) {
            val contextPath = request.getContextPath();
            val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

            val path = getConfigurationContext().getOauthDistributedSessionCookieGenerator().getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for OAuth distributed session cookie generator to: [{}]", cookiePath);
                getConfigurationContext().getOauthDistributedSessionCookieGenerator().setCookiePath(cookiePath);
            } else {
                LOGGER.trace("OAuth distributed cookie domain is [{}] with path [{}]",
                    getConfigurationContext().getOauthDistributedSessionCookieGenerator().getCookieDomain(), path);
            }
        }
    }

    protected boolean isRequestAuthenticated(final ProfileManager manager, final WebContext context,
                                             final OAuthRegisteredService registeredService) {
        return manager.getProfile().isPresent();
    }

    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
    }

    /**
     * Is the OAuth token a Refresh Token?
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    protected static boolean isRefreshToken(final OAuth20Token token) {
        return token instanceof OAuth20RefreshToken;
    }

    /**
     * Is the OAuth token an Access Token?
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    protected static boolean isAccessToken(final OAuth20Token token) {
        return token instanceof OAuth20AccessToken;
    }

    protected void revokeToken(final OAuth20RefreshToken token) throws Exception {
        revokeToken(token.getId());
        token.getAccessTokens().forEach(Unchecked.consumer(this::revokeToken));
    }

    protected void revokeToken(final String token) throws Exception {
        LOGGER.debug("Revoking token [{}]", token);
        getConfigurationContext().getTicketRegistry().deleteTicket(token);
    }

}
