package org.apereo.cas.support.wsfederation;

import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.support.wsfederation.attributes.WsFederationAttributeMutator;
import org.apereo.cas.web.cookie.CasCookieBuilder;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class gathers configuration information for the WS Federation Identity Provider.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Getter
@Setter
public class WsFederationConfiguration implements Serializable {

    private static final long serialVersionUID = 2310859477512242659L;

    private static final String QUERYSTRING = "?wa=wsignin1.0&wtrealm=%s&wctx=%s";

    private transient Resource encryptionPrivateKey;

    private transient Resource encryptionCertificate;

    private String encryptionPrivateKeyPassword;

    private String identityAttribute;

    private String identityProviderIdentifier;

    private String identityProviderUrl;

    private String relyingPartyIdentifier;

    private long tolerance;

    private DelegationAutoRedirectTypes autoRedirectType = DelegationAutoRedirectTypes.NONE;

    private WsFedPrincipalResolutionAttributesType attributesType;

    private WsFederationAttributeMutator attributeMutator;

    private String name;

    private String id = UUID.randomUUID().toString();

    private transient CasCookieBuilder cookieGenerator;

    private String signingCertificates;

    /**
     * Gets authorization url.
     *
     * @param relyingPartyIdentifier the relying party identifier
     * @param wctx                   the wctx
     * @return the authorization url
     */
    public String getAuthorizationUrl(final String relyingPartyIdentifier, final String wctx) {
        return String.format(getIdentityProviderUrl() + QUERYSTRING, relyingPartyIdentifier, wctx);
    }

    public String getName() {
        return StringUtils.isBlank(this.name) ? getClass().getSimpleName() : this.name;
    }

    /**
     * Describes how the WS-FED principal resolution machinery
     * should process attributes from WS-FED.
     */
    public enum WsFedPrincipalResolutionAttributesType {

        /**
         * CAS ws fed principal resolution attributes type.
         */
        CAS,
        /**
         * Wsfed ws fed principal resolution attributes type.
         */
        WSFED,
        /**
         * Both ws fed principal resolution attributes type.
         */
        BOTH
    }
}
