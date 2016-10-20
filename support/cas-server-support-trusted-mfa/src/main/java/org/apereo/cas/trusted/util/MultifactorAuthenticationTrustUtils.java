package org.apereo.cas.trusted.util;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;

/**
 * This is {@link MultifactorAuthenticationTrustUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class MultifactorAuthenticationTrustUtils {

    private MultifactorAuthenticationTrustUtils() {
    }

    /**
     * Generate key.
     *
     * @param r the r
     * @return the geography
     */
    public static String generateKey(final MultifactorAuthenticationTrustRecord r) {
        final StringBuilder builder = new StringBuilder(r.getPrincipal());
        return builder.append("@")
                      .append(r.getGeography())
                      .toString(); 
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
