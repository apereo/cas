package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.ComplexPersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.persondir.cache.AttributeBasedCacheKeyGenerator;
import org.apereo.cas.persondir.cache.CachingPersonAttributeDaoImpl;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CachingPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Attributes")
class CachingPersonAttributeDaoTests {
    private static final String DEFAULT_ATTR = "uid";

    private ComplexPersonAttributeDao stubDao;

    @BeforeEach
    protected void setUp() {
        stubDao = new ComplexPersonAttributeDao();
        val daoBackingMap = new HashMap<String, Map<String, List<Object>>>();

        val user1 = new HashMap<String, List<Object>>();
        user1.put("phone", List.of("777-7777"));
        user1.put("displayName", List.of("Display Name"));
        daoBackingMap.put("edalquist", user1);

        val user2 = new HashMap<String, List<Object>>();
        user2.put("phone", List.of("888-8888"));
        user2.put("displayName", List.of(StringUtils.EMPTY));
        daoBackingMap.put("awp9", user2);

        val user3 = new HashMap<String, List<Object>>();
        user3.put("phone", List.of("666-6666"));
        user3.put("givenName", List.of("Howard"));
        daoBackingMap.put("erider", user3);
        stubDao.setBackingMap(daoBackingMap);
        stubDao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(DEFAULT_ATTR));
    }

    private static void validateUser1(final Map<String, List<Object>> attrs) {
        assertNotNull(attrs);
        assertEquals(List.of("777-7777"), attrs.get("phone"));
        assertEquals(List.of("Display Name"), attrs.get("displayName"));
    }

    private static void validateUser2(final Map<String, List<Object>> attrs) {
        assertNotNull(attrs);
        assertEquals(List.of("888-8888"), attrs.get("phone"));
        assertEquals(List.of(StringUtils.EMPTY), attrs.get("displayName"));
    }

    @Test
    void testCacheStats() {
        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(DEFAULT_ATTR));
        dao.setUserInfoCache(new HashMap<>());
        dao.afterPropertiesSet();

        assertEquals(0, dao.getQueries());
        assertEquals(0, dao.getMisses());

        var result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(1, dao.getQueries());
        assertEquals(1, dao.getMisses());

        result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(2, dao.getQueries());
        assertEquals(1, dao.getMisses());

        result = dao.getPerson("nobody");
        assertNull(result);
        assertEquals(3, dao.getQueries());
        assertEquals(2, dao.getMisses());

        result = dao.getPerson("awp9");
        validateUser2(result.getAttributes());
        assertEquals(4, dao.getQueries());
        assertEquals(3, dao.getMisses());

        result = dao.getPerson("nobody");
        assertNull(result);
        assertEquals(5, dao.getQueries());
        assertEquals(4, dao.getMisses());

        result = dao.getPerson("awp9");
        validateUser2(result.getAttributes());
        assertEquals(6, dao.getQueries());
        assertEquals(4, dao.getMisses());

        result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(7, dao.getQueries());
        assertEquals(4, dao.getMisses());
    }

    @Test
    void testCaching() {
        val cacheMap = new HashMap<Serializable, Set<PersonAttributes>>();

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(DEFAULT_ATTR));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals(0, cacheMap.size());

        var result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(1, cacheMap.size());

        result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(1, cacheMap.size());

        result = dao.getPerson("nobody");
        assertNull(result);
        assertEquals(1, cacheMap.size());

        result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(1, cacheMap.size());

        dao.setCacheNullResults(true);
        result = dao.getPerson("nobody");
        assertNull(result);
        assertEquals(2, cacheMap.size());

        result = dao.getPerson("edalquist");
        validateUser1(result.getAttributes());
        assertEquals(2, cacheMap.size());

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(DEFAULT_ATTR, List.of("edalquist"));
        queryMap.put("name.first", List.of("Eric"));
        queryMap.put("name.last", List.of("Dalquist"));

        var resultSet = dao.getPeopleWithMultivaluedAttributes(queryMap);
        assertEquals(1, resultSet.size());
        validateUser1(resultSet.iterator().next().getAttributes());
        assertEquals(2, cacheMap.size());

        dao.removeUserAttributesMultivaluedSeed(queryMap);
        assertEquals(1, cacheMap.size());

        dao.removeUserAttributes("nobody");
        assertEquals(0, cacheMap.size());
    }


    @Test
    void testMultipleAttributeKeys() {
        val cacheMap = new HashMap<Serializable, Set<PersonAttributes>>();

        val keyAttrs = new HashSet<String>();
        keyAttrs.add("name.first");
        keyAttrs.add("name.last");

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(stubDao);
        var cacheKeyGenerator = new AttributeBasedCacheKeyGenerator();
        cacheKeyGenerator.setCacheKeyAttributes(keyAttrs);
        dao.setCacheKeyGenerator(cacheKeyGenerator);
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals(0, cacheMap.size());

        var result = dao.getPerson("edalquist");
        assertNull(result);
        assertEquals(0, cacheMap.size());

        result = dao.getPerson("nobody");
        assertNull(result);
        assertEquals(0, cacheMap.size());

        result = dao.getPerson("edalquist");
        assertNull(result);
        assertEquals(0, cacheMap.size());

        val queryMap1 = new HashMap<String, List<Object>>();
        queryMap1.put(DEFAULT_ATTR, List.of("edalquist"));
        queryMap1.put("name.first", List.of("Eric"));
        queryMap1.put("name.last", List.of("Dalquist"));

        var resultSet = dao.getPeopleWithMultivaluedAttributes(queryMap1);
        assertEquals(1, resultSet.size());
        validateUser1(resultSet.iterator().next().getAttributes());
        assertEquals(1, cacheMap.size());

        val queryMap2 = new HashMap<String, Object>();
        queryMap2.put("name.first", List.of("John"));
        queryMap2.put("name.last", List.of("Doe"));

        resultSet = dao.getPeople(queryMap2);
        assertNull(resultSet);
        assertEquals(1, cacheMap.size());


        resultSet = dao.getPeopleWithMultivaluedAttributes(queryMap1);
        assertEquals(1, resultSet.size());
        validateUser1(resultSet.iterator().next().getAttributes());
        assertEquals(1, cacheMap.size());
    }

    @Test
    void testEmptyCacheKeyWithDefaultAttr() {
        val cacheMap = new HashMap<Serializable, Set<PersonAttributes>>();

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("UNUSED_ATTR_NAME"));
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();

        assertEquals(0, cacheMap.size());

        var resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("edalquist")));
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("edalquist")));
        assertEquals(1, resultsSet.size());
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("nobody")));
        assertNull(resultsSet);
        assertEquals(0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("edalquist")));
        assertEquals(1, resultsSet.size());
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(DEFAULT_ATTR, List.of("edalquist"));
        queryMap.put("name.first", List.of("Eric"));
        queryMap.put("name.last", List.of("Dalquist"));

        resultsSet = dao.getPeopleWithMultivaluedAttributes(queryMap);
        assertEquals(1, resultsSet.size());
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());
    }

    @Test
    void testEmptyCacheKeyWithKeyAttrs() {
        val cacheMap = new HashMap<Serializable, Set<PersonAttributes>>();

        var dao = new CachingPersonAttributeDaoImpl();
        dao.setCachedPersonAttributesDao(stubDao);
        dao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider("UNUSED_ATTR_NAME"));
        var cacheKeyGenerator = new AttributeBasedCacheKeyGenerator();
        cacheKeyGenerator.setCacheKeyAttributes(Set.of("UNUSED_ATTR_NAME"));
        dao.setCacheKeyGenerator(cacheKeyGenerator);
        dao.setUserInfoCache(cacheMap);
        dao.afterPropertiesSet();


        assertEquals(0, cacheMap.size());

        var resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("edalquist")));
        assertEquals(1, resultsSet.size());
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("edalquist")));
        assertEquals(1, resultsSet.size());
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("nobody")));
        assertNull(resultsSet);
        assertEquals(0, cacheMap.size());

        resultsSet = dao.getPeopleWithMultivaluedAttributes(Map.of(DEFAULT_ATTR, List.of("edalquist")));
        assertEquals(1, resultsSet.size());
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

        val queryMap = new HashMap<String, List<Object>>();
        queryMap.put(DEFAULT_ATTR, List.of("edalquist"));
        queryMap.put("name.first", List.of("Eric"));
        queryMap.put("name.last", List.of("Dalquist"));

        resultsSet = dao.getPeopleWithMultivaluedAttributes(queryMap);
        validateUser1(resultsSet.iterator().next().getAttributes());
        assertEquals(0, cacheMap.size());

    }
}
