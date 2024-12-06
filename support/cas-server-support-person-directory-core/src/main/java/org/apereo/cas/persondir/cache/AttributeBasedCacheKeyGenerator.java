package org.apereo.cas.persondir.cache;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.*;

/**
 * This is {@link AttributeBasedCacheKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@Slf4j
public class AttributeBasedCacheKeyGenerator implements CacheKeyGenerator {

    private static final Map<String, Object> POSSIBLE_USER_ATTRIBUTE_NAMES_SEED_MAP = Map.of("getPossibleUserAttributeNames_seedMap", new Serializable() {
        @Serial
        private static final long serialVersionUID = 1L;
    });

    private static final Map<String, Object> AVAILABLE_QUERY_ATTRIBUTES_SEED_MAP = Map.of("getAvailableQueryAttributes_seedMap", new Serializable() {
        @Serial
        private static final long serialVersionUID = 1L;
    });

    /**
     * Methods on {@link PersonAttributeDao} that are cacheable.
     */
    @RequiredArgsConstructor
    @Getter
    public enum CacheableMethod {
        /**
         * Method to cache.
         */
        PERSON_STR("getPerson", new Class[]{String.class}),
        /**
         * Method to cache.
         */
        PEOPLE_MAP("getPeople", new Class[]{Map.class, PersonAttributeDaoFilter.class}),
        /**
         * Method to cache.
         */
        PEOPLE_MULTIVALUED_MAP("getPeopleWithMultivaluedAttributes", new Class[]{Map.class}),
        /**
         * Method to cache.
         */
        POSSIBLE_USER_ATTRIBUTE_NAMES("getPossibleUserAttributeNames", new Class[]{PersonAttributeDaoFilter.class}),
        /**
         * Method to cache.
         */
        AVAILABLE_QUERY_ATTRIBUTES("getAvailableQueryAttributes", new Class[]{PersonAttributeDaoFilter.class});

        private final String name;

        @SuppressWarnings("ImmutableEnumChecker")
        private final Class<?>[] args;
    }

    /*
     * The set of attributes to use to generate the cache key.
     */
    private Set<String> cacheKeyAttributes;
    private String defaultAttributeName = "username";
    private boolean useAllAttributes;
    private boolean ignoreEmptyAttributes;

    @Override
    public Serializable generateKey(final MethodInvocation methodInvocation) {
        val cacheableMethod = resolveCacheableMethod(methodInvocation);
        val methodArguments = methodInvocation.getArguments();
        val seed = this.getSeed(methodArguments, cacheableMethod);
        val keyHashCode = this.getKeyHash(seed);
        if (keyHashCode == null) {
            LOGGER.debug("No cache key generated for MethodInvocation=[{}]", methodInvocation);
            return null;
        }

        val hashCodeCalculator = new HashCodeCalculator();
        hashCodeCalculator.append(keyHashCode);

        val checkSum = hashCodeCalculator.getCheckSum();
        val hashCode = hashCodeCalculator.getHashCode();
        val hashCodeCacheKey = new HashCodeCacheKey(checkSum, hashCode);

        LOGGER.debug("Generated cache key [{}] for method invocation [{}]", hashCodeCacheKey, methodInvocation);
        return cacheableMethod.getName() + '|' + hashCodeCacheKey;
    }

    /**
     * Get the see {@link Map} that was passed to the {@link CacheableMethod}. For {@link CacheableMethod}s that
     * take {@link String} arguments this method is responsible for converting it into a {@link Map} using the
     * {@code defaultAttributeName}.
     *
     * @param methodArguments The method arguments
     * @param cacheableMethod The targeted cacheable method
     * @return The seed Map for the method call
     */
    protected Map<String, Object> getSeed(final Object[] methodArguments, final CacheableMethod cacheableMethod) {
        return switch (cacheableMethod) {
            case PEOPLE_MAP, PEOPLE_MULTIVALUED_MAP -> (Map<String, Object>) methodArguments[0];
            case PERSON_STR -> {
                val uid = (String) methodArguments[0];
                yield Map.of(this.defaultAttributeName, uid);
            }
            case POSSIBLE_USER_ATTRIBUTE_NAMES -> POSSIBLE_USER_ATTRIBUTE_NAMES_SEED_MAP;
            case AVAILABLE_QUERY_ATTRIBUTES -> AVAILABLE_QUERY_ATTRIBUTES_SEED_MAP;
        };
    }

    /**
     * Gets the hash of the key elements from the seed {@link Map}. The key elements are specified by
     * the {@code cacheKeyAttributes} {@link Set} or if it is {@code null} the
     * {@code defaultAttributeName} is used as the key attribute.
     *
     * @param seed Seed
     * @return Hash of key elements from the seed
     */
    protected Integer getKeyHash(final Map<String, Object> seed) {
        val cacheAttributes = determineCacheAttributes(seed);
        val cacheKey = new HashMap<String, Object>(cacheAttributes.size());
        for (val attr : cacheAttributes) {
            if (seed.containsKey(attr)) {
                val value = seed.get(attr);

                if (!this.ignoreEmptyAttributes) {
                    putAttributeInCache(cacheKey, attr, value);
                } else if (value instanceof final Collection c) {
                    if (!CollectionUtils.isEmpty(c)) {
                        putAttributeInCache(cacheKey, attr, value);
                    }
                } else if (value instanceof final CharSequence cs) {
                    if (StringUtils.isNotEmpty(cs)) {
                        putAttributeInCache(cacheKey, attr, value);
                    }
                } else if (value != null) {
                    putAttributeInCache(cacheKey, attr, value);
                }
            }
        }
        LOGGER.debug("Generated cache Map [{}] from seed Map [{}]", cacheKey, seed);
        if (cacheKey.isEmpty()) {
            return null;
        }
        return cacheKey.hashCode();
    }

    private Set<String> determineCacheAttributes(final Map<String, Object> seed) {
        if (this.useAllAttributes) {
            return seed.keySet();
        }
        return Objects.requireNonNullElseGet(this.cacheKeyAttributes, () -> Set.of(defaultAttributeName));
    }

    protected static void putAttributeInCache(final Map<String, Object> cacheKey, final String attr, final Object value) {
        val hexed = new DigestUtils(SHA_512).digestAsHex(value.toString());
        cacheKey.put(hexed, value);
    }

    /**
     * Iterates over the {@link CacheableMethod} instances to determine which instance the
     * passed {@link MethodInvocation} applies to.
     *
     * @param methodInvocation method invocation
     * @return Cacheable method
     */
    protected CacheableMethod resolveCacheableMethod(final MethodInvocation methodInvocation) {
        val targetMethod = methodInvocation.getMethod();
        val targetClass = targetMethod.getDeclaringClass();

        for (val method : CacheableMethod.values()) {
            Method cacheableMethod = null;
            try {
                cacheableMethod = targetClass.getMethod(method.getName(), method.getArgs());
            } catch (final SecurityException e) {
                LOGGER.warn("Security exception while attempting to if the target class [{}] implements the cacheable method [{}]", targetClass, cacheableMethod, e);
            } catch (final NoSuchMethodException e) {
                LOGGER.warn("Target class [{}] does not implement possible cacheable method [{}].", targetClass, cacheableMethod);
            }
            if (targetMethod.equals(cacheableMethod)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Do not know how to generate a cache entry for " + targetMethod + " on class " + targetClass);
    }
}
