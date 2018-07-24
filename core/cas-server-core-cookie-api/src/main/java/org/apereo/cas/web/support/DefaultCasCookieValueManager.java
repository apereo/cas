package org.apereo.cas.web.support;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.util.HttpRequestUtils;

import com.google.common.base.Splitter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * The {@link DefaultCasCookieValueManager} is responsible creating
 * the CAS SSO cookie and encrypting and signing its value.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultCasCookieValueManager extends EncryptedCookieValueManager {
    private static final char COOKIE_FIELD_SEPARATOR = '@';
    private static final int COOKIE_FIELDS_LENGTH = 3;
    private static final long serialVersionUID = -2696352696382374584L;

    public DefaultCasCookieValueManager(final CipherExecutor<Serializable, Serializable> cipherExecutor) {
        super(cipherExecutor);
    }

    @Override
    protected String buildCompoundCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        val builder = new StringBuilder(givenCookieValue)
            .append(COOKIE_FIELD_SEPARATOR)
            .append(clientInfo.getClientIpAddress());

        val userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Request does not specify a user-agent");
        }
        builder.append(COOKIE_FIELD_SEPARATOR).append(userAgent);

        return builder.toString();
    }

    @Override
    protected String obtainValueFromCompoundCookie(final String cookieValue, final HttpServletRequest request) {
        val cookieParts = Splitter.on(String.valueOf(COOKIE_FIELD_SEPARATOR)).splitToList(cookieValue);
        if (cookieParts.size() != COOKIE_FIELDS_LENGTH) {
            throw new IllegalStateException("Invalid cookie. Required fields are missing");
        }
        val value = cookieParts.get(0);
        val remoteAddr = cookieParts.get(1);
        val userAgent = cookieParts.get(2);

        if (StringUtils.isBlank(value) || StringUtils.isBlank(remoteAddr) || StringUtils.isBlank(userAgent)) {
            throw new IllegalStateException("Invalid cookie. Required fields are empty");
        }

        val clientInfo = ClientInfoHolder.getClientInfo();
        if (!remoteAddr.equals(clientInfo.getClientIpAddress())) {
            throw new IllegalStateException("Invalid cookie. Required remote address "
                + remoteAddr + " does not match " + clientInfo.getClientIpAddress());
        }

        val agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (!userAgent.equals(agent)) {
            throw new IllegalStateException("Invalid cookie. Required user-agent " + userAgent + " does not match " + agent);
        }
        return value;
    }
}
