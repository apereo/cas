package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.crypto.CipherExecutorResolver;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.cookie.CookieSameSitePolicy;
import org.apereo.cas.web.support.InvalidCookieException;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.io.Serial;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * The {@link DefaultCasCookieValueManager} is responsible for creating
 * the CAS SSO cookie and encrypting and signing its value.
 * <p>
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

    @Serial
    private static final long serialVersionUID = -2696352696382374584L;

    private final PinnableCookieProperties cookieProperties;

    private final ObjectProvider<GeoLocationService> geoLocationService;

    public DefaultCasCookieValueManager(final CipherExecutorResolver cipherExecutorResolver,
                                        final TenantExtractor tenantExtractor,
                                        final ObjectProvider<GeoLocationService> geoLocationService,
                                        final CookieSameSitePolicy cookieSameSitePolicy,
                                        final PinnableCookieProperties cookieProperties) {
        super(cipherExecutorResolver, tenantExtractor, cookieSameSitePolicy);
        this.geoLocationService = geoLocationService;
        this.cookieProperties = cookieProperties;
    }

    @Override
    protected String buildCompoundCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        val builder = new StringBuilder(givenCookieValue);

        if (cookieProperties.isPinToSession()) {
            val clientInfo = ClientInfoHolder.getClientInfo();
            if (clientInfo != null) {
                val clientLocation = cookieProperties.isGeoLocateClientSession()
                    ? getClientGeoLocation(clientInfo)
                    : clientInfo.getClientIpAddress();
                builder.append(COOKIE_FIELD_SEPARATOR).append(clientLocation);
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

    private String getClientGeoLocation(final ClientInfo clientInfo) {
        return geoLocationService
            .stream()
            .map(service -> {
                val geoLocation = service.locate(clientInfo.getClientIpAddress());
                if (geoLocation != null && geoLocation.getAddresses() != null && !geoLocation.getAddresses().isEmpty()) {
                    return org.springframework.util.StringUtils.collectionToCommaDelimitedString(geoLocation.getAddresses());
                }
                return clientInfo.getClientIpAddress();
            })
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(clientInfo::getClientIpAddress);
    }

    @Override
    protected String obtainValueFromCompoundCookie(final String value, final HttpServletRequest request) {
        val cookieParts = Splitter.on(String.valueOf(COOKIE_FIELD_SEPARATOR)).splitToList(value);

        val cookieValue = cookieParts.getFirst();
        if (!cookieProperties.isPinToSession()) {
            LOGGER.trace("Cookie session-pinning is disabled. Returning cookie value as it was provided");
            return cookieValue;
        }

        if (cookieParts.size() != COOKIE_FIELDS_LENGTH) {
            throw new InvalidCookieException("Invalid cookie. Required fields are missing");
        }
        val cookieClientLocationOrIp = cookieParts.get(1);
        val cookieUserAgent = cookieParts.get(2);

        if (Stream.of(cookieValue, cookieClientLocationOrIp, cookieUserAgent).anyMatch(StringUtils::isBlank)) {
            throw new InvalidCookieException("Invalid cookie. Required fields are empty");
        }

        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo == null) {
            val message = "Unable to match required remote address %s because client ip at time of cookie creation is unknown".formatted(cookieClientLocationOrIp);
            LOGGER.warn(message);
            throw new InvalidCookieException(message);
        }

        if (cookieProperties.isGeoLocateClientSession()) {
            val clientLocationOrIp = getClientGeoLocation(clientInfo);
            if (!cookieClientLocationOrIp.equals(clientLocationOrIp)) {
                val message = "Invalid cookie. Required remote address %s does not match %s".formatted(cookieClientLocationOrIp, clientLocationOrIp);
                LOGGER.warn(message);
                throw new InvalidCookieException(message);
            }
        } else {
            val clientIpAddress = clientInfo.getClientIpAddress();
            if (!cookieClientLocationOrIp.equals(clientIpAddress)) {
                if (StringUtils.isBlank(cookieProperties.getAllowedIpAddressesPattern())
                    || !RegexUtils.find(cookieProperties.getAllowedIpAddressesPattern(), clientIpAddress)) {
                    val message = "Invalid cookie. Required remote address %s does not match %s".formatted(cookieClientLocationOrIp, clientIpAddress);
                    LOGGER.warn(message);
                    throw new InvalidCookieException(message);
                }
                LOGGER.debug("Required remote address [{}] does not match [{}], but it's authorized to proceed",
                    cookieClientLocationOrIp, clientIpAddress);
            }
        }

        val agent = HttpRequestUtils.getHttpServletRequestUserAgent(request);
        if (!cookieUserAgent.equals(agent)) {
            val message = "Invalid cookie. Required user-agent %s does not match %s".formatted(cookieUserAgent, agent);
            LOGGER.warn(message);
            throw new InvalidCookieException(message);
        }
        return cookieValue;
    }
}
