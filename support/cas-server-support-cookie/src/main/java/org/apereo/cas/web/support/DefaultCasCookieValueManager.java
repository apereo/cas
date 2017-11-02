package org.apereo.cas.web.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * The {@link DefaultCasCookieValueManager} is responsible creating
 * the CAS SSO cookie and encrypting and signing its value.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultCasCookieValueManager implements CookieValueManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCasCookieValueManager.class);
    private static final char COOKIE_FIELD_SEPARATOR = '@';
    private static final int COOKIE_FIELDS_LENGTH = 3;

    /**
     * The cipher exec that is responsible for encryption and signing of the cookie.
     */
    private final CipherExecutor<Serializable, Serializable> cipherExecutor;

    /**
     * Instantiates a new Cas cookie value manager.
     *
     * @param cipherExecutor the cipher executor
     */
    public DefaultCasCookieValueManager(final CipherExecutor cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final StringBuilder builder = new StringBuilder(givenCookieValue)
                .append(COOKIE_FIELD_SEPARATOR)
                .append(clientInfo.getClientIpAddress());

        final String userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Request does not specify a user-agent");
        }
        builder.append(COOKIE_FIELD_SEPARATOR).append(userAgent);

        final String res = builder.toString();
        LOGGER.debug("Encoding cookie value [{}]", res);
        return this.cipherExecutor.encode(res).toString();
    }

    @Override
    public String obtainCookieValue(final Cookie cookie, final HttpServletRequest request) {
        final String cookieValue = this.cipherExecutor.decode(cookie.getValue()).toString();
        LOGGER.debug("Decoded cookie value is [{}]", cookieValue);
        if (StringUtils.isBlank(cookieValue)) {
            LOGGER.debug("Retrieved decoded cookie value is blank. Failed to decode cookie [{}]", cookie.getName());
            return null;
        }

        final String[] cookieParts = cookieValue.split(String.valueOf(COOKIE_FIELD_SEPARATOR));
        if (cookieParts.length != COOKIE_FIELDS_LENGTH) {
            throw new IllegalStateException("Invalid cookie. Required fields are missing");
        }
        final String value = cookieParts[0];
        final String remoteAddr = cookieParts[1];
        final String userAgent = cookieParts[2];

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
