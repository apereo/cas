package org.apereo.cas.config;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.services.RegisteredService;
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
import org.jspecify.annotations.Nullable;
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
    @ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.Audit, module = "javers-mongo")
    static class CasJaversMongoDbRepositoryConfiguration {

        @Bean
        @ConditionalOnMissingBean(name = "javersMongoDatabaseFactory")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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
            @Qualifier(ServicesManager.BEAN_NAME) final ObjectProvider<ServicesManager> servicesManager,
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("objectVersionRepository")
            final ObjectProvider<EntityHistoryRepository> objectVersionRepository,
            final CasConfigurationProperties casProperties) {
            return new EntityHistoryEndpoint(casProperties, applicationContext, objectVersionRepository, servicesManager);
        }

        @Bean
        @ConditionalOnMissingBean(name = "objectVersionRepository")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
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

            @Pointcut("execution(* org.apereo.cas.services.ServicesManager.save(..))")
            private void serviceManagementSaveOperation() {
            }
            
            /**
             * Record the history of every service the services manager saves. A single save returns the saved service,
             * or nothing when no manager supports it; the bulk saves return nothing, so their services are recorded as
             * they are saved: a stream is read into a list first, since it can be read once, and a supplier's services
             * as its consumer receives them.
             *
             * @param joinPoint the join point
             * @return the result of the save
             * @throws Throwable the throwable
             */
            @Around("serviceManagementSaveOperation()")
            @SuppressWarnings("unchecked")
            public Object serviceManagementSaveOperation(final ProceedingJoinPoint joinPoint) throws Throwable {
                val args = joinPoint.getArgs();
                if (args.length == 1 && args[0] instanceof final Stream<?> stream) {
                    val services = stream.toList();
                    val result = joinPoint.proceed(new Object[]{services.stream()});
                    services.forEach(this::remember);
                    return result;
                }
                if (args.length == 3 && args[1] instanceof final Consumer<?> andThenConsume) {
                    val consumer = (Consumer<Object>) andThenConsume;
                    return joinPoint.proceed(new Object[]{args[0], (Consumer<Object>) service -> {
                        consumer.accept(service);
                        remember(service);
                    }, args[2]});
                }
                val result = joinPoint.proceed();
                if (args.length == 1 && args[0] instanceof final RegisteredService[] services) {
                    Arrays.stream(services).forEach(this::remember);
                }
                remember(result);
                return result;
            }

            private void remember(final @Nullable Object entity) {
                if (entity != null) {
                    objectVersionRepository.save(entity);
                }
            }

            @Pointcut("execution(* org.apereo.cas.services.ServicesManager.delete(..))")
            private void serviceManagementDeleteOperation() {
            }

            /**
             * Record the deletion of a service the services manager deletes, so its history ends with who deleted it
             * and when. A deletion that removed nothing is not recorded, and neither is the deletion of a service that
             * has no history, such as one loaded from a file and never saved through CAS.
             *
             * @param joinPoint the join point
             * @return the deleted service, if any
             * @throws Throwable the throwable
             */
            @Around("serviceManagementDeleteOperation()")
            public Object serviceManagementDeleteOperation(final ProceedingJoinPoint joinPoint) throws Throwable {
                val result = joinPoint.proceed();
                if (result instanceof final RegisteredService deleted) {
                    try {
                        objectVersionRepository.delete(deleted);
                    } catch (final Exception e) {
                        LOGGER.debug("The deletion of [{}] is not recorded in its history: [{}]", deleted.getName(), e.getMessage());
                    }
                }
                return result;
            }


        }
    }
}
