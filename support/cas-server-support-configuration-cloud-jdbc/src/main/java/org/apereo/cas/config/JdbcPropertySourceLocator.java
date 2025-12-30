package org.apereo.cas.config;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This is {@link JdbcPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JdbcPropertySourceLocator implements PropertySourceLocator {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val sourceName = JdbcPropertySource.class.getSimpleName();
        return new JdbcPropertySource(sourceName, jdbcTemplate);
    }

}
