package org.apereo.cas.ticket.registry.key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This is {@link RedisKeyGeneratorFactory}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RedisKeyGeneratorFactory {
    private final Map<String, RedisKeyGenerator> keyGeneratorMap = new HashMap<>();

    /**
     * Gets redis key generator.
     *
     * @param key the key
     * @return the redis key generator
     */
    public Optional<RedisKeyGenerator> getRedisKeyGenerator(final String key) {
        return Optional.ofNullable(keyGeneratorMap.get(key));
    }

    /**
     * Register redis key generator.
     *
     * @param key       the key
     * @param generator the generator
     * @return the redis key generator factory
     */
    public RedisKeyGeneratorFactory registerRedisKeyGenerator(final String key, final RedisKeyGenerator generator) {
        keyGeneratorMap.put(key, generator);
        return this;
    }

    /**
     * Gets redis key generators.
     *
     * @return the redis key generators
     */
    public List<RedisKeyGenerator> getRedisKeyGenerators() {
        return new ArrayList<>(keyGeneratorMap.values());
    }

    /**
     * Register redis key generators.
     *
     * @param keyGenerators the key generators
     */
    public void registerRedisKeyGenerators(final RedisKeyGenerator... keyGenerators) {
        Stream.of(keyGenerators).forEach(gen -> registerRedisKeyGenerator(gen.getType(), gen));
    }
}
