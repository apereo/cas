package org.apereo.cas.config;

import module java.base;
import io.etcd.jetcd.Client;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * This is {@link EtcdPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EtcdPropertySourceLocator implements PropertySourceLocator {
    private final Supplier<Client> clientFactory;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val sourceName = EtcdPropertySource.class.getSimpleName();
        return new EtcdPropertySource(sourceName, clientFactory, List.of(environment.getActiveProfiles()));
    }

}
