package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.authentication.DefaultSecurityTokenServiceTokenFetcher;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.authentication.SecurityTokenServiceTokenFetcher;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apache.cxf.ws.security.sts.provider.operation.IssueOperation;
import org.apache.cxf.ws.security.sts.provider.operation.ValidateOperation;
import org.apache.cxf.ws.security.tokenstore.MemoryTokenStore;
import org.apache.cxf.ws.security.tokenstore.TokenStore;
import org.apache.wss4j.dom.validate.Validator;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ScopedProxyMode;

import javax.net.ssl.HostnameVerifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = "classpath:jaxws-realms.xml")
@Configuration(value = "coreWsSecuritySecurityTokenServiceConfiguration", proxyBeanMethods = false)
public class CoreWsSecuritySecurityTokenServiceConfiguration {

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceDelegationConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenServiceDelegationConfiguration {
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
    public static class CoreWsSecuritySecurityTokenServiceOperationeConfiguration {
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
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager) {
            return new DefaultSecurityTokenServiceTokenFetcher(servicesManager, wsFederationAuthenticationServiceSelectionStrategy, securityTokenServiceCredentialCipherExecutor,
                securityTokenServiceClientBuilder);
        }

        @ConditionalOnMissingBean(name = "transportIssueDelegate")
        @Bean
        @Autowired
        public IssueOperation transportIssueDelegate(
            final CasConfigurationProperties casProperties,
            final List<TokenProvider> transportTokenProviders,
            @Qualifier("transportService")
            final StaticService transportService,
            @Qualifier("transportSTSProperties")
            final STSPropertiesMBean transportSTSProperties,
            @Qualifier("wsfedClaimsManager")
            final ClaimsManager wsfedClaimsManager,
            final List<TokenValidator> transportTokenValidators,
            @Qualifier("loggerListener")
            final EventMapper loggerListener,
            final List<TokenDelegationHandler> delegationHandlers,
            @Qualifier("securityTokenServiceTokenStore")
            final TokenStore securityTokenServiceTokenStore) {
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
    public static class CoreWsSecuritySecurityTokenServiceProvidersConfiguration {
        @ConditionalOnMissingBean(name = "transportSecureContextTokenProvider")
        @Bean
        public SCTProvider transportSecureContextTokenProvider() {
            return new SCTProvider();
        }

        @ConditionalOnMissingBean(name = "transportJwtTokenProvider")
        @Bean
        public JWTTokenProvider transportJwtTokenProvider(
            @Qualifier("securityTokenServiceRealms")
            final Map<String, RealmProperties> securityTokenServiceRealms) {
            val provider = new JWTTokenProvider();
            provider.setRealmMap(securityTokenServiceRealms);
            provider.setSignToken(true);
            return provider;
        }

        @ConditionalOnMissingBean(name = "transportSamlTokenProvider")
        @Bean
        @Autowired
        public SAMLTokenProvider transportSamlTokenProvider(final CasConfigurationProperties casProperties,
                                                            @Qualifier("securityTokenServiceRealms")
                                                            final Map<String, RealmProperties> securityTokenServiceRealms) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val s = new DefaultSubjectProvider();
            if (StringUtils.isNotBlank(wsfed.getSubjectNameQualifier())) {
                s.setSubjectNameQualifier(wsfed.getSubjectNameQualifier());
            }
            switch (wsfed.getSubjectNameIdFormat().trim().toLowerCase()) {
                case "email":
                    s.setSubjectNameIDFormat(NameID.EMAIL);
                    break;
                case "entity":
                    s.setSubjectNameIDFormat(NameID.ENTITY);
                    break;
                case "transient":
                    s.setSubjectNameIDFormat(NameID.TRANSIENT);
                    break;
                case "persistent":
                    s.setSubjectNameIDFormat(NameID.PERSISTENT);
                    break;
                case "unspecified":
                default:
                    s.setSubjectNameIDFormat(NameID.UNSPECIFIED);
                    break;
            }
            val c = new DefaultConditionsProvider();
            c.setAcceptClientLifetime(wsfed.isConditionsAcceptClientLifetime());
            c.setFailLifetimeExceedance(wsfed.isConditionsFailLifetimeExceedance());
            c.setFutureTimeToLive(Beans.newDuration(wsfed.getConditionsFutureTimeToLive()).toSeconds());
            c.setLifetime(Beans.newDuration(wsfed.getConditionsLifetime()).toSeconds());
            c.setMaxLifetime(Beans.newDuration(wsfed.getConditionsMaxLifetime()).toSeconds());
            val provider = new SAMLTokenProvider();
            provider.setAttributeStatementProviders(CollectionUtils.wrap(new ClaimsAttributeStatementProvider()));
            provider.setRealmMap(securityTokenServiceRealms);
            provider.setConditionsProvider(c);
            provider.setSubjectProvider(s);
            provider.setSignToken(wsfed.isSignTokens());
            return provider;
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceClientConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenServiceClientConfiguration {
        @ConditionalOnMissingBean(name = "securityTokenServiceClientBuilder")
        @Bean
        @Autowired
        public SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("hostnameVerifier")
            final HostnameVerifier hostnameVerifier,
            @Qualifier("casSslContext")
            final CasSSLContext casSslContext) {
            return new SecurityTokenServiceClientBuilder(casProperties.getAuthn().getWsfedIdp(),
                casProperties.getServer().getPrefix(), hostnameVerifier, casSslContext);
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceRealmsConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenServiceRealmsConfiguration {
        @ConditionalOnMissingBean(name = "casRealm")
        @Bean
        @Autowired
        public RealmProperties casRealm(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val realmConfig = wsfed.getRealm();
            val realm = new RealmProperties();
            val issuer = realmConfig.getIssuer();
            if (StringUtils.isBlank(issuer)) {
                throw new BeanCreationException("Realm issuer for the secure token service cannot be undefined");
            }
            realm.setIssuer(issuer);
            if (StringUtils.isBlank(realmConfig.getKeystoreFile()) || StringUtils.isBlank(realmConfig.getKeyPassword())
                || StringUtils.isBlank(realmConfig.getKeystoreAlias())) {
                throw new BeanCreationException("Keystore file, password or alias assigned to the realm must be defined");
            }
            val p = CryptoUtils.getSecurityProperties(realmConfig.getKeystoreFile(), realmConfig.getKeystorePassword(), realmConfig.getKeystoreAlias());
            realm.setSignatureCryptoProperties(p);
            realm.setCallbackHandler(new RealmPasswordVerificationCallbackHandler(realmConfig.getKeyPassword()));
            return realm;
        }

        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @ConditionalOnMissingBean(name = "securityTokenServiceRealms")
        @Autowired
        public Map<String, RealmProperties> securityTokenServiceRealms(
            final CasConfigurationProperties casProperties,
            @Qualifier("casRealm")
            final RealmProperties casRealm) {
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            val realms = new HashMap<String, RealmProperties>();
            realms.put(idp.getRealmName(), casRealm);
            return realms;
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceValidatorConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenServiceValidatorConfiguration {
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
    public static class CoreWsSecuritySecurityTokenServiceTransportConfiguration {
        @ConditionalOnMissingBean(name = "transportSTSProviderBean")
        @Bean
        public SecurityTokenServiceProvider transportSTSProviderBean(
            @Qualifier("transportIssueDelegate")
            final IssueOperation transportIssueDelegate,
            @Qualifier("transportValidateDelegate")
            final ValidateOperation transportValidateDelegate) throws Exception {
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
            @Qualifier("transportSTSProperties")
            final STSPropertiesMBean transportSTSProperties,
            @Qualifier("loggerListener")
            final EventMapper loggerListener) {
            val op = new TokenValidateOperation();
            op.setTokenValidators(transportTokenValidators);
            op.setStsProperties(transportSTSProperties);
            op.setEventListener(loggerListener);
            return op;
        }

        @ConditionalOnMissingBean(name = "securityTokenServiceCredentialCipherExecutor")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        @Bean
        @Autowired
        public CipherExecutor securityTokenServiceCredentialCipherExecutor(final CasConfigurationProperties casProperties) {
            val crypto = casProperties.getAuthn().getWsfedIdp().getSts().getCrypto();
            return CipherExecutorUtils.newStringCipherExecutor(crypto, SecurityTokenServiceCredentialCipherExecutor.class);
        }


        @ConditionalOnMissingBean(name = "transportUsernameTokenValidator")
        @Bean
        public Validator transportUsernameTokenValidator(
            @Qualifier("securityTokenServiceCredentialCipherExecutor")
            final CipherExecutor securityTokenServiceCredentialCipherExecutor) {
            return new CipheredCredentialsValidator(securityTokenServiceCredentialCipherExecutor);
        }

        @ConditionalOnMissingBean(name = "transportService")
        @Bean
        public StaticService transportService() {
            val s = new StaticService();
            s.setEndpoints(CollectionUtils.wrap(".*"));
            return s;
        }

        @ConditionalOnMissingBean(name = "transportSTSProperties")
        @Bean
        @Autowired
        public STSPropertiesMBean transportSTSProperties(
            final CasConfigurationProperties casProperties,
            @Qualifier("securityTokenServiceRealms")
            final Map<String, RealmProperties> securityTokenServiceRealms) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            val s = new StaticSTSProperties();
            s.setIssuer(getClass().getSimpleName());
            s.setRealmParser(new UriRealmParser(securityTokenServiceRealms));
            s.setSignatureCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getSigningKeystoreFile(), wsfed.getSigningKeystorePassword()));
            s.setEncryptionCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getEncryptionKeystoreFile(), wsfed.getEncryptionKeystorePassword()));
            val rel = new Relationship();
            rel.setType(Relationship.FED_TYPE_IDENTITY);
            rel.setSourceRealm(idp.getRealmName());
            rel.setTargetRealm(idp.getRealmName());
            s.setRelationships(CollectionUtils.wrap(rel));
            return s;
        }
    }

    @Configuration(value = "CoreWsSecuritySecurityTokenServiceWebConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CoreWsSecuritySecurityTokenServiceWebConfiguration {
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
    public static class CoreWsSecuritySecurityTokenServiceClaimsConfiguration {
        @ConditionalOnMissingBean(name = "wrappingSecurityTokenServiceClaimsHandler")
        @Bean
        @Autowired
        public ClaimsHandler wrappingSecurityTokenServiceClaimsHandler(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            return new WrappingSecurityTokenServiceClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer());
        }

        @ConditionalOnMissingBean(name = "nonWSFederationClaimsClaimsHandler")
        @Bean
        @Autowired
        public ClaimsHandler nonWSFederationClaimsClaimsHandler(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            return new NonWSFederationClaimsClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer());
        }

        @ConditionalOnMissingBean(name = "customNamespaceWSFederationClaimsClaimsHandler")
        @Bean
        @Autowired
        public ClaimsHandler customNamespaceWSFederationClaimsClaimsHandler(final CasConfigurationProperties casProperties) {
            val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
            val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
            return new CustomNamespaceWSFederationClaimsClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer(), wsfed.getCustomClaims());
        }

        @ConditionalOnMissingBean(name = "wsfedClaimsHandlers")
        @Bean
        public List<ClaimsHandler> wsfedClaimsHandlers(
            @Qualifier("wrappingSecurityTokenServiceClaimsHandler")
            final ClaimsHandler wrappingSecurityTokenServiceClaimsHandler,
            @Qualifier("nonWSFederationClaimsClaimsHandler")
            final ClaimsHandler nonWSFederationClaimsClaimsHandler,
            @Qualifier("customNamespaceWSFederationClaimsClaimsHandler")
            final ClaimsHandler customNamespaceWSFederationClaimsClaimsHandler) {
            return CollectionUtils.wrapList(wrappingSecurityTokenServiceClaimsHandler, nonWSFederationClaimsClaimsHandler, customNamespaceWSFederationClaimsClaimsHandler);
        }

        @ConditionalOnMissingBean(name = "wsfedClaimsManager")
        @Bean
        public ClaimsManager wsfedClaimsManager(
            @Qualifier("wsfedClaimsHandlers")
            final List<ClaimsHandler> wsfedClaimsHandlers) {
            val claimsManager = new ClaimsManager();
            claimsManager.setClaimHandlers(wsfedClaimsHandlers);
            return claimsManager;
        }

    }
}
