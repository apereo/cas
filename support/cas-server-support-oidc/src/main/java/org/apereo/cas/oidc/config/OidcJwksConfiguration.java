package org.apereo.cas.oidc.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeyStoreListener;
import org.apereo.cas.oidc.jwks.OidcDefaultJsonWebKeystoreCacheLoader;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.OidcJsonWebKeystoreRotationService;
import org.apereo.cas.oidc.jwks.generator.OidcDefaultJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcGroovyJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.generator.OidcRestfulJsonWebKeystoreGeneratorService;
import org.apereo.cas.oidc.jwks.rotation.OidcDefaultJsonWebKeystoreRotationService;
import org.apereo.cas.util.spring.CasEventListener;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jose4j.jwk.PublicJsonWebKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Scheduled;

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
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();
            return new OidcDefaultJsonWebKeystoreRotationService(oidc, applicationContext);
        }


        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRotationScheduler")
        @Bean
        @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.rotation.schedule",
            name = "enabled", havingValue = "true", matchIfMissing = true)
        public Runnable oidcJsonWebKeystoreRotationScheduler(
            @Qualifier("oidcJsonWebKeystoreRotationService")
            final OidcJsonWebKeystoreRotationService oidcJsonWebKeystoreRotationService) {
            return new OidcJsonWebKeystoreRotationScheduler(oidcJsonWebKeystoreRotationService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeystoreRevocationScheduler")
        @Bean
        @ConditionalOnProperty(prefix = "cas.authn.oidc.jwks.revocation.schedule",
            name = "enabled", havingValue = "true", matchIfMissing = true)
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
            @SneakyThrows
            public void run() {
                LOGGER.info("Starting to rotate keys in the OIDC keystore...");
                rotationService.rotate();
            }
        }

        @RequiredArgsConstructor
        @Slf4j
        public static class OidcJsonWebKeystoreRevocationScheduler implements Runnable {
            private final OidcJsonWebKeystoreRotationService rotationService;

            @Scheduled(initialDelayString = "${cas.authn.oidc.jwks.revocation.schedule.start-delay:PT60S}",
                fixedDelayString = "${cas.authn.oidc.jwks.revocation.schedule.repeat-interval:P14D}")
            @Override
            @SneakyThrows
            public void run() {
                LOGGER.info("Starting to revoke keys in the OIDC keystore...");
                rotationService.revoke();
            }
        }
    }

    @Configuration(value = "OidcEndpointsJwksGeneratorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class OidcEndpointsJwksGeneratorConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCacheLoader")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public CacheLoader<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCacheLoader(
            @Qualifier("oidcJsonWebKeystoreGeneratorService")
            final OidcJsonWebKeystoreGeneratorService oidcJsonWebKeystoreGeneratorService) {
            return new OidcDefaultJsonWebKeystoreCacheLoader(oidcJsonWebKeystoreGeneratorService);
        }

        @ConditionalOnMissingBean(name = "oidcJsonWebKeyStoreListener")
        @Bean
        public CasEventListener oidcJsonWebKeyStoreListener(
            @Qualifier("oidcDefaultJsonWebKeystoreCache")
            final LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache) {
            return new OidcDefaultJsonWebKeyStoreListener(oidcDefaultJsonWebKeystoreCache);
        }

        @Bean
        @ConditionalOnMissingBean(name = "oidcDefaultJsonWebKeystoreCache")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public LoadingCache<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCache(
            @Qualifier("oidcDefaultJsonWebKeystoreCacheLoader")
            final CacheLoader<String, Optional<PublicJsonWebKey>> oidcDefaultJsonWebKeystoreCacheLoader,
            final CasConfigurationProperties casProperties) {
            val oidc = casProperties.getAuthn().getOidc();

            val expiration = Beans.newDuration(oidc.getJwks().getJwksCacheExpiration());
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
