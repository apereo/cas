package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.idp.metadata.plan.SamlRegisteredServiceMetadataResolutionPlan;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.component.AbstractIdentifiableInitializableComponent;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;

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
public class SamlRegisteredServiceMetadataResolverCacheLoader implements CacheLoader<@NonNull SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> {

    protected final OpenSamlConfigBean configBean;

    protected final HttpClient httpClient;
    
    private final SamlRegisteredServiceMetadataResolutionPlan metadataResolutionPlan;

    @Override
    public CachedMetadataResolverResult load(final SamlRegisteredServiceCacheKey cacheKey) {
        val metadataResolvers = loadMetadataResolvers(cacheKey);
        if (metadataResolvers.isEmpty()) {
            val registeredService = cacheKey.getRegisteredService();
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(registeredService.getMetadataLocation());
            throw new SamlException("No metadata resolvers could be configured for service " + registeredService.getName()
                + " with metadata location " + metadataLocation);
        }

        val metadataResolver = initializeChainingMetadataResolver(metadataResolvers);
        LOGGER.debug("Metadata resolvers active for this request are [{}]", Objects.requireNonNull(metadataResolvers));
        return CachedMetadataResolverResult
            .builder()
            .cachedInstant(Instant.now(Clock.systemUTC()))
            .metadataResolver(metadataResolver)
            .build();
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


