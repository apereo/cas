package org.apereo.cas;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
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
public class MongoDbPropertySourceLocator implements PropertySourceLocator {
    private final MongoOperations mongo;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            val sourceName = MongoDbPropertySource.class.getSimpleName();
            val composite = new CompositePropertySource(sourceName);
            val source = new MongoDbPropertySource(sourceName, mongo);
            composite.addFirstPropertySource(source);
            return composite;
        }
        return null;
    }
}
