package org.apereo.cas.oidc.config;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.jpa.JpaConfigurationContext;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.configuration.support.JpaBeans;
import org.apereo.cas.jpa.JpaBeanFactory;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
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
import org.apereo.cas.util.spring.BeanContainer;
import org.apereo.cas.util.spring.CasEventListener;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Optional;

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
    @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.mongo", name = {"host", "collection"})
    public static class OidcEndpointsJwksMongoDbConfiguration {
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public MongoTemplate mongoOidcJsonWebKeystoreTemplate(
            final CasConfigurationProperties casProperties,
            @Qualifier(CasSSLContext.BEAN_NAME)
            final CasSSLContext casSslContext) {
            val mongo = casProperties.getAuthn().getOidc().getJwks().getMongo();
            val factory = new MongoDbConnectionFactory(casSslContext.getSslContext());
            val mongoTemplate = factory.buildMongoTemplate(mongo);
            MongoDbConnectionFactory.createCollection(mongoTemplate, mongo.getCollection(), mongo.isDropCollection());
            return mongoTemplate;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties,
            @Qualifier("mongoOidcJsonWebKeystoreTemplate")
            final MongoTemplate mongoOidcJsonWebKeystoreTemplate) {
            return new OidcMongoDbJsonWebKeystoreGeneratorService(mongoOidcJsonWebKeystoreTemplate,
                casProperties.getAuthn().getOidc());
        }
    }

    @Configuration(value = "OidcEndpointsJwksJpaConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnClass(JpaBeanFactory.class)
    @ConditionalOnProperty(name = "cas.authn.oidc.jwks.jpa.url")
    public static class OidcEndpointsJwksJpaConfiguration {
        @Bean
        public PlatformTransactionManager transactionManagerOidcJwks(
            @Qualifier("oidcJwksEntityManagerFactory")
            final EntityManagerFactory emf) {
            val mgmr = new JpaTransactionManager();
            mgmr.setEntityManagerFactory(emf);
            return mgmr;
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean oidcJwksEntityManagerFactory(
            @Qualifier("jpaOidcJwksVendorAdapter")
            final JpaVendorAdapter jpaOidcJwksVendorAdapter,
            @Qualifier("dataSourceOidcJwks")
            final DataSource dataSourceOidcJwks,
            @Qualifier("jpaOidcJwksPackagesToScan")
            final BeanContainer<String> jpaOidcJwksPackagesToScan,
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            val ctx = JpaConfigurationContext.builder()
                .jpaVendorAdapter(jpaOidcJwksVendorAdapter)
                .persistenceUnitName("jpaOidcJwksContext")
                .dataSource(dataSourceOidcJwks)
                .packagesToScan(jpaOidcJwksPackagesToScan.toSet())
                .build();
            return jpaBeanFactory.newEntityManagerFactoryBean(ctx,
                casProperties.getAuthn().getOidc().getJwks().getJpa());
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public JpaVendorAdapter jpaOidcJwksVendorAdapter(
            @Qualifier(JpaBeanFactory.DEFAULT_BEAN_NAME)
            final JpaBeanFactory jpaBeanFactory,
            final CasConfigurationProperties casProperties) {
            return jpaBeanFactory.newJpaVendorAdapter(casProperties.getJdbc());
        }

        @Bean
        public BeanContainer<String> jpaOidcJwksPackagesToScan() {
            return BeanContainer.of(CollectionUtils.wrapSet(OidcJsonWebKeystoreEntity.class.getPackage().getName()));
        }

        @Bean
        @ConditionalOnMissingBean(name = "dataSourceOidcJwks")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public DataSource dataSourceOidcJwks(final CasConfigurationProperties casProperties) {
            return JpaBeans.newDataSource(casProperties.getAuthn().getOidc().getJwks().getJpa());
        }

        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            @Qualifier("transactionManagerOidcJwks")
            final PlatformTransactionManager transactionManagerOidcJwks,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            LOGGER.info("Managing JWKS via a relational database at [{}]", oidc.getJwks().getJpa().getUrl());
            val transactionTemplate = new TransactionTemplate(transactionManagerOidcJwks);
            return new OidcJpaJsonWebKeystoreGeneratorService(oidc, transactionTemplate);
        }
    }

    @Configuration(value = "OidcEndpointsJwksRestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(name = "cas.authn.oidc.jwks.rest.url")
    public static class OidcEndpointsJwksRestConfiguration {
        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcRestfulJsonWebKeystoreGeneratorService(oidc);
        }
    }

    @Configuration(value = "OidcEndpointsJwksGroovyConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    @ConditionalOnProperty(name = "cas.authn.oidc.jwks.groovy.location")
    public static class OidcEndpointsJwksGroovyConfiguration {
        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcGroovyJsonWebKeystoreGeneratorService(oidc.getJwks().getGroovy().getLocation());
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
        @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.rotation.schedule",
            name = "enabled", havingValue = "true", matchIfMissing = false)
        public Runnable oidcJsonWebKeystoreRotationScheduler(
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return new OidcJsonWebKeystoreRotationScheduler(oidcJsonWebKeystoreRotationService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRevocationScheduler")
        @Bean
        @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.revocation.schedule",
            name = "enabled", havingValue = "true", matchIfMissing = false)
        public Runnable oidcJsonWebKeystoreRevocationScheduler(
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return new OidcJsonWebKeystoreRevocationScheduler(oidcJsonWebKeystoreRotationService);
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
        public CasEventListener oidcJsonWebKeyStoreListener(
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

        @Bean(initMethod = "generate")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreGeneratorService")
        public OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcDefaultJsonWebKeystoreGeneratorService(oidc, applicationContext);
        }
    }

}
