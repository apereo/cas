package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SuperBuilder;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import jakarta.validation.constraints.NotNull;

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

    @NotNull
    private final MetadataResolver metadataResolver;

    /*
     * The entity descriptor already resolved for the request this result was handed to, so the caller does not resolve
     * it again. It is set on a per-request copy and never on the cached entry.
     */
    @JsonIgnore
    @Getter(AccessLevel.NONE)
    private final transient @Nullable EntityDescriptor entityDescriptor;

    /**
     * Determines whether the metadata resolver is available.
     *
     * @return true/false
     */
    @JsonIgnore
    public boolean isResolved() {
        return metadataResolver != null;
    }

    @JsonIgnore
    public Optional<EntityDescriptor> getEntityDescriptor() {
        return Optional.ofNullable(entityDescriptor);
    }
}
