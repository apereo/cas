package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.sts.STSPropertiesMBean;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.claims.ClaimsAttributeStatementProvider;
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
import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.sts.token.realm.Relationship;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.sts.token.validator.TokenValidator;
import org.apache.cxf.sts.token.validator.X509TokenValidator;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apache.cxf.ws.security.sts.provider.operation.IssueOperation;
import org.apache.cxf.ws.security.sts.provider.operation.ValidateOperation;
import org.apache.wss4j.dom.validate.Validator;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.SecurityTokenServiceAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.SecurityTokenServiceClientBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.claims.WrappingSecurityTokenServiceClaimsHandler;
import org.apereo.cas.support.realm.RealmPasswordVerificationCallbackHandler;
import org.apereo.cas.support.realm.UriRealmParser;
import org.apereo.cas.support.util.CryptoUtils;
import org.apereo.cas.support.validation.CipheredCredentialsValidator;
import org.apereo.cas.support.validation.SecurityTokenServiceCredentialCipherExecutor;
import org.apereo.cas.support.x509.X509TokenDelegationHandler;
import org.apereo.cas.ticket.DefaultSecurityTokenTicketFactory;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.SecurityTokenTicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;
import org.apereo.cas.ws.idp.WSFederationConstants;
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

import javax.xml.ws.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfiguration}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@Configuration("coreWsSecuritySecurityTokenServiceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = {"classpath:jaxws-realms.xml", "classpath:META-INF/cxf/cxf.xml"})
public class CoreWsSecuritySecurityTokenServiceConfiguration {

    @Autowired
    @Qualifier("grantingTicketExpirationPolicy")
    private ExpirationPolicy grantingTicketExpirationPolicy;

    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ServletRegistrationBean cxfServlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("cxfServletSecurityTokenService");
        bean.setServlet(new CXFServlet());
        bean.setUrlMappings(CollectionUtils.wrap(WSFederationConstants.ENDPOINT_STS.concat("*")));
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean
    public EventMapper loggerListener() {
        return new EventMapper(new MapEventLogger());
    }

    @RefreshScope
    @Bean
    public List<TokenDelegationHandler> delegationHandlers() {
        final List<TokenDelegationHandler> handlers = new ArrayList<>();
        handlers.add(new SAMLDelegationHandler());
        handlers.add(new X509TokenDelegationHandler());
        return handlers;
    }

    @RefreshScope
    @Bean
    public Provider transportSTSProviderBean() {
        try {
            final SecurityTokenServiceProvider provider = new SecurityTokenServiceProvider();
            provider.setIssueOperation(transportIssueDelegate());
            provider.setValidateOperation(transportValidateDelegate());
            return provider;
        } catch (final Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    @RefreshScope
    @Bean
    public IssueOperation transportIssueDelegate() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        final WsFederationProperties.IdentityProvider idp = casProperties.getAuthn().getWsfedIdp().getIdp();

        final ClaimsManager claimsManager = new ClaimsManager();
        claimsManager.setClaimHandlers(CollectionUtils.wrap(new WrappingSecurityTokenServiceClaimsHandler(
            idp.getRealmName(),
            wsfed.getRealm().getIssuer())));

        final TokenIssueOperation op = new TokenIssueOperation();
        op.setTokenProviders(transportTokenProviders());
        op.setServices(CollectionUtils.wrap(transportService()));
        op.setStsProperties(transportSTSProperties());
        op.setClaimsManager(claimsManager);
        op.setTokenValidators(transportTokenValidators());
        op.setEventListener(loggerListener());
        op.setDelegationHandlers(delegationHandlers());
        op.setEncryptIssuedToken(wsfed.isEncryptTokens());

        return op;
    }

    @RefreshScope
    @Bean
    public ValidateOperation transportValidateDelegate() {
        final TokenValidateOperation op = new TokenValidateOperation();
        op.setTokenValidators(transportTokenValidators());
        op.setStsProperties(transportSTSProperties());
        op.setEventListener(loggerListener());
        return op;
    }

    @RefreshScope
    @Bean
    public List transportTokenValidators() {
        final List list = new ArrayList<>();
        list.add(transportSamlTokenValidator());
        list.add(new X509TokenValidator());
        return list;
    }

    @RefreshScope
    @Bean
    public List transportTokenProviders() {
        final List list = new ArrayList<>();
        list.add(transportSamlTokenProvider());
        return list;
    }

    @RefreshScope
    @Bean
    public RealmProperties casRealm() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        final RealmProperties realm = new RealmProperties();
        realm.setIssuer(wsfed.getRealm().getIssuer());
        if (StringUtils.isBlank(wsfed.getRealm().getKeystoreFile())
            || StringUtils.isBlank(wsfed.getRealm().getKeyPassword())
            || StringUtils.isBlank(wsfed.getRealm().getKeystoreAlias())) {
            throw new BeanCreationException("Keystore file, password or alias assigned to the realm must be defined");
        }

        final Properties p = CryptoUtils.getSecurityProperties(wsfed.getRealm().getKeystoreFile(),
            wsfed.getRealm().getKeystorePassword(), wsfed.getRealm().getKeystoreAlias());
        realm.setSignatureCryptoProperties(p);
        realm.setCallbackHandler(new RealmPasswordVerificationCallbackHandler(wsfed.getRealm().getKeyPassword()));
        return realm;
    }


    @RefreshScope
    @Bean
    public Map<String, RealmProperties> realms() {
        final WsFederationProperties.IdentityProvider idp = casProperties.getAuthn().getWsfedIdp().getIdp();
        final Map<String, RealmProperties> realms = new HashMap<>();
        realms.put(idp.getRealmName(), casRealm());
        return realms;
    }

    @RefreshScope
    @Bean
    public SAMLTokenProvider transportSamlTokenProvider() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdp().getSts();

        final DefaultSubjectProvider s = new DefaultSubjectProvider();
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
            case "unspecified":
            default:
                s.setSubjectNameIDFormat(NameID.UNSPECIFIED);
                break;
        }

        final DefaultConditionsProvider c = new DefaultConditionsProvider();
        c.setAcceptClientLifetime(true);

        final SAMLTokenProvider provider = new SAMLTokenProvider();
        provider.setAttributeStatementProviders(CollectionUtils.wrap(new ClaimsAttributeStatementProvider()));
        provider.setRealmMap(realms());
        provider.setConditionsProvider(c);
        provider.setSubjectProvider(s);
        return provider;
    }

    @RefreshScope

    @Bean
    public TokenValidator transportSamlTokenValidator() {
        return new SAMLTokenValidator();
    }

    @RefreshScope
    @Bean
    public Validator transportUsernameTokenValidator() {
        return new CipheredCredentialsValidator(securityTokenServiceCredentialCipherExecutor());
    }

    @RefreshScope
    @Bean
    public StaticService transportService() {
        final StaticService s = new StaticService();
        s.setEndpoints(CollectionUtils.wrap(".*"));
        return s;
    }

    @RefreshScope
    @ConditionalOnMissingBean(name = "transportSTSProperties")
    @Bean
    public STSPropertiesMBean transportSTSProperties() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        final WsFederationProperties.IdentityProvider idp = casProperties.getAuthn().getWsfedIdp().getIdp();

        final StaticSTSProperties s = new StaticSTSProperties();
        s.setIssuer(getClass().getSimpleName());
        s.setRealmParser(new UriRealmParser(realms()));
        s.setSignatureCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getSigningKeystoreFile(), wsfed.getSigningKeystorePassword()));
        s.setEncryptionCryptoProperties(CryptoUtils.getSecurityProperties(wsfed.getEncryptionKeystoreFile(), wsfed.getEncryptionKeystorePassword()));

        final Relationship rel = new Relationship();
        rel.setType(Relationship.FED_TYPE_IDENTITY);
        rel.setSourceRealm(idp.getRealmName());
        rel.setTargetRealm(idp.getRealmName());

        s.setRelationships(CollectionUtils.wrap(rel));
        return s;
    }

    @Bean
    @RefreshScope
    public SecurityTokenServiceClientBuilder securityTokenServiceClientBuilder() {
        return new SecurityTokenServiceClientBuilder(casProperties.getAuthn().getWsfedIdp(),
            casProperties.getServer().getPrefix());
    }

    @ConditionalOnMissingBean(name = "securityTokenServiceAuthenticationMetaDataPopulator")
    @Bean
    @RefreshScope
    public AuthenticationMetaDataPopulator securityTokenServiceAuthenticationMetaDataPopulator() {
        return new SecurityTokenServiceAuthenticationMetaDataPopulator(servicesManager,
            wsFederationAuthenticationServiceSelectionStrategy, securityTokenServiceCredentialCipherExecutor(),
            securityTokenServiceClientBuilder());
    }

    @RefreshScope
    @Bean
    public CipherExecutor securityTokenServiceCredentialCipherExecutor() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdp().getSts();
        return new SecurityTokenServiceCredentialCipherExecutor(wsfed.getCrypto().getEncryption().getKey(),
            wsfed.getCrypto().getSigning().getKey(),
            wsfed.getCrypto().getAlg());
    }

    @Bean
    @RefreshScope
    public SecurityTokenTicketFactory securityTokenTicketFactory() {
        return new DefaultSecurityTokenTicketFactory(securityTokenTicketIdGenerator(), grantingTicketExpirationPolicy);
    }

    @ConditionalOnMissingBean(name = "securityTokenTicketIdGenerator")
    @Bean
    @RefreshScope
    public UniqueTicketIdGenerator securityTokenTicketIdGenerator() {
        return new DefaultUniqueTicketIdGenerator();
    }

    @ConditionalOnMissingBean(name = "coreWsSecuritySecurityTokenServiceAuthenticationEventExecutionPlanConfigurer")
    @Bean
    public AuthenticationEventExecutionPlanConfigurer coreWsSecuritySecurityTokenServiceAuthenticationEventExecutionPlanConfigurer() {
        return plan -> plan.registerMetadataPopulator(securityTokenServiceAuthenticationMetaDataPopulator());
    }
}
