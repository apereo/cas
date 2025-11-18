package org.apereo.cas.persondir.cache;

import lombok.val;
import org.aopalliance.intercept.MethodInvocation;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test to demonstrate and validate the fix for cache key collision vulnerability
 * in AttributeBasedCacheKeyGenerator.
 * 
 * This test suite proves that users gb12359 and hj03694 (and many others) can collide
 * in the cache when using the buggy implementation, causing severe data leakage.
 *
 * @author steve-ross
 * @since 7.2.0
 */
@Tag("AttributeRepository")
class AttributeBasedCacheKeyGeneratorCollisionTest {

    private AttributeBasedCacheKeyGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AttributeBasedCacheKeyGenerator();
        generator.setDefaultAttributeName("username");
    }

    /**
     * Test the specific collision case reported: gb12359 and hj03694
     * These two usernames are known to produce cache key collisions in the buggy implementation.
     */
    @Test
    void testReportedCollision_GB12359_vs_HJ03694() {
        val key1 = generateCacheKey("gb12359");
        val key2 = generateCacheKey("hj03694");

        assertNotEquals(key1, key2, 
            "CRITICAL SECURITY ISSUE: Users gb12359 and hj03694 generate the same cache key! " +
            "This causes User A to receive User B's data.");
        
        System.out.printf("""
            === Reported Collision Test ===
            User 'gb12359' cache key: %s
            User 'hj03694' cache key: %s
            Keys are unique: %s
            """, key1, key2, !key1.equals(key2));
    }

    /**
     * Test multiple known collision pairs to demonstrate the breadth of the problem
     */
    @ParameterizedTest(name = "{index}: {0} vs {1}")
    @CsvSource({
        "gb12359, hj03694",
        "user123, user456",
        "alice, bob",
        "admin, guest",
        "test1, test2",
        "employee1000, employee2000"
    })
    void testUserPairsShouldNotCollide(String user1, String user2) {
        val key1 = generateCacheKey(user1);
        val key2 = generateCacheKey(user2);

        assertNotEquals(key1, key2,
            String.format("SECURITY: Users '%s' and '%s' MUST NOT produce the same cache key", user1, user2));
    }

    /**
     * Test that the same user always produces the same key (cache consistency)
     */
    @ParameterizedTest
    @ValueSource(strings = {"gb12359", "hj03694", "testuser", "admin", "user@example.com"})
    void testSameUserProducesSameKey(String username) {
        val key1 = generateCacheKey(username);
        val key2 = generateCacheKey(username);
        val key3 = generateCacheKey(username);

        assertEquals(key1, key2, "Same user should produce identical cache keys");
        assertEquals(key2, key3, "Same user should produce identical cache keys");
        
        System.out.printf("User '%s' consistently produces key: %s%n", username, key1);
    }

    /**
     * Large-scale collision probability test with realistic username patterns
     */
    @Test
    void testCollisionProbabilityWithRealisticUsernames() {
        val sampleSize = 10000;
        val usernames = new HashSet<String>();
        val cacheKeys = new HashMap<String, List<String>>();

        // Generate realistic username patterns
        for (int i = 0; i < sampleSize; i++) {
            val patterns = List.of(
                String.format("user%d", i),
                String.format("employee%05d", i),
                String.format("student%d", i),
                String.format("%c%c%05d", 'a' + (i % 26), 'b' + (i / 26 % 26), i),
                String.format("gb%d", i),
                String.format("hj%05d", i),
                String.format("test_%d", i),
                String.format("%d_user", i)
            );

            for (val username : patterns) {
                if (usernames.add(username)) {
                    val key = generateCacheKey(username);
                    cacheKeys.computeIfAbsent(key, k -> new ArrayList<>()).add(username);
                }
            }
        }

        // Find all collisions
        val collisionReport = new StringBuilder();
        val collisionGroups = new ArrayList<List<String>>();

        cacheKeys.forEach((key, users) -> {
            if (users.size() > 1) {
                collisionGroups.add(users);
                collisionReport.append(String.format("  Key '%s' maps to %d users: %s%n", 
                    key, users.size(), users.subList(0, Math.min(5, users.size()))));
            }
        });

        val totalUsers = usernames.size();
        val totalKeys = cacheKeys.size();
        val collisionRate = totalUsers > 0 ? (1.0 - ((double) totalKeys / totalUsers)) * 100 : 0;

        System.out.printf("""
            
            === Large-Scale Collision Probability Analysis ===
            Total unique users tested: %,d
            Total unique cache keys: %,d
            Number of collision groups: %,d
            Overall collision rate: %.4f%%
            
            Sample collision groups (showing first 10):
            %s
            """,
            totalUsers,
            totalKeys,
            collisionGroups.size(),
            collisionRate,
            collisionReport.toString().lines().limit(10).reduce("", (a, b) -> a + b + "\n")
        );

        // With the fix applied, we expect near-zero collision rate (< 0.1%)
        // Some collisions may still occur due to HashMap.hashCode() birthday paradox,
        // but they should be extremely rare and acceptable.
        val acceptableCollisionRatePercent = 0.1;
        assertTrue(collisionRate < acceptableCollisionRatePercent,
            String.format("EXCESSIVE COLLISION RATE! %,d users map to only %,d unique keys (%.4f%% collision rate). " +
                "Found %d collision groups. Expected collision rate < %.2f%%. " +
                "This suggests the attribute name bug has not been properly fixed!",
                totalUsers, totalKeys, collisionRate, collisionGroups.size(), acceptableCollisionRatePercent));
    }

    /**
     * Test that demonstrates the actual data leakage scenario
     */
    @Test
    void testDataLeakageScenario() {
        val user1 = "gb12359";
        val user2 = "hj03694";

        // Simulate user data
        val user1Data = Map.of(
            "username", user1,
            "email", "gb12359@example.com",
            "department", "Engineering",
            "role", "admin",
            "ssn", "123-45-6789"
        );

        val user2Data = Map.of(
            "username", user2,
            "email", "hj03694@example.com",
            "department", "Marketing",
            "role", "user",
            "ssn", "987-65-4321"
        );

        // Generate cache keys
        val key1 = generateCacheKey(user1);
        val key2 = generateCacheKey(user2);

        // Simulate cache storage
        val cache = new HashMap<String, Map<String, String>>();
        cache.put(key1, user1Data);

        System.out.printf("""
            
            === Data Leakage Scenario Test ===
            User 1: %s (Admin with SSN %s)
            User 1 Cache Key: %s
            
            User 2: %s (Regular user with SSN %s)
            User 2 Cache Key: %s
            
            """, user1, user1Data.get("ssn"), key1,
                 user2, user2Data.get("ssn"), key2);

        if (key1.equals(key2)) {
            // This is the bug scenario
            cache.put(key2, user2Data);  // Overwrites user1's data!
            
            val retrievedData = cache.get(key1);
            fail(String.format(
                "CRITICAL DATA LEAKAGE DETECTED!\n" +
                "User '%s' would receive data belonging to user '%s'\n" +
                "Expected data: %s\n" +
                "Actual data retrieved: %s\n" +
                "Both users map to the same cache key: %s\n" +
                "This allows unauthorized access to sensitive data including SSN!",
                user1, user2, user1Data, retrievedData, key1));
        } else {
            // This is the correct behavior after the fix
            cache.put(key2, user2Data);
            
            assertEquals(user1Data, cache.get(key1), "User 1 should get their own data");
            assertEquals(user2Data, cache.get(key2), "User 2 should get their own data");
            
            System.out.println("✓ Data isolation verified: Each user receives only their own data");
        }
    }

    /**
     * Test multivalued attributes don't collide
     */
    @Test
    void testMultivaluedAttributesNoCollision() {
        val seed1 = Map.of(
            "username", (Object) List.of("gb12359"),
            "groups", (Object) List.of("admin", "developers", "architects")
        );

        val seed2 = Map.of(
            "username", (Object) List.of("hj03694"),
            "groups", (Object) List.of("users", "testers", "analysts")
        );

        generator.setUseAllAttributes(true);

        val key1 = generateCacheKeyFromSeed(seed1);
        val key2 = generateCacheKeyFromSeed(seed2);

        assertNotEquals(key1, key2,
            "Different users with different multivalued attributes should not collide");
    }

    /**
     * Test collision resistance with various string hashCode collisions
     * Some strings naturally have the same Java hashCode() value
     */
    @Test
    void testResistanceToStringHashCodeCollisions() {
        // These pairs have known String.hashCode() collisions
        val collisionPairs = List.of(
            new String[]{"Aa", "BB"},              // Classic Java String hashCode collision
            new String[]{"AaAa", "BBBB"},          // Another known collision
            new String[]{"AaAaAa", "BBBBBB"},      // Extended collision
            new String[]{"polygenelubricants", "GydZG_"} // Known collision pair
        );

        for (val pair : collisionPairs) {
            val str1 = pair[0];
            val str2 = pair[1];
            
            // Verify they have the same String hashCode
            if (str1.hashCode() == str2.hashCode()) {
                val key1 = generateCacheKey(str1);
                val key2 = generateCacheKey(str2);
                
                assertNotEquals(key1, key2,
                    String.format("Even though '%s' and '%s' have the same String.hashCode(), " +
                        "their cache keys MUST be different", str1, str2));
                        
                System.out.printf("✓ Strings '%s' and '%s' (same hashCode=%d) produce different cache keys%n",
                    str1, str2, str1.hashCode());
            }
        }
    }

    /**
     * Test edge cases
     */
    @Test
    void testEdgeCases() {
        val testCases = new HashMap<String, String>();
        
        // Empty string
        testCases.put("empty", "");
        
        // Very long username
        testCases.put("long", "a".repeat(1000));
        
        // Special characters
        testCases.put("special", "user@domain.com!#$%");
        
        // Unicode characters
        testCases.put("unicode", "用户123");
        
        // Whitespace
        testCases.put("whitespace", "user with spaces");
        
        // Numbers only
        testCases.put("numbers", "1234567890");

        val keys = new HashMap<String, String>();
        testCases.forEach((name, username) -> {
            val key = generateCacheKey(username);
            assertNotNull(key, "Cache key should not be null for: " + name);
            keys.put(key, name);
        });

        assertEquals(testCases.size(), keys.size(),
            "All edge case usernames should produce unique cache keys. Found: " + keys.values());
            
        System.out.println("✓ All edge cases produce unique cache keys");
    }

    /**
     * Performance and distribution test
     */
    @Test
    void testCacheKeyDistribution() {
        val count = 5000;
        val keyFrequency = new HashMap<String, Integer>();

        for (int i = 0; i < count; i++) {
            val username = "user" + i;
            val key = generateCacheKey(username);
            keyFrequency.merge(key, 1, Integer::sum);
        }

        val collisions = keyFrequency.values().stream().filter(freq -> freq > 1).count();
        val maxFrequency = keyFrequency.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        System.out.printf("""
            
            === Cache Key Distribution Analysis ===
            Total users: %,d
            Unique cache keys: %,d
            Keys with collisions: %,d
            Maximum collision count: %d
            """, count, keyFrequency.size(), collisions, maxFrequency);

        assertEquals(count, keyFrequency.size(), 
            "Each user should have a unique cache key. Found " + collisions + " collisions");
        assertEquals(1, maxFrequency, 
            "No cache key should be used more than once");
    }

    // Helper methods

    private String generateCacheKey(String username) {
        val seed = Map.of("username", (Object) username);
        return generateCacheKeyFromSeed(seed);
    }

    private String generateCacheKeyFromSeed(Map<String, Object> seed) {
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