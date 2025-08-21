package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalNameTransformerUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.support.password.PasswordEncoderUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import org.apereo.cas.redis.RedisAuthenticationHandler;
import org.apereo.cas.redis.RedisPersonAttributeDao;
import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.redis.core.RedisObjectFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.util.stream.Collectors;

/**
 * This is {@link CasRedisAuthenticationAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Authentication, module = "redis")
@AutoConfiguration
public class CasRedisAuthenticationAutoConfiguration {
    private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.redis.enabled").isTrue().evenIfMissing();

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisPrincipalFactory")
    public PrincipalFactory redisPrincipalFactory(final ConfigurableApplicationContext applicationContext) {
        return BeanSupplier.of(PrincipalFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(PrincipalFactoryUtils::newPrincipalFactory)
            .otherwiseProxy()
            .get();
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisAuthenticationConnectionFactory")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RedisConnectionFactory redisAuthenticationConnectionFactory(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(RedisConnectionFactory.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(Unchecked.supplier(() -> {
                val redis = casProperties.getAuthn().getRedis();
                return RedisObjectFactory.newRedisConnectionFactory(redis, casSslContext);
            }))
            .otherwiseProxy()
            .get();
    }

    @Bean(name = {"authenticationRedisTemplate", "redisTemplate"})
    @ConditionalOnMissingBean(name = "authenticationRedisTemplate")
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public CasRedisTemplate authenticationRedisTemplate(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisAuthenticationConnectionFactory")
        final RedisConnectionFactory redisAuthenticationConnectionFactory) {
        return BeanSupplier.of(CasRedisTemplate.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> RedisObjectFactory.newRedisTemplate(redisAuthenticationConnectionFactory))
            .otherwiseProxy()
            .get();
    }

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "redisAuthenticationHandler")
    public AuthenticationHandler redisAuthenticationHandler(
        final CasConfigurationProperties casProperties,
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisPrincipalFactory")
        final PrincipalFactory redisPrincipalFactory,
        @Qualifier("authenticationRedisTemplate")
        final CasRedisTemplate authenticationRedisTemplate,
        @Qualifier(ServicesManager.BEAN_NAME)
        final ServicesManager servicesManager) {
        return BeanSupplier.of(AuthenticationHandler.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val redis = casProperties.getAuthn().getRedis();
                val handler = new RedisAuthenticationHandler(redis.getName(),
                    redisPrincipalFactory, redis.getOrder(), authenticationRedisTemplate);
                handler.setPrincipalNameTransformer(PrincipalNameTransformerUtils.newPrincipalNameTransformer(redis.getPrincipalTransformation()));
                handler.setPasswordEncoder(PasswordEncoderUtils.newPasswordEncoder(redis.getPasswordEncoder(), applicationContext));
                return handler;
            })
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "redisAuthenticationEventExecutionPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public AuthenticationEventExecutionPlanConfigurer redisAuthenticationEventExecutionPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisAuthenticationHandler")
        final AuthenticationHandler redisAuthenticationHandler,
        @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
        final PrincipalResolver defaultPrincipalResolver) {
        return BeanSupplier.of(AuthenticationEventExecutionPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> plan.registerAuthenticationHandlerWithPrincipalResolver(redisAuthenticationHandler, defaultPrincipalResolver))
            .otherwiseProxy()
            .get();
    }

    @ConditionalOnMissingBean(name = "redisPersonAttributeDaos")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public BeanContainer<PersonAttributeDao> redisPersonAttributeDaos(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier(CasSSLContext.BEAN_NAME)
        final CasSSLContext casSslContext,
        final CasConfigurationProperties casProperties) {
        return BeanSupplier.of(BeanContainer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> {
                val redis = casProperties.getAuthn().getAttributeRepository().getRedis();
                return BeanContainer.of(redis
                    .stream()
                    .filter(r -> StringUtils.isNotBlank(r.getHost())).map(Unchecked.function(r -> {
                        val conn = RedisObjectFactory.newRedisConnectionFactory(r, true, casSslContext);
                        val template = RedisObjectFactory.newRedisTemplate(conn);
                        template.initialize();
                        val cb = new RedisPersonAttributeDao(template);
                        cb.setOrder(r.getOrder());
                        FunctionUtils.doIfNotNull(r.getId(), id -> cb.setId(id));
                        return cb;
                    }))
                    .collect(Collectors.toList()));
            })
            .otherwise(BeanContainer::empty)
            .get();
    }

    @ConditionalOnMissingBean(name = "redisAttributeRepositoryPlanConfigurer")
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public PersonDirectoryAttributeRepositoryPlanConfigurer redisAttributeRepositoryPlanConfigurer(
        final ConfigurableApplicationContext applicationContext,
        @Qualifier("redisPersonAttributeDaos")
        final BeanContainer<PersonAttributeDao> redisPersonAttributeDaos) {
        return BeanSupplier.of(PersonDirectoryAttributeRepositoryPlanConfigurer.class)
            .when(CONDITION.given(applicationContext.getEnvironment()))
            .supply(() -> plan -> redisPersonAttributeDaos.toList().stream().filter(PersonAttributeDao::isEnabled).forEach(plan::registerAttributeRepository))
            .otherwiseProxy()
            .get();
    }
}
