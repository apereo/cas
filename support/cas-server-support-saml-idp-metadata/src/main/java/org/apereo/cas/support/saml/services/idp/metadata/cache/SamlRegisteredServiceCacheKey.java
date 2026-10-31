package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.DigestUtils;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.criterion.EntityIdCriterion;

/**
 * This is {@link SamlRegisteredServiceCacheKey}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@ToString
@EqualsAndHashCode(of = "id")
@Getter
public class SamlRegisteredServiceCacheKey implements Serializable {
    /**
     * Cache key field separator.
     */
    static final String KEY_SEPARATOR = "|";

    private static final Pattern PATTERN_METADATA_SOURCE_ID = Pattern.compile("^[a-z][a-z0-9+.-]*://$", Pattern.CASE_INSENSITIVE);

    @Serial
    private static final long serialVersionUID = -7238573226470492601L;
    
    private final String id;

    private final SamlRegisteredService registeredService;

    private final transient CriteriaSet criteriaSet;

    @Getter(AccessLevel.PACKAGE)
    private final String cacheKey;

    public SamlRegisteredServiceCacheKey(final SamlRegisteredService registeredService,
                                         final CriteriaSet criteriaSet) {
        this.cacheKey = getCacheKeyForRegisteredService(registeredService, criteriaSet);
        LOGGER.trace("Calculated service cache key [{}]", cacheKey);
        this.id = buildRegisteredServiceCacheKey(this.cacheKey);
        this.registeredService = registeredService;
        this.criteriaSet = criteriaSet;
    }
    
    private static String buildRegisteredServiceCacheKey(final String key) {
        val hashedKey = DigestUtils.sha512(key);
        LOGGER.trace("Hashed service cache key [{}] as [{}]", key, hashedKey);
        return hashedKey;
    }

    /**
     * Builds the cache key from the service id, the metadata being looked up and the service settings that decide how
     * that metadata is filtered and cached. A service whose settings change therefore gets a fresh resolver on every
     * node, while the entry built with the old settings is left to expire.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the cache key
     */
    private static String getCacheKeyForRegisteredService(final SamlRegisteredService service,
                                                          final CriteriaSet criteriaSet) {
        val metadataKey = getMetadataKeyForRegisteredService(service, criteriaSet);
        return service.getId() + KEY_SEPARATOR + metadataKey + KEY_SEPARATOR + getMetadataSettingsFingerprint(service);
    }

    /**
     * Gets the part of the key that identifies the metadata. Metadata queries and metadata stores
     * (a bare source such as {@code jdbc://} or {@code mongodb://}) load the document of the requested entity only,
     * so they are keyed by that entity; any other location is loaded whole and keyed by the location.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the metadata key
     */
    private static String getMetadataKeyForRegisteredService(final SamlRegisteredService service,
                                                              final CriteriaSet criteriaSet) {
        val metadataLocation = service.getMetadataLocation();
        if (SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation) || isMetadataSourceId(metadataLocation)) {
            val entityId = criteriaSet.contains(EntityIdCriterion.class)
                ? Objects.requireNonNull(criteriaSet.get(EntityIdCriterion.class)).getEntityId()
                : service.getServiceId();
            LOGGER.trace("Located entity id [{}] for service metadata location [{}]", entityId, metadataLocation);
            return metadataLocation + KEY_SEPARATOR + entityId;
        }
        return metadataLocation;
    }

    private static boolean isMetadataSourceId(final String metadataLocation) {
        return StringUtils.isNotBlank(metadataLocation) && PATTERN_METADATA_SOURCE_ID.matcher(metadataLocation.trim()).matches();
    }

    private static String getMetadataSettingsFingerprint(final SamlRegisteredService service) {
        val entityAttributes = Optional.ofNullable(service.getMetadataCriteriaEntityAttributes())
            .map(attributes -> new TreeMap<>(attributes).toString())
            .orElse(StringUtils.EMPTY);
        return Stream.of(service.getMetadataSignatureLocation(), service.isRequireSignedRoot(),
                service.getMetadataMaxValidity(), service.isValidateMetadataCertificates(),
                service.getMetadataCriteriaDirection(), service.getMetadataCriteriaPattern(), entityAttributes,
                service.getMetadataCriteriaRoles(), service.isMetadataCriteriaRemoveEmptyEntitiesDescriptors(),
                service.isMetadataCriteriaRemoveRolelessEntityDescriptors(), service.getMetadataExpirationDuration(),
                service.getMetadataProxyLocation())
            .map(String::valueOf)
            .collect(Collectors.joining(KEY_SEPARATOR));
    }
}
