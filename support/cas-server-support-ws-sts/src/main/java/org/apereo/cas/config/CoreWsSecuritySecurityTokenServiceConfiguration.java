package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.DefaultSecurityTokenServiceTokenFetcher;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcher;
import org.apereo.cas.authentication.WSFederationAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.claims.CustomNamespaceWSFederationClaimsClaimsHandler;
import org.apereo.cas.support.claims.NonWSFederationClaimsClaimsHandler;
import org.apereo.cas.support.claims.WrappingSecurityTokenServiceClaimsHandler;
import org.apereo.cas.support.realm.RealmPasswordVerificationCallbackHandler;
import org.apereo.cas.support.realm.UriRealmParser;
import org.apereo.cas.support.util.CryptoUtils;
import org.apereo.cas.support.validation.CipheredCredentialsValidator;
import org.apereo.cas.support.validation.SecurityTokenServiceCredentialCipherExecutor;
import org.apereo.cas.support.x509.X509TokenDelegationHandler;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;
import org.apereo.cas.ws.idp.WSFederationConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.claims.ClaimsAttributeStatementProvider;
import org.apache.cxf.sts.claims.ClaimsHandler;
import org.apache.cxf.sts.claims.ClaimsManager;
import org.apache.cxf.sts.event.map.EventMapper;
import org.apache.cxf.sts.event.map.MapEventLogger;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.operation.TokenValidateOperation;
import org.apache.cxf.sts.service.StaticService;
import org.apache.cxf.sts.token.delegation.SAMLDelegationHandler;
import org.apache.cxf.sts.token.delegation.TokenDelegationHandler;
import org.apache.cxf.sts.token.provider.DefaultConditionsProvider;
import org.apache.cxf.sts.token.provider.DefaultSubjectProvider;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.provider.SCTProvider;
import org.apache.cxf.sts.token.provider.TokenProvider;
import org.apache.cxf.sts.token.provider.jwt.JWTTokenProvider;
import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.sts.token.realm.Relationship;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.sts.token.validator.SCTValidator;
import org.apache.cxf.sts.token.validator.TokenValidator;
import org.apache.cxf.sts.token.validator.X509TokenValidator;
import org.apache.cxf.sts.token.validator.jwt.JWTTokenValidator;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.policy.PolicyInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.HttpsTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.IssuedTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.SamlTokenInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.WSSecurityInterceptorProvider;
import org.apache.cxf.ws.security.policy.interceptors.WSSecurityPolicyInterceptorProvider;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apache.cxf.ws.security.sts.provider.operation.IssueOperation;
import org.apache.cxf.ws.security.sts.provider.operation.ValidateOperation;
import org.apache.cxf.ws.security.tokenstore.MemoryTokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.wss4j.dom.validate.Validator;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ScopedProxyMode;
import jakarta.xml.ws.WebServiceContext;
import javax.net.ssl.HostnameVerifier;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.WsFederationIdentityProvider)
@ImportResource(locations = "classpath:jaxws-realms.xml")
@Configuration(value = "CoreWsSecuritySecurityTokenServiceConfiguration", proxyBeanMethods = false)
@Slf4j
class CoreWsSecuritySecurityTokenServiceConfiguration {

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceDelegationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceDelegationConfiguration {
        @Bean
        @ConditionalOnMissingBean(name = "samlTokenDelegationHandler")
        public TokenDelegationHandler samlTokenDelegationHandler() {
            return new SAMLDelegationHandler();
        }

        @Bean
        @ConditionalOnMissingBean(name = "x509TokenDelegationHandler")
        public TokenDelegationHandler x509TokenDelegationHandler() {
            return new X509TokenDelegationHandler();
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceOperationeConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceOperationeConfiguration {
        @ConditionalOnMissingBean(name = "securityTokenServiceTokenFetcher")
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher(
            @Qualifier("securityTokenServiceCredentialCipherExecutor")
            final CipherExecutor securityTokenServiceCredentialCipherExecutor,
            @Qualifier("securityTokenServiceClientBuilder")
            final SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder,
            @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
            final AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
            return new DefaultSecurityTokenServiceTokenFetcher(servicesManager, wsFederationAuthenticationServiceSelectionStrategy,
                securityTokenServiceCredentialCipherExecutor, securityTokenServiceClientBuilder);
        }

        @ConditionalOnMissingBean(name = "transportIssueDelegate")
        @Bean
        public IssueOperation transportIssueDelegate(
            final CasConfigurationProperties casProperties,
            final List<TokenProvider> transportTokenProviders,
            @Qualifier("transportService") final StaticService transportService,
            @Qualifier("transportSTSProperties") final STSPropertiesMBean transportSTSProperties,
            @Qualifier("wsfedClaimsManager") final ClaimsManager wsfedClaimsManager,
            final List<TokenValidator> transportTokenValidators,
            @Qualifier("loggerListener") final EventMapper loggerListener,
            final List<TokenDelegationHandler> delegationHandlers,
            @Qualifier("securityTokenServiceTokenStore") final TokenStore securityTokenServiceTokenStore) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val op = new TokenIssueOperation();
            op.setTokenProviders(transportTokenProviders);
            op.setServices(CollectionUtils.wrap(transportService));
            op.setStsProperties(transportSTSProperties);
            op.setClaimsManager(wsfedClaimsManager);
            op.setTokenValidators(transportTokenValidators);
            op.setEventListener(loggerListener);
            op.setDelegationHandlers(delegationHandlers);
            op.setEncryptIssuedToken(wsfed.isEncryptTokens());
            op.setTokenStore(securityTokenServiceTokenStore);
            return op;
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceProvidersConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceProvidersConfiguration {
        @ConditionalOnMissingBean(name = "transportSecureContextTokenProvider")
        @Bean
        public SCTProvider transportSecureContextTokenProvider() {
            return new SCTProvider();
        }

        @ConditionalOnMissingBean(name = "transportJwtTokenProvider")
        @Bean
        public JWTTokenProvider transportJwtTokenProvider(
            @Qualifier("securityTokenServiceRealms") final Map<String, RealmProperties> securityTokenServiceRealms) {
            val provider = new JWTTokenProvider();
            provider.setRealmMap(securityTokenServiceRealms);
            provider.setSignToken(true);
            return provider;
        }

        @ConditionalOnMissingBean(name = "transportSamlTokenProvider")
        @Bean
        public SAMLTokenProvider transportSamlTokenProvider(final CasConfigurationProperties casProperties,
                                                            @Qualifier("securityTokenServiceRealms") final Map<String, RealmProperties> securityTokenServiceRealms) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val subProvider = new DefaultSubjectProvider();

            FunctionUtils.doIfNotBlank(wsfed.getSubjectNameQualifier(),
                __ -> subProvider.setSubjectNameQualifier(wsfed.getSubjectNameQualifier()));
            switch (wsfed.getSubjectNameIdFormat().trim().toLowerCase(Locale.ENGLISH)) {
                case "email" -> subProvider.setSubjectNameIDFormat(NameIDType.EMAIL);
                case "entity" -> subProvider.setSubjectNameIDFormat(NameIDType.ENTITY);
                case "transient" -> subProvider.setSubjectNameIDFormat(NameIDType.TRANSIENT);
                case "persistent" -> subProvider.setSubjectNameIDFormat(NameIDType.PERSISTENT);
                default -> subProvider.setSubjectNameIDFormat(NameIDType.UNSPECIFIED);
            }
            val condProvider = new DefaultConditionsProvider();
            condProvider.setAcceptClientLifetime(wsfed.isConditionsAcceptClientLifetime());
            condProvider.setFailLifetimeExceedance(wsfed.isConditionsFailLifetimeExceedance());
            condProvider.setFutureTimeToLive(Beans.newDuration(wsfed.getConditionsFutureTimeToLive()).toSeconds());
            condProvider.setLifetime(Beans.newDuration(wsfed.getConditionsLifetime()).toSeconds());
            condProvider.setMaxLifetime(Beans.newDuration(wsfed.getConditionsMaxLifetime()).toSeconds());
            val provider = new SAMLTokenProvider();
            provider.setAttributeStatementProviders(CollectionUtils.wrap(new ClaimsAttributeStatementProvider()));
            provider.setRealmMap(securityTokenServiceRealms);
            provider.setConditionsProvider(condProvider);
            provider.setSubjectProvider(subProvider);
            provider.setSignToken(wsfed.isSignTokens());
            return provider;
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceClientConfiguration {
        @ConditionalOnMissingBean(name = "securityTokenServiceClientBuilder")
        @Bean
        public SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("hostnameVerifier") final HostnameVerifier hostnameVerifier,
            @Qualifier(CasSSLContext.BEAN_NAME) final CasSSLContext casSslContext) {
            return new SecurityTokenServiceClientBuilder(casProperties.getAuthn().getWsfedIdp(),
                casProperties.getServer().getPrefix(), hostnameVerifier, casSslContext);
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceRealmsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceRealmsConfiguration {
        @ConditionalOnMissingBean(name = "casRealm")
        @Bean
        public RealmProperties casRealm(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val realmConfig = wsfed.getRealm();
            val realm = new RealmProperties();
            realm.setIssuer(StringUtils.defaultIfBlank(realmConfig.getIssuer(), casProperties.getServer().getPrefix()));
            if (StringUtils.isBlank(realmConfig.getKeystoreFile())
                || StringUtils.isBlank(realmConfig.getKeystorePassword())
                || StringUtils.isBlank(realmConfig.getKeyPassword())
                || StringUtils.isBlank(realmConfig.getKeystoreAlias())) {
                LOGGER.warn("Keystore file, password or alias assigned to the realm are undefined");
            } else {
                val properties = CryptoUtils.getSecurityProperties(realmConfig.getKeystoreFile(), realmConfig.getKeystorePassword(), realmConfig.getKeystoreAlias());
                realm.setSignatureCryptoProperties(properties);
                realm.setCallbackHandler(new RealmPasswordVerificationCallbackHandler(realmConfig.getKeyPassword().toCharArray()));
            }
            return realm;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "securityTokenServiceRealms")
        public Map<String, RealmProperties> securityTokenServiceRealms(
            final CasConfigurationProperties casProperties,
            @Qualifier("casRealm") final RealmProperties casRealm) {
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            val realms = new HashMap<String, RealmProperties>();
            realms.put(idp.getRealmName(), casRealm);
            return realms;
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceValidatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceValidatorConfiguration {
        @ConditionalOnMissingBean(name = "transportSamlTokenValidator")
        @Bean
        public TokenValidator transportSamlTokenValidator() {
            return new SAMLTokenValidator();
        }

        @ConditionalOnMissingBean(name = "transportJwtTokenValidator")
        @Bean
        public TokenValidator transportJwtTokenValidator() {
            return new JWTTokenValidator();
        }

        @ConditionalOnMissingBean(name = "transportSecureContextTokenValidator")
        @Bean
        public TokenValidator transportSecureContextTokenValidator() {
            return new SCTValidator();
        }

        @ConditionalOnMissingBean(name = "transportX509TokenValidator")
        @Bean
        public TokenValidator transportX509TokenValidator() {
            return new X509TokenValidator();
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceTransportConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceTransportConfiguration {
        @Bean
        public WebServiceContext transportSTSWebServiceContext() {
            return new WebServiceContextImpl();
        }

        @ConditionalOnMissingBean(name = "transportSTSProviderBean")
        @Bean
        public SecurityTokenServiceProvider transportSTSProviderBean(
            @Qualifier("transportIssueDelegate") final IssueOperation transportIssueDelegate,
            @Qualifier("transportValidateDelegate") final ValidateOperation transportValidateDelegate) throws Exception {
            val provider = new SecurityTokenServiceProvider();
            provider.setIssueOperation(transportIssueDelegate);
            provider.setValidateOperation(transportValidateDelegate);
            return provider;
        }

        @ConditionalOnMissingBean(name = "securityTokenServiceTokenStore")
        @Bean
        public TokenStore securityTokenServiceTokenStore() {
            return new MemoryTokenStore();
        }

        @ConditionalOnMissingBean(name = "transportValidateDelegate")
        @Bean
        public ValidateOperation transportValidateDelegate(
            final List<TokenValidator> transportTokenValidators,
            @Qualifier("transportSTSProperties") final STSPropertiesMBean transportSTSProperties,
            @Qualifier("loggerListener") final EventMapper loggerListener) {
            val validateOperation = new TokenValidateOperation();
            validateOperation.setTokenValidators(transportTokenValidators);
            validateOperation.setStsProperties(transportSTSProperties);
            validateOperation.setEventListener(loggerListener);
            return validateOperation;
        }

        @ConditionalOnMissingBean(name = "securityTokenServiceCredentialCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        public CipherExecutor securityTokenServiceCredentialCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getWsfedIdp().getSts().getCrypto();
            return CipherExecutorUtils.newStringCipherExecutor(crypto, SecurityTokenServiceCredentialCipherExecutor.class);
        }


        @ConditionalOnMissingBean(name = "transportUsernameTokenValidator")
        @Bean
        public Validator transportUsernameTokenValidator(
            @Qualifier("securityTokenServiceCredentialCipherExecutor") final CipherExecutor securityTokenServiceCredentialCipherExecutor) {
            return new CipheredCredentialsValidator(securityTokenServiceCredentialCipherExecutor);
        }

        @ConditionalOnMissingBean(name = "transportService")
        @Bean
        public StaticService transportService() {
            val staticService = new StaticService();
            staticService.setEndpoints(CollectionUtils.wrap(".*"));
            return staticService;
        }

        @ConditionalOnMissingBean(name = "transportSTSProperties")
        @Bean
        public STSPropertiesMBean transportSTSProperties(
            final CasConfigurationProperties casProperties,
            @Qualifier("securityTokenServiceRealms") final Map<String, RealmProperties> securityTokenServiceRealms) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            val stsProperties = new StaticSTSProperties();
            stsProperties.setIssuer(getClass().getSimpleName());
            stsProperties.setRealmParser(new UriRealmParser(securityTokenServiceRealms));
            stsProperties.setSignatureCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getSigningKeystoreFile(), wsfed.getSigningKeystorePassword()));
            stsProperties.setEncryptionCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getEncryptionKeystoreFile(), wsfed.getEncryptionKeystorePassword()));
            val rel = new Relationship();
            rel.setType(Relationship.FED_TYPE_IDENTITY);
            rel.setSourceRealm(idp.getRealmName());
            rel.setTargetRealm(idp.getRealmName());
            stsProperties.setRelationships(CollectionUtils.wrap(rel));
            return stsProperties;
        }

        @Bean
        public PolicyInterceptorProvider wsSecurityPolicyInterceptorProvider() {
            return new WSSecurityPolicyInterceptorProvider();
        }

        @Bean
        public PolicyInterceptorProvider wsSecurityInterceptorProvider() {
            return new WSSecurityInterceptorProvider();
        }

        @Bean
        public PolicyInterceptorProvider wsIssuedTokenInterceptorProvider() {
            return new IssuedTokenInterceptorProvider();
        }

        @Bean
        public PolicyInterceptorProvider wsHttpsTokenInterceptorProvider() {
            return new HttpsTokenInterceptorProvider();
        }

        @Bean
        public PolicyInterceptorProvider wsSamlTokenInterceptorProvider() {
            return new SamlTokenInterceptorProvider();
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceWebConfiguration {
        @ConditionalOnMissingBean(name = "cxfServlet")
        @Bean
        public ServletRegistrationBean<CXFServlet> cxfServlet() {
            val bean = new ServletRegistrationBean();
            bean.setEnabled(true);
            bean.setName("cxfServletSecurityTokenService");
            bean.setServlet(new CXFServlet());
            bean.setUrlMappings(CollectionUtils.wrap(WSFederationConstants.BASE_ENDPOINT_STS.concat("*")));
            bean.setAsyncSupported(true);
            return bean;
        }

        @ConditionalOnMissingBean(name = "loggerListener")
        @Bean
        public EventMapper loggerListener() {
            return new EventMapper(new MapEventLogger());
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceClaimsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceClaimsConfiguration {
        @ConditionalOnMissingBean(name = "wrappingSecurityTokenServiceClaimsHandler")
        @Bean
        public ClaimsHandler wrappingSecurityTokenServiceClaimsHandler(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            return new WrappingSecurityTokenServiceClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer());
        }

        @ConditionalOnMissingBean(name = "nonWSFederationClaimsClaimsHandler")
        @Bean
        public ClaimsHandler nonWSFederationClaimsClaimsHandler(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            return new NonWSFederationClaimsClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer());
        }

        @ConditionalOnMissingBean(name = "customNamespaceWSFederationClaimsClaimsHandler")
        @Bean
        public ClaimsHandler customNamespaceWSFederationClaimsClaimsHandler(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            return new CustomNamespaceWSFederationClaimsClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer(), wsfed.getCustomClaims());
        }

        @ConditionalOnMissingBean(name = "wsfedClaimsHandlers")
        @Bean
        public List<ClaimsHandler> wsfedClaimsHandlers(
            @Qualifier("wrappingSecurityTokenServiceClaimsHandler") final ClaimsHandler wrappingSecurityTokenServiceClaimsHandler,
            @Qualifier("nonWSFederationClaimsClaimsHandler") final ClaimsHandler nonWSFederationClaimsClaimsHandler,
            @Qualifier("customNamespaceWSFederationClaimsClaimsHandler") final ClaimsHandler customNamespaceWSFederationClaimsClaimsHandler) {
            return CollectionUtils.wrapList(wrappingSecurityTokenServiceClaimsHandler, nonWSFederationClaimsClaimsHandler, customNamespaceWSFederationClaimsClaimsHandler);
        }

        @ConditionalOnMissingBean(name = "wsfedClaimsManager")
        @Bean
        public ClaimsManager wsfedClaimsManager(
            @Qualifier("wsfedClaimsHandlers") final List<ClaimsHandler> wsfedClaimsHandlers) {
            val claimsManager = new ClaimsManager();
            claimsManager.setClaimHandlers(wsfedClaimsHandlers);
            return claimsManager;
        }

    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceServiceSelectionConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class CoreWsSecuritySecurityTokenServiceServiceSelectionConfiguration {
        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @ConditionalOnMissingBean(name = "wsFederationAuthenticationServiceSelectionStrategy")
        public AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy(
            @Qualifier(WebApplicationService.BEAN_NAME_FACTORY)
            final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new WSFederationAuthenticationServiceSelectionStrategy(servicesManager, webApplicationServiceFactory);
        }
    }
}
