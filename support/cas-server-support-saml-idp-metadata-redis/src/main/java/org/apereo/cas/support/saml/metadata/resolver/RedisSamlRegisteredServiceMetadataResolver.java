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
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

/**
 * This is {@link RedisSamlRegisteredServiceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class RedisSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver
    implements SamlRegisteredServiceMetadataManager {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = SamlMetadataDocument.class.getSimpleName() + ':';

    private final CasRedisTemplate<String, SamlMetadataDocument> redisTemplate;

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

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            return results
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
                .map(doc -> buildMetadataResolverFrom(service, doc))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
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
        val redisKey = CAS_PREFIX + document.getName() + ':' + document.getId();
        redisTemplate.boundValueOps(redisKey).set(document);
        return document;
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
                .forEach(redisTemplate::delete);
        }
    }

    @Override
    public void removeByName(final String name) {
        val pattern = CAS_PREFIX + name + ":*";
        try (val results = redisTemplate.scan(pattern)) {
            results.forEach(redisTemplate::delete);
        }
    }

    @Override
    public void removeAll() {
        try (val results = redisTemplate.scan(getPatternRedisKey())) {
            results.forEach(redisTemplate::delete);
        }
    }

    @Override
    public Optional<SamlMetadataDocument> findByName(final String name) {
        val pattern = CAS_PREFIX + name + ":*";
        try (val results = redisTemplate.scan(pattern)) {
            return results
                .map(redisKey -> redisTemplate.boundValueOps(redisKey).get())
                .filter(Objects::nonNull)
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
