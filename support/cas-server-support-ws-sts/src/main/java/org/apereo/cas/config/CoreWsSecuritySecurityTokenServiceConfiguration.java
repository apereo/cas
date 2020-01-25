package org.apereo.cas.config;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
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
import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactory;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.ws.idp.WSFederationConstants;

import lombok.SneakyThrows;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import java.util.ArrayList;
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
@Configuration("coreWsSecuritySecurityTokenServiceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = "classpath:jaxws-realms.xml")
public class CoreWsSecuritySecurityTokenServiceConfiguration {

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ObjectProvider<ExpirationPolicyBuilder> grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private ObjectProvider<AuthenticationServiceSelectionStrategy> wsFederationAuthenticationServiceSelectionStrategy;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @ConditionalOnMissingBean(name = "cxfServlet")
    @Bean
    public ServletRegistrationBean cxfServlet() {
        val bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("cxfServletSecurityTokenService");
        bean.setServlet(new CXFServlet());
        bean.setUrlMappings(CollectionUtils.wrap(WSFederationConstants.ENDPOINT_STS.concat("*")));
        bean.setAsyncSupported(true);
        return bean;
    }

    @ConditionalOnMissingBean(name = "loggerListener")
    @Bean
    public EventMapper loggerListener() {
        return new EventMapper(new MapEventLogger());
    }

    @ConditionalOnMissingBean(name = "delegationHandlers")
    @RefreshScope
    @Bean
    public List<TokenDelegationHandler> delegationHandlers() {
        val handlers = new ArrayList<TokenDelegationHandler>(2);
        handlers.add(new SAMLDelegationHandler());
        handlers.add(new X509TokenDelegationHandler());
        return handlers;
    }

    @ConditionalOnMissingBean(name = "transportSTSProviderBean")
    @Bean
    @SneakyThrows
    public SecurityTokenServiceProvider transportSTSProviderBean() {
        val provider = new SecurityTokenServiceProvider();
        provider.setIssueOperation(transportIssueDelegate());
        provider.setValidateOperation(transportValidateDelegate());
        return provider;
    }

    @ConditionalOnMissingBean(name = "wrappingSecurityTokenServiceClaimsHandler")
    @Bean
    public ClaimsHandler wrappingSecurityTokenServiceClaimsHandler() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
        return new WrappingSecurityTokenServiceClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer());
    }

    @ConditionalOnMissingBean(name = "nonWSFederationClaimsClaimsHandler")
    @Bean
    public ClaimsHandler nonWSFederationClaimsClaimsHandler() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
        return new NonWSFederationClaimsClaimsHandler(idp.getRealmName(), wsfed.getRealm().getIssuer());
    }

    @ConditionalOnMissingBean(name = "customNamespaceWSFederationClaimsClaimsHandler")
    @Bean
    public ClaimsHandler customNamespaceWSFederationClaimsClaimsHandler() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
        return new CustomNamespaceWSFederationClaimsClaimsHandler(idp.getRealmName(),
            wsfed.getRealm().getIssuer(), wsfed.getCustomClaims());
    }

    @ConditionalOnMissingBean(name = "wsfedClaimsHandlers")
    @Bean
    public List<ClaimsHandler> wsfedClaimsHandlers() {
        return CollectionUtils.wrapList(
            wrappingSecurityTokenServiceClaimsHandler(),
            nonWSFederationClaimsClaimsHandler(),
            customNamespaceWSFederationClaimsClaimsHandler());
    }

    @ConditionalOnMissingBean(name = "wsfedClaimsManager")
    @Bean
    public ClaimsManager wsfedClaimsManager() {
        val claimsManager = new ClaimsManager();
        claimsManager.setClaimHandlers(wsfedClaimsHandlers());
        return claimsManager;
    }

    @ConditionalOnMissingBean(name = "transportIssueDelegate")
    @Bean
    public IssueOperation transportIssueDelegate() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val op = new TokenIssueOperation();
        op.setTokenProviders(transportTokenProviders());
        op.setServices(CollectionUtils.wrap(transportService()));
        op.setStsProperties(transportSTSProperties());
        op.setClaimsManager(wsfedClaimsManager());
        op.setTokenValidators(transportTokenValidators());
        op.setEventListener(loggerListener());
        op.setDelegationHandlers(delegationHandlers());
        op.setEncryptIssuedToken(wsfed.isEncryptTokens());
        op.setTokenStore(securityTokenServiceTokenStore());
        return op;
    }

    @ConditionalOnMissingBean(name = "securityTokenServiceTokenStore")
    @Bean
    public TokenStore securityTokenServiceTokenStore() {
        return new MemoryTokenStore();
    }

    @ConditionalOnMissingBean(name = "transportValidateDelegate")
    @Bean
    public ValidateOperation transportValidateDelegate() {
        val op = new TokenValidateOperation();
        op.setTokenValidators(transportTokenValidators());
        op.setStsProperties(transportSTSProperties());
        op.setEventListener(loggerListener());
        return op;
    }

    @ConditionalOnMissingBean(name = "transportTokenValidators")
    @RefreshScope
    @Bean
    public List transportTokenValidators() {
        val list = new ArrayList<Object>(4);
        list.add(transportSamlTokenValidator());
        list.add(transportJwtTokenValidator());
        list.add(transportSecureContextTokenValidator());
        list.add(transportX509TokenValidator());
        return list;
    }

    @ConditionalOnMissingBean(name = "transportTokenProviders")
    @RefreshScope
    @Bean
    public List transportTokenProviders() {
        val list = new ArrayList<Object>(3);
        list.add(transportSamlTokenProvider());
        list.add(transportJwtTokenProvider());
        list.add(transportSecureContextTokenProvider());
        return list;
    }

    @ConditionalOnMissingBean(name = "casRealm")
    @Bean
    public RealmProperties casRealm() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val realmConfig = wsfed.getRealm();
        val realm = new RealmProperties();
        val issuer = realmConfig.getIssuer();
        if (StringUtils.isBlank(issuer)) {
            throw new BeanCreationException("Realm issuer for the secure token service cannot be undefined");
        }
        realm.setIssuer(issuer);
        if (StringUtils.isBlank(realmConfig.getKeystoreFile())
            || StringUtils.isBlank(realmConfig.getKeyPassword())
            || StringUtils.isBlank(realmConfig.getKeystoreAlias())) {
            throw new BeanCreationException("Keystore file, password or alias assigned to the realm must be defined");
        }

        val p = CryptoUtils.getSecurityProperties(realmConfig.getKeystoreFile(),
            realmConfig.getKeystorePassword(), realmConfig.getKeystoreAlias());
        realm.setSignatureCryptoProperties(p);
        realm.setCallbackHandler(new RealmPasswordVerificationCallbackHandler(realmConfig.getKeyPassword()));
        return realm;
    }


    @RefreshScope
    @Bean
    @ConditionalOnMissingBean(name = "securityTokenServiceRealms")
    public Map<String, RealmProperties> securityTokenServiceRealms() {
        val idp = casProperties.getAuthn().getWsfedIdp().getIdp();
        val realms = new HashMap<String, RealmProperties>();
        realms.put(idp.getRealmName(), casRealm());
        return realms;
    }

    @ConditionalOnMissingBean(name = "transportSecureContextTokenProvider")
    @Bean
    public SCTProvider transportSecureContextTokenProvider() {
        return new SCTProvider();
    }

    @ConditionalOnMissingBean(name = "transportJwtTokenProvider")
    @Bean
    public JWTTokenProvider transportJwtTokenProvider() {
        val provider = new JWTTokenProvider();
        provider.setRealmMap(securityTokenServiceRealms());
        provider.setSignToken(true);
        return provider;
    }

    @ConditionalOnMissingBean(name = "transportSamlTokenProvider")
    @Bean
    public SAMLTokenProvider transportSamlTokenProvider() {
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
        provider.setRealmMap(securityTokenServiceRealms());
        provider.setConditionsProvider(c);
        provider.setSubjectProvider(s);
        provider.setSignToken(wsfed.isSignTokens());
        return provider;
    }

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

    @ConditionalOnMissingBean(name = "transportUsernameTokenValidator")
    @Bean
    public Validator transportUsernameTokenValidator() {
        return new CipheredCredentialsValidator(securityTokenServiceCredentialCipherExecutor());
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
    public STSPropertiesMBean transportSTSProperties() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val idp = casProperties.getAuthn().getWsfedIdp().getIdp();

        val s = new StaticSTSProperties();
        s.setIssuer(getClass().getSimpleName());
        s.setRealmParser(new UriRealmParser(securityTokenServiceRealms()));
        s.setSignatureCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getSigningKeystoreFile(), wsfed.getSigningKeystorePassword()));
        s.setEncryptionCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getEncryptionKeystoreFile(), wsfed.getEncryptionKeystorePassword()));

        val rel = new Relationship();
        rel.setType(Relationship.FED_TYPE_IDENTITY);
        rel.setSourceRealm(idp.getRealmName());
        rel.setTargetRealm(idp.getRealmName());

        s.setRelationships(CollectionUtils.wrap(rel));
        return s;
    }

    @ConditionalOnMissingBean(name = "securityTokenServiceClientBuilder")
    @Bean
    public SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder() {
        return new SecurityTokenServiceClientBuilder(casProperties.getAuthn().getWsfedIdp(),
            casProperties.getServer().getPrefix());
    }

    @ConditionalOnMissingBean(name = "securityTokenServiceTokenFetcher")
    @Bean
    @RefreshScope
    public SecurityTokenServiceTokenFetcher securityTokenServiceTokenFetcher() {
        return new SecurityTokenServiceTokenFetcher(servicesManager.getObject(),
            wsFederationAuthenticationServiceSelectionStrategy.getObject(),
            securityTokenServiceCredentialCipherExecutor(),
            securityTokenServiceClientBuilder());
    }

    @ConditionalOnMissingBean(name = "securityTokenServiceCredentialCipherExecutor")
    @RefreshScope
    @Bean
    public CipherExecutor securityTokenServiceCredentialCipherExecutor() {
        val wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        val crypto = wsfed.getCrypto();
        return CipherExecutorUtils.newStringCipherExecutor(crypto, SecurityTokenServiceCredentialCipherExecutor.class);
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketFactory")
    @Bean
    @RefreshScope
    public SecurityTokenTicketFactory securityTokenTicketFactory() {
        return new DefaultSecurityTokenTicketFactory(securityTokenTicketIdGenerator(), grantingTicketExpirationPolicy.getObject());
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator securityTokenTicketIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

}
