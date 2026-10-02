package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link WebAuthnUtils}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@UtilityClass
class WebAuthnUtils {
    /**
     * Web authn origins set.
     *
     * @param casProperties the cas properties
     * @return the set
     */
    static Set<String> determineAllowedOrigins(final CasConfigurationProperties casProperties) {
        val origins = new LinkedHashSet<String>();
        origins.add(casProperties.getServer().getName());
        val allowedOrigins = casProperties.getAuthn().getMfa().getWebAuthn().getCore().getAllowedOrigins();
        if (StringUtils.isNotBlank(allowedOrigins)) {
            origins.addAll(org.springframework.util.StringUtils.commaDelimitedListToSet(allowedOrigins));
        }
        return origins;
    }
}
