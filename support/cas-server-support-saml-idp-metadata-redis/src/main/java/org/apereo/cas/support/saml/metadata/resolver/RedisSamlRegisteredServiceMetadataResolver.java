package org.apereo.cas.support.saml.metadata.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlMetadataDocument;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.BaseSamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataManager;
import org.apereo.cas.util.DigestUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

/**
 * This is {@link RedisSamlRegisteredServiceMetadataResolver}.
 * <p>
 * Each metadata document is kept under a key derived from its entity id, name and identifier. Next to the documents,
 * every entity id has an index, a hash whose fields are the keys of its documents, so that the documents of the
 * requested entity are found without scanning the keyspace. Documents stored before the index existed are indexed
 * once, the first time an entity is looked up, after which a marker records that the index is complete.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
public class RedisSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = SamlMetadataDocument.class.getSimpleName() + ':';

    /**
     * Redis key prefix of the per-entity index of metadata documents.
     */
    public static final String CAS_INDEX_PREFIX = SamlMetadataDocument.class.getSimpleName() + "Index:";

    /**
     * Redis key recording that every metadata document has been indexed.
     */
    public static final String CAS_INDEX_MARKER = CAS_INDEX_PREFIX + "complete";

    private final CasRedisTemplate<String, SamlMetadataDocument> redisTemplate;

    private final AtomicBoolean indexed = new AtomicBoolean();

    public RedisSamlRegisteredServiceMetadataResolver(
        final SamlIdPProperties samlIdPProperties,
        final OpenSamlConfigBean configBean,
        final CasRedisTemplate<String, SamlMetadataDocument> redisTemplate) {
        super(samlIdPProperties, configBean);
        this.redisTemplate = redisTemplate;
    }

    private static String getPatternRedisKey() {
        return CAS_PREFIX + '*';
    }

    private static String getIndexKey(final String entityId) {
        return CAS_INDEX_PREFIX + DigestUtils.sha512(entityId);
    }

    private static String getIndexKeyForRedisKey(final String redisKey) {
        return CAS_INDEX_PREFIX + StringUtils.substringBefore(StringUtils.removeStart(redisKey, CAS_PREFIX), ":");
    }

    private static String getRedisKey(final SamlMetadataDocument document) {
        val entityId = StringUtils.defaultString(document.getEntityId());
        return CAS_PREFIX + DigestUtils.sha512(entityId) + ':' + document.getName() + ':' + document.getId();
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val entityId = getEntityIdFromCriteriaSet(criteriaSet);
        if (entityId.isPresent()) {
            return findDocumentsForEntity(entityId.get())
                .stream()
                .map(doc -> buildMetadataResolverFrom(service, doc))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            return results
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .map(doc -> buildMetadataResolverFrom(service, doc))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
    }

    /**
     * Finds the documents of an entity through its index. Index entries whose document no longer exists are dropped.
     *
     * @param entityId the entity id
     * @return the documents
     */
    protected List<SamlMetadataDocument> findDocumentsForEntity(final String entityId) {
        indexExistingDocuments();
        val indexKey = getIndexKey(entityId);
        val redisKeys = List.copyOf(redisTemplate.<String, String>opsForHash().keys(indexKey));
        if (redisKeys.isEmpty()) {
            return List.of();
        }
        val documents = Objects.requireNonNull(redisTemplate.opsForValue().multiGet(redisKeys));
        val found = new ArrayList<SamlMetadataDocument>();
        for (var i = 0; i < redisKeys.size(); i++) {
            val document = documents.get(i);
            if (document == null) {
                redisTemplate.opsForHash().delete(indexKey, redisKeys.get(i));
            } else {
                found.add(document);
            }
        }
        return found;
    }

    /**
     * Indexes the documents stored before documents were indexed. This happens once: a marker records that every
     * document is indexed, and documents stored since are indexed as they are stored. Indexing is idempotent, so
     * nodes that do it at the same time do no harm.
     */
    protected void indexExistingDocuments() {
        if (indexed.get()) {
            return;
        }
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(CAS_INDEX_MARKER))) {
            LOGGER.info("Indexing SAML2 service provider metadata documents in Redis by entity id");
            try (val results = redisTemplate.scan(getPatternRedisKey())) {
                results.forEach(this::addToIndex);
            }
            redisTemplate.<String, String>opsForHash().put(CAS_INDEX_MARKER, "indexedAt", Instant.now(Clock.systemUTC()).toString());
        }
        indexed.set(true);
    }

    private void addToIndex(final String redisKey) {
        redisTemplate.<String, String>opsForHash().put(getIndexKeyForRedisKey(redisKey), redisKey, redisKey);
    }

    private void delete(final String redisKey) {
        redisTemplate.delete(redisKey);
        redisTemplate.opsForHash().delete(getIndexKeyForRedisKey(redisKey), redisKey);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        val metadataLocation = service != null ? service.getMetadataLocation() : StringUtils.EMPTY;
        return metadataLocation.trim().startsWith(getSourceId());
    }

    @Override
    public String getSourceId() {
        return "redis://";
    }
    
    @Override
    public SamlMetadataDocument store(final SamlMetadataDocument document) {
        val metadataDocument = prepareMetadataDocument(document);
        val redisKey = getRedisKey(metadataDocument);
        redisTemplate.boundValueOps(redisKey).set(metadataDocument);
        addToIndex(redisKey);
        return metadataDocument;
    }

    @Override
    public List<SamlMetadataDocument> load() {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            return results
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }

    @Override
    public void removeById(final long id) {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            results
                .filter(redisKey -> redisKey.endsWith(":" + id))
                .forEach(this::delete);
        }
    }

    @Override
    public void removeByName(final String name) {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            results
                .filter(redisKey -> {
                    val document = redisTemplate.boundValueOps(redisKey).get();
                    return document != null && name.equalsIgnoreCase(document.getName());
                })
                .forEach(this::delete);
        }
    }

    @Override
    public void removeAll() {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            results.forEach(this::delete);
        }
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            return results
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .filter(document -> name.equalsIgnoreCase(document.getName()))
                .findFirst();
        }
    }

    @Override
    public Optional<SamlMetadataDocument> findById(final long id) {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            return results
                .filter(redisKey -> redisKey.endsWith(":" + id))
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .findFirst();
        }
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
