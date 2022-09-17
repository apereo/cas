package org.apereo.cas.support.saml.services.idp.metadata.cache;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

import java.io.Serial;
import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;

/**
 * This is {@link CachedMetadataResolverResult}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SuperBuilder
@Getter
@RequiredArgsConstructor
public class CachedMetadataResolverResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -4629377820633279509L;

    @Builder.Default
    private final Instant cachedInstant = Instant.now(Clock.systemUTC());

    private final MetadataResolver metadataResolver;
}
