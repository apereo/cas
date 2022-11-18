package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.web.response.accesstoken.response.OAuth20JwtAccessTokenEncoder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;


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
    private final T configurationContext;

    /**
     * Extract access token from token.
     *
     * @param token the token
     * @return the string
     */
    protected String extractAccessTokenFrom(final String token) {
        return OAuth20JwtAccessTokenEncoder.builder()
            .accessTokenJwtBuilder(getConfigurationContext().getAccessTokenJwtBuilder())
            .build()
            .decode(token);
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
}
