package org.apereo.cas;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * This is {@link MongoDbPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MongoDbPropertySourceLocator implements PropertySourceLocator {
    private final MongoOperations mongo;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val sourceName = MongoDbPropertySource.class.getSimpleName();
        return new MongoDbPropertySource(sourceName, mongo);
    }
}
