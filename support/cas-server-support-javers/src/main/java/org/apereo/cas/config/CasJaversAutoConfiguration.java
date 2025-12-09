package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.version.EntityHistoryEndpoint;
import org.apereo.cas.version.EntityHistoryRepository;
import org.apereo.cas.version.JaversEntityHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.javers.core.Javers;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.MongoDatabaseFactory;

/**
 * This is {@link CasJaversAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit, module = "javers")
@AutoConfiguration
@Lazy(false)
public class CasJaversAutoConfiguration {

    @Configuration(value = "CasJaversMongoDbRepositoryConfiguration", proxyBeanMethods = false)
    static class CasJaversMongoDbRepositoryConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "javersMongoDatabaseFactory")
        @RefreshScope
        public MongoDatabaseFactory javersMongoDatabaseFactory(
            final CasConfigurationProperties casProperties) {
            val factory = new MongoDbConnectionFactory();
            val operations = factory.buildMongoTemplate(casProperties.getJavers().getMongo());
            return operations.getMongoDatabaseFactory();
        }
    }

    @Configuration(value = "CasJaversCoreConfiguration", proxyBeanMethods = false)
    static class CasJaversCoreConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnAvailableEndpoint
        public EntityHistoryEndpoint entityHistoryEndpoint(
            @Qualifier(ServicesManager.BEAN_NAME) final ObjectProvider<@NonNull ServicesManager> servicesManager,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("objectVersionRepository")
            final ObjectProvider<@NonNull EntityHistoryRepository> objectVersionRepository,
            final CasConfigurationProperties casProperties) {
            return new EntityHistoryEndpoint(casProperties, applicationContext, objectVersionRepository, servicesManager);
        }

        @Bean
        @ConditionalOnMissingBean(name = "objectVersionRepository")
        @RefreshScope
        public EntityHistoryRepository objectVersionRepository(
            @Qualifier("JaversFromStarter") final Javers javers) {
            return new JaversEntityHistoryRepository(javers);
        }

        @Bean
        @ConditionalOnMissingBean(name = "servicesManagerJaversAspect")
        public ServicesManagerJaversAspect servicesManagerJaversAspect(
            @Qualifier("objectVersionRepository") final EntityHistoryRepository objectVersionRepository) {
            return new ServicesManagerJaversAspect(objectVersionRepository);
        }

        @Aspect
        @Slf4j
        @SuppressWarnings("UnusedMethod")
        record ServicesManagerJaversAspect(EntityHistoryRepository objectVersionRepository) {

            @Around("serviceManagementSaveOperation()")
            public Object serviceManagementSaveOperation(final ProceedingJoinPoint joinPoint) throws Throwable {
                val result = joinPoint.proceed();
                return objectVersionRepository.save(result);
            }

            @Pointcut("execution(* org.apereo.cas.services.ServicesManager.save(..))")
            private void serviceManagementSaveOperation() {
            }
        }
    }
}
