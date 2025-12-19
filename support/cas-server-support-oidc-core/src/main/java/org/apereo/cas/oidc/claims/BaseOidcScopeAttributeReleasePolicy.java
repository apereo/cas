package org.apereo.cas.oidc.claims;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractRegisteredServiceAttributeReleasePolicy;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;

/**
 * This is {@link BaseOidcScopeAttributeReleasePolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@Setter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public abstract class BaseOidcScopeAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy
    implements OidcRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = -7302163334687300920L;

    @JsonProperty
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> allowedAttributes = new ArrayList<>();

    @JsonProperty("claimMappings")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, String> claimMappings = new TreeMap<>();

    @JsonIgnore
    private String scopeType;

    protected BaseOidcScopeAttributeReleasePolicy(final String scopeType) {
        this.scopeType = scopeType;
    }

    protected Optional<String> getMappedClaim(final String claim,
                                              final RegisteredServiceAttributeReleasePolicyContext context) {
        val mapper = context.getApplicationContext().getBean(OidcAttributeToScopeClaimMapper.DEFAULT_BEAN_NAME,
            OidcAttributeToScopeClaimMapper.class);
        LOGGER.debug("Attempting to process claim [{}]", claim);
        return mapper.containsMappedAttribute(claim, context.getRegisteredService())
            ? Optional.of(mapper.getMappedAttribute(claim, context.getRegisteredService()))
            : Optional.empty();
    }

    protected Pair<String, Object> mapClaimToAttribute(final String claim,
                                                       final RegisteredServiceAttributeReleasePolicyContext context,
                                                       final Map<String, List<Object>> resolvedAttributes) throws Throwable {
        val mappedClaimResult = getMappedClaim(claim, context);
        if (mappedClaimResult.isPresent()) {
            val mappedAttr = mappedClaimResult.get();
            LOGGER.trace("Attribute [{}] is mapped to claim [{}]", mappedAttr, claim);

            val scriptFactoryInstance = ExecutableCompiledScriptFactory.findExecutableCompiledScriptFactory();

            if (scriptFactoryInstance.isPresent()
                && CasRuntimeHintsRegistrar.notInNativeImage()
                && scriptFactoryInstance.get().isScript(mappedAttr)) {
                
                LOGGER.trace("Locating attribute value via script [{}] for definition [{}]", mappedAttr, claim);
                try (val cacheManager = ApplicationContextProvider.getScriptResourceCacheManager()
                    .orElseThrow(() -> new IllegalArgumentException("No groovy script cache manager is available to execute claim mappings"))) {
                    val scriptResource = cacheManager.resolveScriptableResource(mappedAttr, mappedAttr);
                    val args = CollectionUtils.<String, Object>wrap("attributes", resolvedAttributes, "context", context, "claim", claim, "logger", LOGGER);
                    scriptResource.setBinding(args);
                    val result = scriptResource.execute(args.values().toArray(), Object.class);
                    LOGGER.debug("Mapped attribute [{}] to [{}] from script", claim, result);
                    return Pair.of(claim, result);
                }
            }

            if (resolvedAttributes.containsKey(mappedAttr)) {
                val value = resolvedAttributes.get(mappedAttr);
                LOGGER.debug("Found mapped attribute [{}] with value [{}] for claim [{}]", mappedAttr, value, claim);
                return Pair.of(claim, value);
            }
            if (resolvedAttributes.containsKey(claim)) {
                val value = resolvedAttributes.get(claim);
                LOGGER.debug("CAS is unable to find the attribute [{}] that is mapped to claim [{}]. "
                        + "However, since resolved attributes [{}] already contain this claim, "
                        + "CAS will use [{}] with value(s) [{}]",
                    mappedAttr, claim, resolvedAttributes, claim, value);
                return Pair.of(claim, value);
            }
            LOGGER.warn("Located claim [{}] mapped to attribute [{}], yet "
                    + "resolved attributes [{}] do not contain attribute [{}]",
                claim, mappedAttr, resolvedAttributes, mappedAttr);
        }

        val value = resolvedAttributes.get(claim);
        LOGGER.debug("No mapped attribute is defined for claim [{}]; Used [{}] to locate value [{}]", claim, claim, value);
        return Pair.of(claim, value);
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) {
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attributes);

        val attributesToRelease = new HashMap<String, List<Object>>(attributes.size());
        LOGGER.debug("Attempting to map and filter claims based on resolved attributes [{}]", resolvedAttributes);

        val allowedClaims = new LinkedHashSet<>(getAllowedAttributes());
        if (claimsMustBeDefinedViaDiscovery()) {
            val properties = context.getApplicationContext().getBean(CasConfigurationProperties.class);
            val supportedClaims = properties.getAuthn().getOidc().getDiscovery().getClaims();
            allowedClaims.retainAll(supportedClaims);
            LOGGER.debug("[{}] is designed to allow claims [{}] for scope [{}]. After cross-checking with "
                    + "supported claims [{}], the final collection of allowed attributes is [{}]",
                getClass().getSimpleName(), getAllowedAttributes(), getScopeType(), supportedClaims, allowedClaims);
        } else {
            LOGGER.debug("[{}] is designed to allow claims [{}].", getClass().getSimpleName(), allowedClaims);
        }

        allowedClaims
            .stream()
            .map(Unchecked.function(claim -> mapClaimToAttribute(claim, context, resolvedAttributes)))
            .filter(p -> Objects.nonNull(p.getValue()))
            .forEach(p -> attributesToRelease.put(p.getKey(), CollectionUtils.toCollection(p.getValue(), ArrayList.class)));
        return attributesToRelease;
    }

    @Override
    public List<String> determineRequestedAttributeDefinitions(final RegisteredServiceAttributeReleasePolicyContext context) {
        val attributes = getAllowedAttributes();
        return attributes != null ? attributes : new ArrayList<>();
    }

    @JsonIgnore
    protected boolean claimsMustBeDefinedViaDiscovery() {
        return true;
    }
}
