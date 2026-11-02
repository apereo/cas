package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.component.AbstractIdentifiableInitializableComponent;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

/**
 * This is {@link SamlRegisteredServiceMetadataResolverCacheLoader} that uses Guava's cache loading strategy
 * to keep track of metadata resources and resolvers. The cache loader here supports loading
 * metadata resources from SAML services, supports dynamic metadata queries and is able
 * to run various validation filters on the metadata before finally caching the resolver.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class SamlRegisteredServiceMetadataResolverCacheLoader implements CacheLoader<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> {

    protected final OpenSamlConfigBean configBean;

    protected final HttpClient httpClient;
    
    private final SamlRegisteredServiceMetadataResolutionPlan metadataResolutionPlan;

    @Override
    public CachedMetadataResolverResult load(final SamlRegisteredServiceCacheKey cacheKey) {
        val metadataResolvers = loadMetadataResolvers(cacheKey);
        if (metadataResolvers.isEmpty()) {
            val registeredService = cacheKey.getRegisteredService();
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(registeredService.getMetadataLocation());
            val supportingResolvers = metadataResolutionPlan.getRegisteredMetadataResolvers()
                .stream()
                .filter(Objects::nonNull)
                .filter(resolver -> resolver.supports(registeredService))
                .map(SamlRegisteredServiceMetadataResolver::getName)
                .toList();
            val reason = supportingResolvers.isEmpty()
                ? "no metadata resolver supports this metadata location"
                : "metadata resolver(s) %s produced no metadata".formatted(supportingResolvers);
            throw new SamlException("No metadata resolvers could be configured for service %s with metadata location %s: %s"
                .formatted(registeredService.getName(), metadataLocation, reason));
        }

        val metadataResolver = initializeChainingMetadataResolver(metadataResolvers);
        LOGGER.debug("Metadata resolvers active for this request are [{}]", Objects.requireNonNull(metadataResolvers));
        val metadataSourceFiles = getMetadataSourceFiles(cacheKey.getRegisteredService());
        return CachedMetadataResolverResult
            .builder()
            .cachedInstant(Instant.now(Clock.systemUTC()))
            .metadataResolver(metadataResolver)
            .metadataSourceFiles(metadataSourceFiles)
            .metadataSourceFilesSnapshot(CachedMetadataResolverResult.snapshotOf(metadataSourceFiles))
            .singleServiceProvider(holdsSingleServiceProvider(metadataResolver))
            .build();
    }

    /**
     * Whether the metadata holds exactly one service provider. This is decided once per load, because it walks every
     * entity of the metadata, and the answer is needed on requests for entities the metadata does not hold.
     * Resolvers that only look entities up by their id, such as those reading a metadata directory, report none.
     *
     * @param metadataResolver the metadata resolver
     * @return true if the metadata holds a single service provider
     */
    protected boolean holdsSingleServiceProvider(final MetadataResolver metadataResolver) {
        return FunctionUtils.doAndHandle(() -> {
            val criteria = new CriteriaSet(new EvaluableEntityRoleEntityDescriptorCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            return Iterables.size(metadataResolver.resolve(criteria)) == 1;
        }, e -> Boolean.FALSE).get();
    }

    /**
     * Gets the local files the metadata of the service is read from, as reported by the resolvers that support it.
     *
     * @param registeredService the registered service
     * @return the files
     */
    protected List<File> getMetadataSourceFiles(final SamlRegisteredService registeredService) {
        return metadataResolutionPlan.getRegisteredMetadataResolvers()
            .stream()
            .filter(Objects::nonNull)
            .filter(resolver -> resolver.supports(registeredService))
            .flatMap(resolver -> resolver.getMetadataSourceFiles(registeredService).stream())
            .distinct()
            .toList();
    }

    /**
     * Describes where the metadata of the service comes from, as reported by the resolvers that support it.
     *
     * @param cacheKey the cache key
     * @return the description
     */
    public Map<String, Object> describe(final SamlRegisteredServiceCacheKey cacheKey) {
        val description = new LinkedHashMap<String, Object>();
        metadataResolutionPlan.getRegisteredMetadataResolvers()
            .stream()
            .filter(Objects::nonNull)
            .filter(resolver -> resolver.supports(cacheKey.getRegisteredService()))
            .forEach(resolver -> description.putAll(resolver.describe(cacheKey.getRegisteredService(), cacheKey.getCriteriaSet())));
        return description;
    }

    protected MetadataResolver initializeChainingMetadataResolver(final List<MetadataResolver> metadataResolvers) {
        return FunctionUtils.doUnchecked(() -> {
            val metadataResolver = new ChainingMetadataResolver();
            metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
            LOGGER.trace("There are [{}] eligible metadata resolver(s) for this request", metadataResolvers.size());
            metadataResolver.setResolvers(metadataResolvers);
            metadataResolver.initialize();
            return metadataResolver;
        });
    }

    protected List<MetadataResolver> loadMetadataResolvers(final SamlRegisteredServiceCacheKey cacheKey) {
        val availableResolvers = metadataResolutionPlan.getRegisteredMetadataResolvers();
        LOGGER.debug("There are [{}] metadata resolver(s) available in the chain", availableResolvers.size());
        val registeredService = cacheKey.getRegisteredService();
        return availableResolvers
            .stream()
            .filter(Objects::nonNull)
            .filter(resolver -> {
                LOGGER.trace("Evaluating whether metadata resolver [{}] can support service [{}]", resolver.getName(), registeredService.getName());
                return resolver.supports(registeredService);
            })
            .map(Unchecked.function(resolver -> {
                LOGGER.trace("Metadata resolver [{}] has started to process metadata for [{}]", resolver.getName(), registeredService.getName());
                return resolver.resolve(registeredService, cacheKey.getCriteriaSet());
            }))
            .flatMap(Collection::stream)
            .filter(Objects::nonNull)
            .peek(Unchecked.consumer(givenResolver -> {
                if (givenResolver instanceof final AbstractIdentifiableInitializableComponent metadataResolver && !metadataResolver.isInitialized()) {
                    FunctionUtils.doIfBlank(metadataResolver.getId(), _ -> metadataResolver.setId(registeredService.getName() + '-' + RandomUtils.generateSecureRandomId()));
                    LOGGER.trace("Metadata resolver [{}] will be forcefully initialized", metadataResolver.getId());
                    metadataResolver.initialize();
                }
            }))
            .collect(Collectors.toList());
    }
}


