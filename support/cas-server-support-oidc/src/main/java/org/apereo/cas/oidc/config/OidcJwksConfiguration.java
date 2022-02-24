package org.apereo.cas.oidc.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.PseudoPlatformTransactionManager;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcGroovyJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreEntity;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.jpa.OidcJpaJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.mongo.OidcMongoDbJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcDefaultJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This is {@link OidcJwksConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Configuration(value = "OidcJwksConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OidcJwksConfiguration {

    @Configuration(value = "OidcEndpointsJwksMongoDbConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(MongoTemplate.class)
    public static class OidcEndpointsJwksMongoDbConfiguration {
        private static final BeanCondition CONDITION_HOST = BeanCondition.on("cas.authn.oidc.jwks.mongo.host");

        private static final BeanCondition CONDITION_COLLECTION = BeanCondition.on("cas.authn.oidc.jwks.mongo.collection");


        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public MongoOperations mongoOidcJsonWebKeystoreTemplate(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            return BeanSupplier.of(MongoOperations.class)
                .when(CONDITION_HOST.given(applicationContext.getEnvironment()))
                .and(CONDITION_COLLECTION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val mongo = casProperties.getAuthn().getOidc().getJwks().getMongo();
                    val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
                    val mongoTemplate = factory.buildMongoTemplate(mongo);
                    MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
                    return mongoTemplate;
                })
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public Supplier<OidcJsonWebKeystoreGeneratorService> mongoOidcJsonWebKeystoreGeneratorService(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties,
            @Qualifier("mongoOidcJsonWebKeystoreTemplate")
            final MongoOperations mongoOidcJsonWebKeystoreTemplate) {
            return BeanSupplier.of(Supplier.class)
                .when(CONDITION_HOST.given(applicationContext.getEnvironment()))
                .and(CONDITION_COLLECTION.given(applicationContext.getEnvironment()))
                .supply(() -> () -> new OidcMongoDbJsonWebKeystoreGeneratorService(mongoOidcJsonWebKeystoreTemplate,
                    casProperties.getAuthn().getOidc()))
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "OidcEndpointsJwksJpaConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(JpaBeanFactory.class)
    public static class OidcEndpointsJwksJpaConfiguration {
        private static final BeanCondition CONDITION = BeanCondition.on("cas.authn.oidc.jwks.jpa.url");

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "transactionManagerOidcJwks")
        public PlatformTransactionManager transactionManagerOidcJwks(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oidcJwksEntityManagerFactory")
            final EntityManagerFactory emf) {

            return BeanSupplier.of(PlatformTransactionManager.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val mgmr = new JpaTransactionManager();
                    mgmr.setEntityManagerFactory(emf);
                    return mgmr;
                })
                .otherwise(PseudoPlatformTransactionManager::new)
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJwksEntityManagerFactory")
        public EntityManagerFactory oidcJwksEntityManagerFactory(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("jpaOidcJwksVendorAdapter")
            final JpaVendorAdapter jpaOidcJwksVendorAdapter,
            @Qualifier("dataSourceOidcJwks")
            final DataSource dataSourceOidcJwks,
            @Qualifier("jpaOidcJwksPackagesToScan")
            final BeanContainer<String> jpaOidcJwksPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(EntityManagerFactory.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(Unchecked.supplier(() -> {
                    val ctx = JpaConfigurationContext.builder()
                        .jpaVendorAdapter(jpaOidcJwksVendorAdapter)
                        .persistenceUnitName("jpaOidcJwksContext")
                        .dataSource(dataSourceOidcJwks)
                        .packagesToScan(jpaOidcJwksPackagesToScan.toSet())
                        .build();
                    return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                        casProperties.getAuthn().getOidc().getJwks().getJpa()).getObject();
                }))
                .otherwiseProxy()
                .get();
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "jpaOidcJwksVendorAdapter")
        public JpaVendorAdapter jpaOidcJwksVendorAdapter(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(JpaVendorAdapter.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public BeanContainer<String> jpaOidcJwksPackagesToScan(final ConfigurableApplicationContext applicationContext) {
            return BeanSupplier.of(BeanContainer.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> BeanContainer.of(CollectionUtils.wrapSet(OidcJsonWebKeystoreEntity.class.getPackage().getName())))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceOidcJwks")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceOidcJwks(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(DataSource.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> JpaBeans.newDataSource(casProperties.getAuthn().getOidc().getJwks().getJpa()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Supplier<OidcJsonWebKeystoreGeneratorService> jpaJsonWebKeystoreGeneratorService(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("transactionManagerOidcJwks")
            final PlatformTransactionManager transactionManagerOidcJwks,
            final CasConfigurationProperties casProperties) {
            return BeanSupplier.of(Supplier.class)
                .when(CONDITION.given(applicationContext.getEnvironment()))
                .supply(() -> {
                    val oidc = casProperties.getAuthn().getOidc();
                    LOGGER.info("Managing JWKS via a relational database at [{}]", oidc.getJwks().getJpa().getUrl());
                    val transactionTemplate = new TransactionTemplate(transactionManagerOidcJwks);
                    return () -> new OidcJpaJsonWebKeystoreGeneratorService(oidc, transactionTemplate);
                })
                .otherwiseProxy()
                .get();
        }
    }

    @Configuration(value = "OidcEndpointsJwksRotationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsJwksRotationConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRotationService")
        public OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcDefaultJsonWebKeystoreRotationService(oidc, oidcJsonWebKeystoreGeneratorService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRotationScheduler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Runnable oidcJsonWebKeystoreRotationScheduler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) throws Exception {
            return BeanSupplier.of(Runnable.class)
                .when(BeanCondition.on("cas.authn.oidc.jwks.rotation.schedule").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> new OidcJsonWebKeystoreRotationScheduler(oidcJsonWebKeystoreRotationService))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRevocationScheduler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public Runnable oidcJsonWebKeystoreRevocationScheduler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return BeanSupplier.of(Runnable.class)
                .when(BeanCondition.on("cas.authn.oidc.jwks.revocation.schedule.enabled").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> new OidcJsonWebKeystoreRevocationScheduler(oidcJsonWebKeystoreRotationService))
                .otherwiseProxy()
                .get();
        }

        @RequiredArgsConstructor
        @Slf4j
        public static class OidcJsonWebKeystoreRotationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(initialDelayString = "${cas.authn.oidc.jwks.rotation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.rotation.schedule.repeat-interval:P90D}")
            @Override
            public void run() {
                FunctionUtils.doUnchecked(ig -> {
                    LOGGER.info("Starting to rotate keys in the OIDC keystore...");
                    rotationService.rotate();
                });
            }
        }

        @RequiredArgsConstructor
        @Slf4j
        public static class OidcJsonWebKeystoreRevocationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(initialDelayString = "${cas.authn.oidc.jwks.revocation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.revocation.schedule.repeat-interval:P14D}")
            @Override
            public void run() {
                FunctionUtils.doUnchecked(ig -> {
                    LOGGER.info("Starting to revoke keys in the OIDC keystore...");
                    rotationService.revoke();
                });
            }
        }
    }

    @Configuration(value = "OidcEndpointsJwksGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsJwksGeneratorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCacheLoader")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CacheLoader<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCacheLoader(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService) {
            return new OidcDefaultJsonWebKeystoreCacheLoader(oidcJsonWebKeystoreGeneratorService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeyStoreListener")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJsonWebKeyStoreListener oidcJsonWebKeyStoreListener(
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCache) {
            return new OidcDefaultJsonWebKeyStoreListener(oidcDefaultJsonWebKeystoreCache);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCache")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoadingCache<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCache(
            @Qualifier("oidcDefaultJsonWebKeystoreCacheLoader")
            final CacheLoader<OidcJsonWebKeyCacheKey, Optional<JsonWebKeySet>> oidcDefaultJsonWebKeystoreCacheLoader,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();

            val expiration = Beans.newDuration(oidc.getJwks().getCore().getJwksCacheExpiration());
            return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(expiration)
                .build(oidcDefaultJsonWebKeystoreCacheLoader);
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "groovyOidcJsonWebKeystoreGeneratorService")
        @SuppressWarnings("unchecked")
        public Supplier<OidcJsonWebKeystoreGeneratorService> groovyOidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val oidc = casProperties.getAuthn().getOidc();
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.oidc.jwks.groovy.location").exists().given(applicationContext.getEnvironment()))
                .supply(() -> () -> new OidcGroovyJsonWebKeystoreGeneratorService(oidc.getJwks().getGroovy().getLocation()))
                .otherwiseProxy()
                .get();
        }

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @SuppressWarnings("unchecked")
        @ConditionalOnMissingBean(name = "restOidcJsonWebKeystoreGeneratorService")
        public Supplier<OidcJsonWebKeystoreGeneratorService> restOidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext) {
            val oidc = casProperties.getAuthn().getOidc();
            return BeanSupplier.of(Supplier.class)
                .when(BeanCondition.on("cas.authn.oidc.jwks.rest.url").isUrl().given(applicationContext.getEnvironment()))
                .supply(() -> () -> new OidcRestfulJsonWebKeystoreGeneratorService(oidc))
                .otherwiseProxy()
                .get();
        }

        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreGeneratorService")
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties,
            final ConfigurableApplicationContext applicationContext,
            final List<Supplier<OidcJsonWebKeystoreGeneratorService>> oidsJwksSuppliers) {
            val supplier = oidsJwksSuppliers
                .stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .filter(BeanSupplier::isNotProxy)
                .findFirst()
                .orElse(() -> {
                    val oidc = casProperties.getAuthn().getOidc();
                    return new OidcDefaultJsonWebKeystoreGeneratorService(oidc, applicationContext);
                });
            return supplier.get();
        }
    }

}
