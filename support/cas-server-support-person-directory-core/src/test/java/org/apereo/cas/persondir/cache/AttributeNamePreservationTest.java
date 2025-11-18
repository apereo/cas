package org.apereo.cas.persondir.cache;

import lombok.val;
import org.aopalliance.intercept.MethodInvocation;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to ensure attribute names are properly preserved in cache key generation.
 *
 * Verifies the fix for the cache key collision vulnerability where attribute
 * names were discarded, causing different queries to produce identical cache keys.
 *
 * These tests verify that:
 * - Queries with swapped attribute values produce different cache keys
 * - Same value in different attributes produce different cache keys
 *
 * @author CAS Security Team
 * @since 7.2.0
 * @see AttributeBasedCacheKeyGenerator#putAttributeInCache
 */
@Tag("AttributeRepository")
class AttributeNamePreservationTest {

    private AttributeBasedCacheKeyGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AttributeBasedCacheKeyGenerator();
        generator.setUseAllAttributes(true);
    }

    /**
     * This test DEMONSTRATES the actual vulnerability.
     *
     * With BUGGY code (line 175: cacheKey.put(hexed, value)):
     *   - Attribute names are ignored
     *   - Only the hash values matter
     *   - Queries with swapped values can collide
     *   - This test will FAIL (keys will be equal)
     *
     * With FIXED code (line 175: cacheKey.put(attr, hexed)):
     *   - Attribute names are preserved
     *   - Different attributes = different keys
     *   - This test will PASS (keys will be different)
     */
    @Test
    void testSwappedAttributeValuesShouldNotCollide() {
        // Two queries with swapped username/email values
        val query1 = Map.of(
            "username", (Object) "alice",
            "email", (Object) "bob@example.com"
        );

        val query2 = Map.of(
            "username", (Object) "bob@example.com",
            "email", (Object) "alice"
        );

        val key1 = generateCacheKey(query1);
        val key2 = generateCacheKey(query2);

        System.out.printf("""
            === Swapped Attribute Values Collision Test ===
            Query 1: {username: 'alice', email: 'bob@example.com'}
            Cache key 1: %s

            Query 2: {username: 'bob@example.com', email: 'alice'}
            Cache key 2: %s

            Keys are equal (COLLISION): %s

            IMPACT: %s
            """,
            key1,
            key2,
            key1.equals(key2),
            key1.equals(key2)
                ? "CRITICAL BUG! User Alice could receive Bob's cached data!"
                : "OK - Attribute names are properly preserved in cache key"
        );

        // This assertion FAILS with buggy code (they're equal)
        // This assertion PASSES with fixed code (they're different)
        assertNotEquals(key1, key2,
            "CRITICAL SECURITY BUG: Swapped attribute values produce the same cache key! " +
            "Query {username: 'alice', email: 'bob@example.com'} and " +
            "query {username: 'bob@example.com', email: 'alice'} should have DIFFERENT cache keys. " +
            "This bug means User Alice could receive User Bob's cached attributes!");
    }

    /**
     * Another demonstration: same value in different attributes
     */
    @Test
    void testSameValueDifferentAttributeShouldNotCollide() {
        val query1 = Map.of("username", (Object) "john");
        val query2 = Map.of("email", (Object) "john");

        val key1 = generateCacheKey(query1);
        val key2 = generateCacheKey(query2);

        System.out.printf("""
            === Same Value, Different Attribute Test ===
            Query 1: {username: 'john'}
            Cache key 1: %s

            Query 2: {email: 'john'}
            Cache key 2: %s

            Keys are equal (COLLISION): %s
            """,
            key1,
            key2,
            key1.equals(key2)
        );

        assertNotEquals(key1, key2,
            "BUG: Queries with the same value in different attributes should have different cache keys! " +
            "{username: 'john'} and {email: 'john'} are completely different queries.");
    }

    private String generateCacheKey(Map<String, Object> seed) {
        val methodInvocation = createMethodInvocation(seed);
        val key = generator.generateKey(methodInvocation);
        return key != null ? key.toString() : null;
    }

    private MethodInvocation createMethodInvocation(Map<String, Object> seed) {
        return new MethodInvocation() {
            @Override
            public Method getMethod() {
                try {
                    return PersonAttributeDao.class.getMethod("getPeopleWithMultivaluedAttributes", Map.class);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object[] getArguments() {
                return new Object[]{seed};
            }

            @Override
            public Object proceed() {
                return null;
            }

            @Override
            public Object getThis() {
                return null;
            }

            @Override
            public java.lang.reflect.AccessibleObject getStaticPart() {
                return getMethod();
            }
        };
    }
}
