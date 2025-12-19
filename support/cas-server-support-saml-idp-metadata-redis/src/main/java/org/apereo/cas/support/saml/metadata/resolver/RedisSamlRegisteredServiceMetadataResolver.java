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
public class RedisSamlRegisteredServiceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    /**
     * Redis key prefix.
     */
    public static final String CAS_PREFIX = SamlMetadataDocument.class.getSimpleName() + ':';

    private final CasRedisTemplate<String, SamlMetadataDocument> redisTemplate;

    private final long scanCount;

    public RedisSamlRegisteredServiceMetadataResolver(
        final SamlIdPProperties samlIdPProperties,
        final OpenSamlConfigBean configBean,
        final CasRedisTemplate<String, SamlMetadataDocument> redisTemplate,
        final long scanCount) {
        super(samlIdPProperties, configBean);
        this.redisTemplate = redisTemplate;
        this.scanCount = scanCount;
    }

    private static String getPatternRedisKey() {
        return CAS_PREFIX + '*';
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        try (val results = redisTemplate.scan(getPatternRedisKey(), this.scanCount)) {
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
        return metadataLocation.trim().startsWith("redis://");
    }

    @Override
    public void saveOrUpdate(final SamlMetadataDocument document) {
        val redisKey = CAS_PREFIX + document.getName() + ':' + document.getId();
        redisTemplate.boundValueOps(redisKey).set(document);
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        return supports(service);
    }
}
