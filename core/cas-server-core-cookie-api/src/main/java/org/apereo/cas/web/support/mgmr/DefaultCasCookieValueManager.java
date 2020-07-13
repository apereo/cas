package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.support.InvalidCookieException;

import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * The {@link DefaultCasCookieValueManager} is responsible for creating
 * the CAS SSO cookie and encrypting and signing its value.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class DefaultCasCookieValueManager extends EncryptedCookieValueManager {
    private static final char COOKIE_FIELD_SEPARATOR = '@';
    private static final int COOKIE_FIELDS_LENGTH = 3;
    private static final long serialVersionUID = -2696352696382374584L;

    private final CookieProperties cookieProperties;

    public DefaultCasCookieValueManager(final CipherExecutor<Serializable, Serializable> cipherExecutor,
                                        final CookieProperties cookieProperties) {
        super(cipherExecutor);
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected String buildCompoundCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        val builder = new StringBuilder(givenCookieValue);

        if (cookieProperties.isPinToSession()) {
            val clientInfo = ClientInfoHolder.getClientInfo();
            builder.append(COOKIE_FIELD_SEPARATOR).append(clientInfo.getClientIpAddress());

            val userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
            if (StringUtils.isBlank(userAgent)) {
                throw new IllegalStateException("Request does not specify a user-agent");
            }
            builder.append(COOKIE_FIELD_SEPARATOR).append(userAgent);
        } else {
            LOGGER.trace("Cookie session-pinning is disabled");
        }

        return builder.toString();
    }

    @Override
    protected String obtainValueFromCompoundCookie(final String cookieValue, final HttpServletRequest request) {
        val cookieParts = Splitter.on(String.valueOf(COOKIE_FIELD_SEPARATOR)).splitToList(cookieValue);
        val value = cookieParts.get(0);
        if (!cookieProperties.isPinToSession()) {
            LOGGER.trace("Cookie session-pinning is disabled. Returning cookie value as it was provided");
            return value;
        }

        if (cookieParts.size() != COOKIE_FIELDS_LENGTH) {
            throw new InvalidCookieException("Invalid cookie. Required fields are missing");
        }
        val remoteAddr = cookieParts.get(1);
        val userAgent = cookieParts.get(2);

        if (Stream.of(value, remoteAddr, userAgent).anyMatch(StringUtils::isBlank)) {
            throw new InvalidCookieException("Invalid cookie. Required fields are empty");
        }

        val clientInfo = ClientInfoHolder.getClientInfo();
        if (!remoteAddr.equals(clientInfo.getClientIpAddress())) {
            throw new InvalidCookieException("Invalid cookie. Required remote address "
                + remoteAddr + " does not match " + clientInfo.getClientIpAddress());
        }

        val agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (!userAgent.equals(agent)) {
            throw new InvalidCookieException("Invalid cookie. Required user-agent " + userAgent + " does not match " + agent);
        }
        return value;
    }
}
