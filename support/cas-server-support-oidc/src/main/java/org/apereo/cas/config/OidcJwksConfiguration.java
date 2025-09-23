package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyCacheKey;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcGroovyJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcDefaultJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.jwks.rotation.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanCondition;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.util.spring.boot.ConditionalOnMissingGraalVMNativeImage;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.JsonWebKeySet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.function.Supplier;

/**
 * This is {@link OidcJwksConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.OpenIDConnect)
@Configuration(value = "OidcJwksConfiguration", proxyBeanMethods = false)
class OidcJwksConfiguration {

    @Configuration(value = "OidcEndpointsJwksRotationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcEndpointsJwksRotationConfiguration {
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
        @Lazy(false)
        public Runnable oidcJsonWebKeystoreRotationScheduler(
            final ConfigurableApplicationContext applicationContext,
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return BeanSupplier.of(Runnable.class)
                .when(BeanCondition.on("cas.authn.oidc.jwks.rotation.schedule").isTrue().given(applicationContext.getEnvironment()))
                .supply(() -> new OidcJsonWebKeystoreRotationScheduler(oidcJsonWebKeystoreRotationService))
                .otherwiseProxy()
                .get();
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRevocationScheduler")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
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
        static class OidcJsonWebKeystoreRotationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(
                cron = "${cas.authn.oidc.jwks.rotation.schedule.cron-expression:}",
                initialDelayString = "${cas.authn.oidc.jwks.rotation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.rotation.schedule.repeat-interval:P90D}")
            @Override
            public void run() {
                FunctionUtils.doUnchecked(__ -> {
                    LOGGER.info("Starting to rotate keys in the OIDC keystore...");
                    rotationService.rotate();
                });
            }
        }

        @RequiredArgsConstructor
        @Slf4j
        static class OidcJsonWebKeystoreRevocationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(
                cron = "${cas.authn.oidc.jwks.revocation.schedule.cron-expression:}",
                zone = "${cas.authn.oidc.jwks.revocation.schedule.cron-time-zone:}",
                initialDelayString = "${cas.authn.oidc.jwks.revocation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.revocation.schedule.repeat-interval:P14D}")
            @Override
            public void run() {
                FunctionUtils.doUnchecked(__ -> {
                    LOGGER.info("Starting to revoke keys in the OIDC keystore...");
                    rotationService.revoke();
                });
            }
        }
    }

    @Configuration(value = "OidcEndpointsJwksGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class OidcEndpointsJwksGeneratorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCacheLoader")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CacheLoader<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCacheLoader(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService) {
            return new OidcDefaultJsonWebKeystoreCacheLoader(oidcJsonWebKeystoreGeneratorService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeyStoreListener")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Lazy(false)
        public OidcJsonWebKeyStoreListener oidcJsonWebKeyStoreListener(
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache) {
            return new OidcDefaultJsonWebKeyStoreListener(oidcDefaultJsonWebKeystoreCache);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCache")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoadingCache<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCache(
            @Qualifier("oidcDefaultJsonWebKeystoreCacheLoader")
            final CacheLoader<OidcJsonWebKeyCacheKey, JsonWebKeySet> oidcDefaultJsonWebKeystoreCacheLoader,
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
        @ConditionalOnMissingGraalVMNativeImage
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
