package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SuperBuilder;
import lombok.val;
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
     * The local files the metadata was read from, if any. A directory stands for the files inside it.
     */
    @JsonIgnore
    @Builder.Default
    private final transient List<File> metadataSourceFiles = List.of();

    /**
     * When each local source file was last modified, as of loading the metadata.
     */
    @JsonIgnore
    @Builder.Default
    private final transient Map<String, Long> metadataSourceFilesSnapshot = Map.of();

    /**
     * When the local source files were last compared with {@link #metadataSourceFilesSnapshot}, in epoch milliseconds.
     */
    @JsonIgnore
    @Builder.Default
    @Getter(AccessLevel.NONE)
    private final transient AtomicLong metadataSourceFilesCheckedAt = new AtomicLong(System.currentTimeMillis());

    /**
     * Whether the metadata holds exactly one service provider, as of loading it. Such metadata may have been replaced
     * at its source when it no longer holds the requested entity, whereas an aggregate simply does not hold it.
     */
    @JsonIgnore
    private final transient boolean singleServiceProvider;

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

    /**
     * Takes a snapshot of when local metadata files were last modified: each file, and each directory together with
     * every file directly inside it, so that a file that is added or removed changes the snapshot as well.
     * A file that does not exist is recorded as such.
     *
     * @param files the files
     * @return the snapshot
     */
    public static Map<String, Long> snapshotOf(final Collection<File> files) {
        val snapshot = new TreeMap<String, Long>();
        for (val file : files) {
            snapshot.put(file.getAbsolutePath(), file.exists() ? file.lastModified() : -1L);
            if (file.isDirectory()) {
                val children = file.listFiles(File::isFile);
                if (children != null) {
                    for (val child : children) {
                        snapshot.put(child.getAbsolutePath(), child.lastModified());
                    }
                }
            }
        }
        return snapshot;
    }

    /**
     * Whether a local file the metadata was read from has changed since the metadata was loaded. The files are
     * compared at most once per the given interval, and only by one of the callers that ask at the same time;
     * the others are told nothing changed.
     *
     * @param interval the least time between two comparisons
     * @return true if a file was changed, added or removed
     */
    @JsonIgnore
    public boolean haveMetadataSourceFilesChanged(final Duration interval) {
        if (metadataSourceFiles.isEmpty()) {
            return false;
        }
        val now = System.currentTimeMillis();
        val lastChecked = metadataSourceFilesCheckedAt.get();
        if (now - lastChecked < interval.toMillis() || !metadataSourceFilesCheckedAt.compareAndSet(lastChecked, now)) {
            return false;
        }
        return !snapshotOf(metadataSourceFiles).equals(metadataSourceFilesSnapshot);
    }
}
