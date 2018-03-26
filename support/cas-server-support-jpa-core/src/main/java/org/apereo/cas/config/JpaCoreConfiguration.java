package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.jpa.DefaultJpaStreamerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

/**
 * Configuration for core JPA interface components.
 *
 * @author Timur Duehr
 * @since 5.3.0
 */
@Configuration("jpaCoreConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@EnableTransactionManagement(proxyTargetClass = true)
@Slf4j
public class JpaCoreConfiguration {

    @ConditionalOnMissingBean(name = "jpaEntityManagerFactoryBeanFactory")
    @Bean
    public DefaultJpaEntityManagerFactoryBeanFactory jpaEntityManagerFactoryBeanFactory(final List<JpaEntityManagerFactoryBeanFactoryConfigurer> configurers) {
        final DefaultJpaEntityManagerFactoryBeanFactory factory = new DefaultJpaEntityManagerFactoryBeanFactory();

        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring logout execution plan [{}]", name);
            c.configureDefaultJpaEntityManagerFactoryBeanFactory(factory);
        });
        return factory;
    }

    @ConditionalOnMissingBean(name = "jpaStreamerFactory")
    @Bean
    public DefaultJpaStreamerFactory jpaStreamerFactory(final List<JpaStreamerFactoryConfigurer> configurers) {
        final DefaultJpaStreamerFactory factory = new DefaultJpaStreamerFactory();
        configurers.forEach(c -> {
            final String name = StringUtils.removePattern(c.getClass().getSimpleName(), "\\$.+");
            LOGGER.debug("Configuring JpaStreamerFactory [{}]", name);
            c.configureDefaultJpaStreamerFactory(factory);
        });
        return factory;
    }
}
