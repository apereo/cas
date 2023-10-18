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

    public Optional<RedisKeyGenerator> getRedisKeyGenerator(final String key) {
        return Optional.ofNullable(keyGeneratorMap.get(key));
    }

    public RedisKeyGeneratorFactory registerRedisKeyGenerator(final String key, final RedisKeyGenerator generator) {
        keyGeneratorMap.put(key, generator);
        return this;
    }

    public List<RedisKeyGenerator> getRedisKeyGenerators() {
        return new ArrayList<>(keyGeneratorMap.values());
    }

    public void registerRedisKeyGenerators(final RedisKeyGenerator... keyGenerators) {
        Stream.of(keyGenerators).forEach(gen -> registerRedisKeyGenerator(gen.getType(), gen));
    }
}
