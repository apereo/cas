package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.RegexUtils;
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
 * This class by default ({@code CookieProperties.isPinToSession=true}) ensures the cookie is used on a
 * request from same IP and with the same user-agent as when cookie was created.
 * The client info (with original client ip) may be null if cluster failover occurs and session replication not working.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
public class DefaultCasCookieValueManager extends EncryptedCookieValueManager {
    private static final char COOKIE_FIELD_SEPARATOR = '@';
    private static final int COOKIE_FIELDS_LENGTH = 3;
    private static final long serialVersionUID = -2696352696382374584L;

    private final PinnableCookieProperties cookieProperties;

    public DefaultCasCookieValueManager(final CipherExecutor<Serializable, Serializable> cipherExecutor,
                                        final PinnableCookieProperties cookieProperties) {
        super(cipherExecutor);
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected String buildCompoundCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        val builder = new StringBuilder(givenCookieValue);

        if (cookieProperties.isPinToSession()) {
            val clientInfo = ClientInfoHolder.getClientInfo();
            if (clientInfo != null) {
                builder.append(COOKIE_FIELD_SEPARATOR).append(clientInfo.getClientIpAddress());
            }
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
    protected String obtainValueFromCompoundCookie(final String value, final HttpServletRequest request) {
        val cookieParts = Splitter.on(String.valueOf(COOKIE_FIELD_SEPARATOR)).splitToList(value);

        val cookieValue = cookieParts.get(0);
        if (!cookieProperties.isPinToSession()) {
            LOGGER.trace("Cookie session-pinning is disabled. Returning cookie value as it was provided");
            return cookieValue;
        }

        if (cookieParts.size() != COOKIE_FIELDS_LENGTH) {
            throw new InvalidCookieException("Invalid cookie. Required fields are missing");
        }
        val cookieIpAddress = cookieParts.get(1);
        val cookieUserAgent = cookieParts.get(2);

        if (Stream.of(cookieValue, cookieIpAddress, cookieUserAgent).anyMatch(StringUtils::isBlank)) {
            throw new InvalidCookieException("Invalid cookie. Required fields are empty");
        }

        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo == null) {
            throw new InvalidCookieException("Unable to match required remote address "
                    + cookieIpAddress + " because client ip at time of cookie creation is unknown");
        }

        if (!cookieIpAddress.equals(clientInfo.getClientIpAddress())) {
            if (StringUtils.isBlank(cookieProperties.getAllowedIpAddressesPattern())
                || !RegexUtils.find(cookieProperties.getAllowedIpAddressesPattern(), clientInfo.getClientIpAddress())) {
                throw new InvalidCookieException("Invalid cookie. Required remote address "
                    + cookieIpAddress + " does not match " + clientInfo.getClientIpAddress());
            }
            LOGGER.debug("Required remote address [{}] does not match [{}], but it's authorized proceed",
                cookieIpAddress, clientInfo.getClientIpAddress());
        }

        val agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (!cookieUserAgent.equals(agent)) {
            throw new InvalidCookieException("Invalid cookie. Required user-agent " + cookieUserAgent + " does not match " + agent);
        }
        return cookieValue;
    }
}
