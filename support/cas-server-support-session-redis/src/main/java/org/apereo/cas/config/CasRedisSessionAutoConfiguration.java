package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.SpringSessionRedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * This is {@link CasRedisSessionAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableRedisHttpSession
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SessionManagement, module = "redis")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@AutoConfiguration(
    afterName = "org.apereo.cas.config.CasRedisTicketRegistryAutoConfiguration",
    before = DataRedisAutoConfiguration.class)
public class CasRedisSessionAutoConfiguration {

    /**
     * Use the ticket registry's Redis connection for Spring Session when no dedicated
     * Spring Data Redis configuration is provided. CAS-owned Redis connection factories
     * are not default autowire candidates, so this qualified bridge also prevents Spring
     * Boot from creating an unused connection factory with its default settings.
     *
     * @param redisTicketConnectionFactory the ticket registry connection factory
     * @return the Spring Session Redis connection factory
     */
    @Bean
    @SpringSessionRedisConnectionFactory
    @ConditionalOnBean(name = "redisTicketConnectionFactory")
    @ConditionalOnMissingBean(
        name = "springSessionRedisConnectionFactory",
        annotation = SpringSessionRedisConnectionFactory.class)
    @Conditional(SpringDataRedisPropertiesAbsentCondition.class)
    public RedisConnectionFactory springSessionRedisConnectionFactory(
        @Qualifier("redisTicketConnectionFactory")
        final RedisConnectionFactory redisTicketConnectionFactory) {
        return redisTicketConnectionFactory;
    }

    static final class SpringDataRedisPropertiesAbsentCondition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            val properties = Binder.get(context.getEnvironment())
                .bind("spring.data.redis", Bindable.of(DataRedisProperties.class));
            return properties.isBound()
                ? ConditionOutcome.noMatch("Spring Data Redis properties are configured")
                : ConditionOutcome.match("Spring Data Redis properties are not configured");
        }
    }
}
