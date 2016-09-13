package org.apereo.cas.trusted;

import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

/**
 * This is {@link MultifactorAuthenticationTrustUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public final class MultifactorAuthenticationTrustUtils {

    private MultifactorAuthenticationTrustUtils() {
    }

    /**
     * Generate geography.
     *
     * @return the geography
     */
    public static String generateGeography() {
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String geography = clientInfo.getClientIpAddress().concat("@").concat(WebUtils.getHttpServletRequestUserAgent());
        return geography;
    }
}
