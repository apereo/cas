package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.MultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.handler.ByCredentialTypeAuthenticationHandlerResolver;
import org.apereo.cas.authentication.metadata.AuthenticationContextAttributeMetaDataPopulator;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.webauthn.WebAuthnController;
import org.apereo.cas.webauthn.WebAuthnCredential;
import org.apereo.cas.webauthn.WebAuthnMultifactorAuthenticationProvider;
import org.apereo.cas.webauthn.web.flow.WebAuthnAuthenticationHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yubico.internal.util.JacksonCodecs;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.attestation.AttestationResolver;
import com.yubico.webauthn.attestation.MetadataObject;
import com.yubico.webauthn.attestation.MetadataService;
import com.yubico.webauthn.attestation.StandardMetadataService;
import com.yubico.webauthn.attestation.TrustResolver;
import com.yubico.webauthn.attestation.resolver.CompositeAttestationResolver;
import com.yubico.webauthn.attestation.resolver.CompositeTrustResolver;
import com.yubico.webauthn.attestation.resolver.SimpleAttestationResolver;
import com.yubico.webauthn.attestation.resolver.SimpleTrustResolverWithEquality;
import com.yubico.webauthn.core.DefaultSessionManager;
import com.yubico.webauthn.core.InMemoryRegistrationStorage;
import com.yubico.webauthn.core.RegistrationStorage;
import com.yubico.webauthn.core.SessionManager;
import com.yubico.webauthn.core.WebAuthnServer;
import com.yubico.webauthn.data.AttestationConveyancePreference;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.extension.appid.AppId;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * This is {@link WebAuthnConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Configuration("webAuthnConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class WebAuthnConfiguration {
    private static final int CACHE_MAX_SIZE = 10_000;

    private static final ObjectMapper MAPPER = JacksonCodecs.json().findAndRegisterModules();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    @Qualifier("failureModeEvaluator")
    private ObjectProvider<MultifactorAuthenticationFailureModeEvaluator> failureModeEvaluator;

    @Autowired
    @Qualifier("webAuthnBypassEvaluator")
    private ObjectProvider<MultifactorAuthenticationProviderBypassEvaluator> webAuthnBypassEvaluator;

    @ConditionalOnMissingBean(name = "webAuthnController")
    @Bean
    public WebAuthnController webAuthnController() throws Exception {
        return new WebAuthnController(webAuthnServer());
    }

    @ConditionalOnMissingBean(name = "webAuthnCredentialRepository")
    @Bean
    @RefreshScope
    public RegistrationStorage webAuthnCredentialRepository() {
        return new InMemoryRegistrationStorage();
    }

    @ConditionalOnMissingBean(name = "webAuthnMultifactorAuthenticationProvider")
    @Bean
    @RefreshScope
    public MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider() {
        val u2f = casProperties.getAuthn().getMfa().getWebAuthn();
        val p = new WebAuthnMultifactorAuthenticationProvider();
        p.setBypassEvaluator(webAuthnBypassEvaluator.getObject());
        p.setFailureMode(u2f.getFailureMode());
        p.setFailureModeEvaluator(failureModeEvaluator.getObject());
        p.setOrder(u2f.getRank());
        p.setId(u2f.getId());
        return p;
    }

    @Bean
    @ConditionalOnMissingBean(name = "simpleTrustResolverWithEquality")
    public TrustResolver simpleTrustResolverWithEquality() {
        return new SimpleTrustResolverWithEquality(new ArrayList<>());
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnMetadataService")
    public MetadataService webAuthnMetadataService() throws Exception {
        val foundTrustResolvers = applicationContext.getBeansOfType(TrustResolver.class, false, true);
        val trustResolvers = new ArrayList<TrustResolver>();
        trustResolvers.add(StandardMetadataService.createDefaultTrustResolver());

        trustResolvers.addAll(foundTrustResolvers.values());
        val trustResolver = new CompositeTrustResolver(trustResolvers);

        val foundAttestations = applicationContext.getBeansOfType(AttestationResolver.class, false, true);
        val attestationResolvers = new ArrayList<AttestationResolver>();
        attestationResolvers.add(StandardMetadataService.createDefaultAttestationResolver(trustResolver));

        val resource = casProperties.getAuthn().getMfa().getWebAuthn().getTrustedDeviceMetadata().getLocation();
        if (resource != null) {
            val metadata = MAPPER.readValue(resource.getInputStream(), MetadataObject.class);
            attestationResolvers.add(new SimpleAttestationResolver(CollectionUtils.wrapList(metadata), trustResolver));
        }
        attestationResolvers.addAll(foundAttestations.values());
        val attestationResolver = new CompositeAttestationResolver(attestationResolvers);

        return new StandardMetadataService(attestationResolver);
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnSessionManager")
    public SessionManager webAuthnSessionManager() {
        return new DefaultSessionManager();
    }

    @Bean
    @ConditionalOnMissingBean(name = "webAuthnServer")
    public WebAuthnServer webAuthnServer() throws Exception {
        val webAuthn = casProperties.getAuthn().getMfa().getWebAuthn();
        val serverName = casProperties.getServer().getName();
        val appId = new AppId(StringUtils.defaultString(webAuthn.getApplicationId(), serverName));

        val defaultRelyingPartyId = RelyingPartyIdentity
            .builder()
            .id(StringUtils.defaultString(webAuthn.getRelyingPartyId(), new URL(serverName).getHost()))
            .name(StringUtils.defaultString(webAuthn.getRelyingPartyName(), "CAS"))
            .build();

        val origins = new LinkedHashSet<String>();
        if (StringUtils.isNotBlank(webAuthn.getAllowedOrigins())) {
            origins.addAll(org.springframework.util.StringUtils.commaDelimitedListToSet(webAuthn.getAllowedOrigins()));
        } else {
            origins.add(serverName);
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

        val server = new WebAuthnServer(webAuthnCredentialRepository(),
            newCache(), newCache(), relyingParty, webAuthnSessionManager());
        return server;
    }

    @ConditionalOnMissingBean(name = "webAuthnPrincipalFactory")
    @Bean
    @RefreshScope
    public PrincipalFactory webAuthnPrincipalFactory() {
        return PrincipalFactoryUtils.newPrincipalFactory();
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "webAuthnAuthenticationHandler")
    public AuthenticationHandler webAuthnAuthenticationHandler() {
        val webAuthn = this.casProperties.getAuthn().getMfa().getWebAuthn();
        return new WebAuthnAuthenticationHandler(webAuthn.getName(),
            servicesManager.getObject(), webAuthnPrincipalFactory(),
            webAuthnCredentialRepository(), webAuthnSessionManager(),
            webAuthn.getOrder());
    }

    @Bean
    @RefreshScope
    @ConditionalOnMissingBean(name = "webAuthnAuthenticationMetaDataPopulator")
    public AuthenticationMetaDataPopulator webAuthnAuthenticationMetaDataPopulator() {
        val authenticationContextAttribute = casProperties.getAuthn().getMfa().getAuthenticationContextAttribute();
        return new AuthenticationContextAttributeMetaDataPopulator(
            authenticationContextAttribute,
            webAuthnAuthenticationHandler(),
            webAuthnMultifactorAuthenticationProvider().getId()
        );
    }

    @ConditionalOnMissingBean(name = "webAuthnAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer webAuthnAuthenticationEventExecutionPlanConfigurer() {
        return plan -> {
            plan.registerAuthenticationHandler(webAuthnAuthenticationHandler());
            plan.registerAuthenticationMetadataPopulator(webAuthnAuthenticationMetaDataPopulator());
            plan.registerAuthenticationHandlerResolver(new ByCredentialTypeAuthenticationHandlerResolver(WebAuthnCredential.class));
        };
    }

    private static <K, V> Cache<K, V> newCache() {
        return CacheBuilder.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .expireAfterAccess(Duration.ofMinutes(5))
            .build();
    }
}
