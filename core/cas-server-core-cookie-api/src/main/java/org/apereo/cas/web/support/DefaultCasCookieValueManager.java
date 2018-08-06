package org.apereo.cas.web.support;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.util.HttpRequestUtils;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.List;

/**
 * The {@link DefaultCasCookieValueManager} is responsible creating
 * the CAS SSO cookie and encrypting and signing its value.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class DefaultCasCookieValueManager extends EncryptedCookieValueManager {
    private static final char COOKIE_FIELD_SEPARATOR = '@';
    private static final int COOKIE_FIELDS_LENGTH = 3;

    private final CookieProperties cookieProperties;

    public DefaultCasCookieValueManager(final CipherExecutor<Serializable, Serializable> cipherExecutor,
                                        final CookieProperties cookieProperties) {
        super(cipherExecutor);
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected String buildCompoundCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final StringBuilder builder = new StringBuilder(givenCookieValue);

        if (cookieProperties.isPinToSession()) {
            builder.append(COOKIE_FIELD_SEPARATOR).append(clientInfo.getClientIpAddress());

            final String userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
            if (StringUtils.isBlank(userAgent)) {
                throw new IllegalStateException("Request does not specify a user-agent");
            }
            builder.append(COOKIE_FIELD_SEPARATOR).append(userAgent);
        } else {
            LOGGER.debug("Cookie session-pinning is disabled");
        }

        return builder.toString();
    }

    @Override
    protected String obtainValueFromCompoundCookie(final String cookieValue, final HttpServletRequest request) {
        final List<String> cookieParts = Splitter.on(String.valueOf(COOKIE_FIELD_SEPARATOR)).splitToList(cookieValue);
        if (cookieParts.isEmpty()) {
            throw new IllegalStateException("Invalid empty cookie");
        }
        final String value = cookieParts.get(0);
        if (!cookieProperties.isPinToSession()) {
            LOGGER.debug("Cookie session-pinning is disabled. Returning cookie value as it was provided");
            return value;
        }

        if (cookieParts.size() != COOKIE_FIELDS_LENGTH) {
            throw new IllegalStateException("Invalid cookie. Required fields are missing");
        }
        final String remoteAddr = cookieParts.get(1);
        final String userAgent = cookieParts.get(2);

        if (StringUtils.isBlank(value) || StringUtils.isBlank(remoteAddr) || StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Invalid cookie. Required fields are empty");
        }

        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        if (!remoteAddr.equals(clientInfo.getClientIpAddress())) {
            throw new IllegalStateException("Invalid cookie. Required remote address "
                + remoteAddr + " does not match " + clientInfo.getClientIpAddress());
        }

        final String agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (!userAgent.equals(agent)) {
            throw new IllegalStateException("Invalid cookie. Required user-agent " + userAgent + " does not match " + agent);
        }
        return value;
    }
}
