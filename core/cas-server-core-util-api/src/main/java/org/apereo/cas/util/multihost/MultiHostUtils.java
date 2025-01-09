package org.apereo.cas.util.multihost;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.oidc.OidcProperties;
import org.apereo.cas.multihost.MultiHostClientInfo;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;

/**
 * Helper for the multi-hosts feature.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@UtilityClass
@Slf4j
public class MultiHostUtils {

    /**
     * Compute the login URL in a multi-hosts environment.
     *
     * @param casProperties the CAS configuration properties
     * @return the login URL
     */
    public static String computeLoginUrl(final CasConfigurationProperties casProperties) {
        return computeServerPrefix(casProperties).concat(CasProtocolConstants.ENDPOINT_LOGIN);
    }

    /**
     * Compute the prefix URL in a multi-hosts environment.
     *
     * @param casProperties the CAS configuration properties
     * @return the prefix URL
     */
    public static String computeServerPrefix(final CasConfigurationProperties casProperties) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo instanceof final MultiHostClientInfo mhClientInfo) {
            LOGGER.trace("Using current host server prefix: [{}]", mhClientInfo.getCurrentHost().getServerPrefix());
            return mhClientInfo.getCurrentHost().getServerPrefix();
        }
        LOGGER.trace("Using default (primary) server prefix: [{}]", casProperties.getServer().getPrefix());
        return casProperties.getServer().getPrefix();
    }

    /**
     * Compute the OIDC issuer in a multi-hosts environment.
     *
     * @param oidcProperties the OIDC configuration properties
     * @return the OIDC issuer
     */
    public static String computeOidcIssuer(final OidcProperties oidcProperties) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo instanceof final MultiHostClientInfo mhClientInfo) {
            LOGGER.trace("Using current host OIDC issuer: [{}]", mhClientInfo.getCurrentHost().getOidcIssuer());
            return mhClientInfo.getCurrentHost().getOidcIssuer();
        }
        LOGGER.trace("Using default (primary) OIDC issuer: [{}]", oidcProperties.getCore().getIssuer());
        return oidcProperties.getCore().getIssuer();
    }
}
