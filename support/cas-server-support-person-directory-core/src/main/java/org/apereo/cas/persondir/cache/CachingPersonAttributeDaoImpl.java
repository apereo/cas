package org.apereo.cas.persondir.cache;

import org.apereo.cas.authentication.attribute.AbstractDefaultAttributePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.InitializingBean;
import jakarta.annotation.Nonnull;
import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A configurable caching implementation of {@link PersonAttributeDao}
 * which caches results from a wrapped {@link PersonAttributeDao}.
 *
 * @author dgrimwood@unicon.net
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Slf4j
public class CachingPersonAttributeDaoImpl extends AbstractDefaultAttributePersonAttributeDao implements InitializingBean {
    protected static final Set<PersonAttributes> NULL_RESULTS_OBJECT;

    static {
        NULL_RESULTS_OBJECT = new HashSet<>();
        NULL_RESULTS_OBJECT.add(new SimplePersonAttributes(UUID.randomUUID().toString()));
    }

    @Getter
    @Setter
    private PersonAttributeDao cachedPersonAttributesDao;

    @Getter
    @Setter
    private CacheKeyGenerator cacheKeyGenerator;

    /*
     * The cache to store query results in.
     */
    @Getter
    @Setter
    private Map<Serializable, Set<PersonAttributes>> userInfoCache;

    /*
     * If null results should be cached.
     */
    @Getter
    @Setter
    private boolean cacheNullResults;

    /*
     * The Object that should be stored in the cache if cacheNullResults is true
     */
    @Getter
    @Setter
    private Set<PersonAttributes> nullResultsObject = NULL_RESULTS_OBJECT;

    @Getter
    private long queries;
    @Getter
    private long misses;

    @Override
    public void afterPropertiesSet() {
        if (cacheKeyGenerator == null) {
            cacheKeyGenerator = new AttributeBasedCacheKeyGenerator();
            var usernameAttributeProvider = getUsernameAttributeProvider();
            var usernameAttribute = usernameAttributeProvider.getUsernameAttribute();
            cacheKeyGenerator.setDefaultAttributeName(usernameAttribute);
        }
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> seed,
                                                                    final PersonAttributeDaoFilter filter,
                                                                    final Set<PersonAttributes> resultPeople) {
        var methodInvocation = new PersonAttributeDaoMethodInvocation(seed);
        var cacheKey = cacheKeyGenerator.generateKey(methodInvocation);

        if (cacheKey != null) {
            var cacheResults = userInfoCache.get(cacheKey);
            if (cacheResults != null) {
                if (nullResultsObject.equals(cacheResults)) {
                    cacheResults = null;
                }
                LOGGER.debug("Retrieved query from cache for key=[{}], results=[{}]", cacheKey, cacheResults);
                this.queries++;
                return cacheResults;
            }
        }

        var queryResults = cachedPersonAttributesDao.getPeopleWithMultivaluedAttributes(seed, filter, resultPeople);

        if (cacheKey != null) {
            if (queryResults != null) {
                userInfoCache.put(cacheKey, queryResults);
            } else if (cacheNullResults) {
                userInfoCache.put(cacheKey, nullResultsObject);
            }
            this.queries++;
            this.misses++;
        }

        return queryResults;
    }

    /**
     * Remove user attributes.
     *
     * @param uid the uid
     */
    public void removeUserAttributes(final String uid) {
        val seed = toSeedMap(uid);
        removeUserAttributesMultivaluedSeed(seed);
    }

    /**
     * Remove user attributes multivalued seed.
     *
     * @param seed the seed
     */
    public void removeUserAttributesMultivaluedSeed(final Map<String, List<Object>> seed) {
        val methodInvocation = new PersonAttributeDaoMethodInvocation(seed);
        val cacheKey = cacheKeyGenerator.generateKey(methodInvocation);
        userInfoCache.remove(cacheKey);
    }

    @Override
    public String[] getId() {
        val ids = new ArrayList<String>();
        ids.add(getClass().getSimpleName());
        ids.addAll(List.of(cachedPersonAttributesDao.getId()));
        return ids.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final PersonAttributeDaoFilter filter) {
        return cachedPersonAttributesDao.getPossibleUserAttributeNames(filter);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final PersonAttributeDaoFilter filter) {
        return cachedPersonAttributesDao.getAvailableQueryAttributes(filter);
    }

    private static class PersonAttributeDaoMethodInvocation implements MethodInvocation {
        private static final Method METHOD;

        static {
            try {
                METHOD = PersonAttributeDao.class.getMethod("getPeopleWithMultivaluedAttributes", Map.class);
            } catch (final SecurityException e) {
                val nsme = new NoSuchMethodError("The 'getPeopleWithMultivaluedAttributes(" + Map.class + ")' method on the '"
                    + PersonAttributeDao.class + "' is not accessible due to a security policy.");
                nsme.initCause(e);
                throw nsme;
            } catch (final NoSuchMethodException e) {
                val nsme = new NoSuchMethodError("The 'getPeopleWithMultivaluedAttributes(" + Map.class + ")' method on the '"
                    + PersonAttributeDao.class + "' does not exist.");
                nsme.initCause(e);
                throw nsme;
            }
        }

        @Getter
        private final Object[] arguments;

        PersonAttributeDaoMethodInvocation(final Object... args) {
            arguments = args;
        }
        
        @Nonnull
        @Override
        public AccessibleObject getStaticPart() {
            throw new UnsupportedOperationException("getStaticPart() is not supported.");
        }

        @Override
        public Object getThis() {
            throw new UnsupportedOperationException("getThis() is not supported.");
        }

        @Override
        public Object proceed() {
            throw new UnsupportedOperationException("proceed() is not supported.");
        }

        @Nonnull
        @Override
        public Method getMethod() {
            return METHOD;
        }
    }
}
