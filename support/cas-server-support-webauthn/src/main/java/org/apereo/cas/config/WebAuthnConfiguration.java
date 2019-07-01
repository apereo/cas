package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.webauthn.WebAuthnOperations;
import org.apereo.cas.webauthn.attestation.DefaultAttestationCertificateTrustResolver;
import org.apereo.cas.webauthn.credential.repository.CachingInMemoryWebAuthnCredentialRepository;
import org.apereo.cas.webauthn.credential.repository.WebAuthnCredentialRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.yubico.internal.util.WebAuthnCodecs;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.attestation.AttestationResolver;
import com.yubico.webauthn.attestation.MetadataObject;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.StandardMetadataService;
import com.yubico.webauthn.attestation.TrustResolver;
import com.yubico.webauthn.attestation.resolver.CompositeAttestationResolver;
import com.yubico.webauthn.attestation.resolver.CompositeTrustResolver;
import com.yubico.webauthn.attestation.resolver.SimpleAttestationResolver;
import com.yubico.webauthn.attestation.resolver.SimpleTrustResolver;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.extension.appid.AppId;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link WebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("webAuthnConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class WebAuthnConfiguration {
    private static final int CACHE_MAX_SIZE = 10_000;

    private static final ObjectMapper MAPPER = WebAuthnCodecs.json().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @ConditionalOnMissingBean(name = "webAuthnCredentialRepository")
    @Bean
    @RefreshScope
    public WebAuthnCredentialRepository webAuthnCredentialRepository() {
        return new CachingInMemoryWebAuthnCredentialRepository();
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnMetadataService")
    public MetadataService webAuthnMetadataService() throws Exception {
        val foundTrustResolvers = applicationContext.getBeansOfType(TrustResolver.class, false, true);
        val trustResolvers = new ArrayList<TrustResolver>();
        trustResolvers.add(StandardMetadataService.createDefaultTrustResolver());

        val resource = casProperties.getAuthn().getMfa().getWebAuthn().getTrustedDeviceMetadata().getLocation();
        if (ResourceUtils.doesResourceExist(resource)) {
            val metadata = MAPPER.readValue(resource.getInputStream(), MetadataObject.class);
            val trustedCertificates = metadata.getParsedTrustedCertificates();
            val resolver = new SimpleTrustResolver(trustedCertificates);
            val trustedCerts = ArrayListMultimap.<String, X509Certificate>create();
            trustedCertificates.forEach(cert -> trustedCerts.put(cert.getSubjectDN().getName(), cert));
            trustResolvers.add(new DefaultAttestationCertificateTrustResolver(resolver, trustedCerts));
        }
        trustResolvers.addAll(foundTrustResolvers.values());
        val trustResolver = new CompositeTrustResolver(trustResolvers);

        val foundAttestations = applicationContext.getBeansOfType(AttestationResolver.class, false, true);
        val attestationResolvers = new ArrayList<AttestationResolver>();
        attestationResolvers.add(StandardMetadataService.createDefaultAttestationResolver(trustResolver));

        if (resource != null) {
            val metadata = MAPPER.readValue(resource.getInputStream(), MetadataObject.class);
            attestationResolvers.add(new SimpleAttestationResolver(CollectionUtils.wrapList(metadata), trustResolver));
        }
        attestationResolvers.addAll(foundAttestations.values());
        val attestationResolver = new CompositeAttestationResolver(attestationResolvers);

        return new StandardMetadataService(attestationResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnOperations")
    public WebAuthnOperations webAuthnOperations() throws Exception {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();

        val appId = new AppId(StringUtils.defaultString(webAuthn.getApplicationId(), casProperties.getServer().getName()));
        val defaultRelyingPartyId = RelyingPartyIdentity
            .builder()
            .id(StringUtils.defaultString(webAuthn.getRelyingPartyId(), casProperties.getServer().getName()))
            .name(StringUtils.defaultString(webAuthn.getRelyingPartyName(), casProperties.getServer().getName()))
            .build();

        val origins = new LinkedHashSet<String>();
        if (StringUtils.isNotBlank(webAuthn.getAllowedOrigins())) {
            origins.addAll(org.springframework.util.StringUtils.commaDelimitedListToSet(webAuthn.getAllowedOrigins()));
        } else {
            origins.add(casProperties.getServer().getName());
        }

        val conveyance = AttestationConveyancePreference.valueOf(webAuthn.getAttestationConveyancePreference().toUpperCase());
        val relyingParty = RelyingParty.builder()
            .identity(defaultRelyingPartyId)
            .credentialRepository(webAuthnCredentialRepository())
            .origins(origins)
            .attestationConveyancePreference(conveyance)
            .metadataService(webAuthnMetadataService())
            .allowUnrequestedExtensions(webAuthn.isAllowUnrequestedExtensions())
            .allowUntrustedAttestation(webAuthn.isAllowUntrustedAttestation())
            .validateSignatureCounter(webAuthn.isValidateSignatureCounter())
            .appId(appId)
            .build();

        return new WebAuthnOperations(webAuthnCredentialRepository(),
            newCache(),
            newCache(),
            newCache(),
            relyingParty,
            webAuthnMetadataService());
    }

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
}
