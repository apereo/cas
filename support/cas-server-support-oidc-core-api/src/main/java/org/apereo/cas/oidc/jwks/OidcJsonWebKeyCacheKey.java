package org.apereo.cas.oidc.jwks;

import module java.base;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.DigestUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcJsonWebKeyCacheKey}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@ToString
@EqualsAndHashCode(of = "key")
@Getter
public class OidcJsonWebKeyCacheKey implements Serializable {
    @Serial
    private static final long serialVersionUID = -1238573226470492601L;

    private final String key;

    private final String issuer;

    private final OidcJsonWebKeyUsage usage;

    private OAuthRegisteredService registeredService;

    public OidcJsonWebKeyCacheKey(final String issuer, final OidcJsonWebKeyUsage usage) {
        this(issuer, usage, null);
    }

    /**
     * Cache key for the keys of a registered service. The keys the service defines, or where it finds them, are part
     * of the cache key, so a service saved again with other keys, as when an OpenID Federation relying party is
     * resolved again after it rolls its keys over, is not answered with the keys cached for its earlier definition.
     *
     * @param service the registered service
     * @param usage   the key usage
     */
    public OidcJsonWebKeyCacheKey(final OAuthRegisteredService service, final OidcJsonWebKeyUsage usage) {
        this(service.getServiceId() + '|' + service.getClientId(), usage,
            service instanceof final OidcRegisteredService oidcService ? oidcService.getJwks() + '|' + oidcService.getJwksKeyId() : null);
        this.registeredService = service;
    }

    private OidcJsonWebKeyCacheKey(final String issuer, final OidcJsonWebKeyUsage usage, @Nullable final String keys) {
        this.issuer = issuer;
        this.usage = usage;
        this.key = DigestUtils.sha512(keys == null ? this.issuer + '|' + this.usage.getValue() : this.issuer + '|' + this.usage.getValue() + '|' + keys);
        LOGGER.trace("Hashed JSON web key cache key for [{}]:[{}] as [{}]", this.issuer, this.usage, this.key);
    }
}
