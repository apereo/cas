package org.apereo.cas.ticket.registry.key;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * @param prefix the key
     * @return the redis key generator
     */
    public Optional<RedisKeyGenerator> getRedisKeyGenerator(final String prefix) {
        return Optional.ofNullable(keyGeneratorMap.get(prefix));
    }

    /**
     * Register redis key generator.
     *
     * @param generator the generator
     * @return the redis key generator factory
     */
    public RedisKeyGeneratorFactory registerRedisKeyGenerator(final RedisKeyGenerator generator) {
        keyGeneratorMap.put(generator.getPrefix(), generator);
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
}
