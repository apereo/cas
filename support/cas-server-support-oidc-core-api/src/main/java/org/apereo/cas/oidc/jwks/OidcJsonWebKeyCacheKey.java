package org.apereo.cas.oidc.jwks;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.DigestUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

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
    private static final long serialVersionUID = -1238573226470492601L;

    private final String key;

    private final String issuer;

    private final OidcJsonWebKeyUsage usage;

    private OAuthRegisteredService registeredService;

    public OidcJsonWebKeyCacheKey(final String issuer, final OidcJsonWebKeyUsage usage) {
        this.issuer = issuer;
        this.usage = usage;
        this.key = DigestUtils.sha512(this.issuer + '|' + this.usage.getValue());
        LOGGER.trace("Hashed JSON web key cache key for [{}]:[{}] as [{}]",
            this.issuer, this.usage, this.key);
        LOGGER.trace("Calculated JSON web key cache key [{}]", key);
    }

    public OidcJsonWebKeyCacheKey(final OAuthRegisteredService service, final OidcJsonWebKeyUsage usage) {
        this(service.getServiceId() + '|' + service.getClientId(), usage);
        this.registeredService = service;
    }
}
