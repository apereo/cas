package org.apereo.cas.support.saml.idp.metadata;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataLocator;
import org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPSamlRegisteredServiceCriterion;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.function.FunctionUtils;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import net.shibboleth.shared.resolver.ResolverException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.resilience.annotation.Retryable;

/**
 * This is {@link SamlIdPMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SamlIdPMetadataResolver extends BaseElementMetadataResolver {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final SamlIdPMetadataLocator locator;

    private final SamlIdPMetadataGenerator generator;

    private final OpenSamlConfigBean openSamlConfigBean;

    private final CasConfigurationProperties casProperties;

    private final Cache<String, List<EntityDescriptor>> metadataCache;

    public SamlIdPMetadataResolver(final SamlIdPMetadataLocator locator,
                                   final SamlIdPMetadataGenerator generator,
                                   final OpenSamlConfigBean openSamlConfigBean,
                                   final CasConfigurationProperties casProperties) {
        this.locator = locator;
        this.generator = generator;
        this.openSamlConfigBean = openSamlConfigBean;
        this.casProperties = casProperties;

        setResolveViaPredicatesOnly(true);

        val idp = casProperties.getAuthn().getSamlIdp();
        this.metadataCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(Beans.newDuration(idp.getMetadata().getCore().getCacheExpiration()))
            .build();
    }

    /**
     * Determine the criteria to resolve idp metadata.
     * If the criteria-set contains a service definition to act as an override,
     * based on the presence of {@link SamlIdPSamlRegisteredServiceCriterion},
     * that service is positioned first in the list. An empty criteria
     * is always added to calculate and resolve metadata globally as the last step,
     * in case an override is not available.
     *
     * @param criteria the criteria set
     * @return list of optional service definitions
     */
    private static List<Optional<SamlRegisteredService>> determineFilteringCriteria(final CriteriaSet criteria) {
        val results = new ArrayList<Optional<SamlRegisteredService>>();
        if (criteria.contains(SamlIdPSamlRegisteredServiceCriterion.class)) {
            val criterion = criteria.get(SamlIdPSamlRegisteredServiceCriterion.class);
            results.add(Optional.of(Objects.requireNonNull(criterion).registeredService()));
        }
        results.add(Optional.empty());
        return results;
    }

    @NonNull
    @Override
    @Retryable(value = ResolverException.class, maxRetries = 3, delay = 1000)
    public Iterable<EntityDescriptor> resolve(final CriteriaSet criteria) {
        val filteringCriteria = determineFilteringCriteria(criteria);
        for (val filter : filteringCriteria) {
            val cacheKey = getMetadataCacheKey(filter, criteria);
            LOGGER.debug("Cache key for SAML IdP metadata is [{}]", cacheKey);
            val cachedEntities = metadataCache.getIfPresent(cacheKey);
            if (cachedEntities != null) {
                return cachedEntities;
            }
            val entities = resolveAndCacheMetadata(criteria, filter, cacheKey);
            if (entities != null) {
                return entities;
            }
        }
        return List.of();
    }

    /**
     * Resolves the metadata document for the given service, if any, and caches the outcome.
     * <p>
     * Resolution replaces the backing store of this resolver, which is a singleton shared by
     * every request, and then immediately reads that same backing store back. The two steps
     * must therefore be atomic with respect to each other: without the lock, a request that
     * resolves metadata for one service can read the document another request has just
     * installed, which for per-service identity provider metadata means answering with the
     * wrong entity descriptor and, through
     * {@link org.apereo.cas.support.saml.idp.metadata.locator.SamlIdPMetadataCredentialResolver},
     * the wrong signing and encryption credentials. The cache is consulted a second time
     * inside the lock so that threads queued behind the winner do not repeat the work.
     *
     * @param criteria          the criteria set
     * @param registeredService the registered service acting as an override, if any
     * @param cacheKey          the cache key for this resolution
     * @return the resolved entity descriptors, or null when this document yields none
     */
    private @Nullable List<EntityDescriptor> resolveAndCacheMetadata(final CriteriaSet criteria,
                                                                     final Optional<SamlRegisteredService> registeredService,
                                                                     final String cacheKey) {
        return lock.execute(() -> {
            val cachedEntities = metadataCache.getIfPresent(cacheKey);
            if (cachedEntities != null) {
                return cachedEntities;
            }
            val entities = FunctionUtils.doUnchecked(() -> resolveMetadata(criteria, registeredService));
            if (entities != null && !entities.isEmpty()) {
                metadataCache.put(cacheKey, entities);
                return entities;
            }
            return null;
        });
    }

    private String getMetadataCacheKey(final Optional<SamlRegisteredService> serviceResult,
                                       final CriteriaSet criteriaSet) {
        return serviceResult.map(registeredService -> registeredService.getName() + registeredService.getId())
            .or(() -> criteriaSet.contains(EntityIdCriterion.class)
                ? Optional.of(criteriaSet.get(EntityIdCriterion.class).getEntityId())
                : Optional.empty())
            .orElseGet(() -> casProperties.getAuthn().getSamlIdp().getCore().getEntityId());
    }

    /**
     * Generates the metadata document when absent, installs it as this resolver's backing
     * store and resolves the given criteria against it. The result is copied into an
     * immutable list before it leaves this method, so that a cached value can never be a
     * lazy view over a backing store that a later resolution replaces.
     * <p>
     * Callers must hold {@link #lock}; see {@link #resolveAndCacheMetadata}.
     *
     * @param criteria          the criteria set
     * @param registeredService the registered service acting as an override, if any
     * @return the resolved entity descriptors, or null when no metadata is available
     * @throws Throwable the throwable
     */
    private @Nullable List<EntityDescriptor> resolveMetadata(final CriteriaSet criteria,
                                                             final Optional<SamlRegisteredService> registeredService) throws Throwable {
        if (!locator.exists(registeredService) && locator.shouldGenerateMetadataFor(registeredService)) {
            generator.generate(registeredService);
        }
        val resource = locator.resolveMetadata(registeredService);
        LOGGER.trace("Resolved metadata resource is [{}]", resource);
        if (resource.contentLength() > 0) {
            val element = SamlUtils.getRootElementFrom(resource.getInputStream(), openSamlConfigBean);

            LOGGER.trace("Located metadata root element [{}]", element.getNodeName());
            setMetadataRootElement(element);
            LOGGER.trace("Resolving metadata for criteria [{}]", criteria);
            val entityDescriptors = new ArrayList<EntityDescriptor>();
            super.resolve(criteria).forEach(entityDescriptors::add);
            return List.copyOf(entityDescriptors);
        }
        return null;
    }
}
