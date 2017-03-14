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
import org.apache.cxf.sts.service.EncryptionProperties;
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
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationPostProcessor;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.SecurityTokenServiceAuthenticationPostProcessor;
import org.apereo.cas.config.support.authentication.AuthenticationEventExecutionPlanConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.wsfed.WsFederationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.claims.WrappingSecurityTokenServiceClaimsHandler;
import org.apereo.cas.support.realm.RealmVerificationCallbackHandler;
import org.apereo.cas.support.realm.UriRealmParser;
import org.apereo.cas.support.saml.SamlRealmCodec;
import org.apereo.cas.support.validation.CipheredCredentialsValidator;
import org.apereo.cas.support.validation.SecurityTokenServiceCredentialCipherExecutor;
import org.apereo.cas.support.x509.X509TokenDelegationHandler;
import org.apereo.cas.ws.idp.IdentityProviderConfigurationService;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.xml.ws.Provider;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link CoreWsSecuritySecurityTokenServiceConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("coreWsSecuritySecurityTokenServiceConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportResource(locations = {"classpath:jaxws-realms.xml", "classpath:META-INF/cxf/cxf.xml"})
public class CoreWsSecuritySecurityTokenServiceConfiguration implements AuthenticationEventExecutionPlanConfigurer {

    @Autowired
    @Qualifier("wsFederationAuthenticationServiceSelectionStrategy")
    private AuthenticationServiceSelectionStrategy wsFederationAuthenticationServiceSelectionStrategy;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("idpConfigService")
    private IdentityProviderConfigurationService idpConfigService;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ServletRegistrationBean cxfServlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("cxfServletSecurityTokenService");
        bean.setServlet(new CXFServlet());
        bean.setUrlMappings(Collections.singleton("/ws/sts/*"));
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean
    public EventMapper loggerListener() {
        return new EventMapper(new MapEventLogger());
    }

    @Bean
    public List<TokenDelegationHandler> delegationHandlers() {
        return Arrays.asList(new SAMLDelegationHandler(), new X509TokenDelegationHandler());
    }

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

    @Bean
    public IssueOperation transportIssueDelegate() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdP().getSts();
        final WsFederationProperties.IdentityProvider idp = casProperties.getAuthn().getWsfedIdP().getIdp();

        final ClaimsManager claimsManager = new ClaimsManager();
        claimsManager.setClaimHandlers(Arrays.asList(new WrappingSecurityTokenServiceClaimsHandler(
                idp.getRealmUri(),
                wsfed.getRealm().getIssuer())));

        final TokenIssueOperation op = new TokenIssueOperation();
        op.setTokenProviders(transportTokenProviders());
        op.setServices(transportServices());
        op.setStsProperties(transportSTSProperties());
        op.setClaimsManager(claimsManager);
        op.setTokenValidators(transportTokenValidators());
        op.setEventListener(loggerListener());
        op.setDelegationHandlers(delegationHandlers());
        op.setEncryptIssuedToken(wsfed.isEncryptTokens());

        return op;
    }

    @Bean
    public ValidateOperation transportValidateDelegate() {
        final TokenValidateOperation op = new TokenValidateOperation();
        op.setTokenValidators(transportTokenValidators());
        op.setStsProperties(transportSTSProperties());
        op.setEventListener(loggerListener());
        return op;
    }

    @Bean
    public List<Relationship> relationships() {
        final Relationship rel1 = new Relationship();
        rel1.setSourceRealm("REALMA");
        rel1.setTargetRealm("REALMB");
        rel1.setType("FederatedIdentity");
        return Arrays.asList(rel1);
    }

    @Bean
    public List transportTokenValidators() {
        return Arrays.asList(transportSamlTokenValidator(), new X509TokenValidator());
    }

    @Bean
    public List transportTokenProviders() {
        return Arrays.asList(transportSamlTokenProvider());
    }

    @Bean
    public RealmProperties realmA() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdP().getSts();
        final WsFederationProperties.IdentityProvider idp = casProperties.getAuthn().getWsfedIdP().getIdp();

        final RealmProperties realm = new RealmProperties();
        realm.setIssuer(wsfed.getRealm().getIssuer());
        final Properties p = getSecurityProperties(wsfed.getRealm().getKeystoreFile(), wsfed.getRealm().getKeystorePassword(),
                wsfed.getRealm().getKeystoreAlias());
        realm.setSignatureCryptoProperties(p);
        realm.setCallbackHandler(new RealmVerificationCallbackHandler(idp.getRealmUri()));
        return realm;
    }

    @Bean
    public Map<String, RealmProperties> realms() {
        final Map<String, RealmProperties> realms = new HashMap<>();
        realms.put("REALMA", realmA());
        return realms;
    }

    @Bean
    public SAMLTokenProvider transportSamlTokenProvider() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdP().getSts();

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
        c.setLifetime(wsfed.getTokenLifetime());
        c.setAcceptClientLifetime(true);

        final SAMLTokenProvider provider = new SAMLTokenProvider();
        provider.setAttributeStatementProviders(Arrays.asList(new ClaimsAttributeStatementProvider()));
        provider.setRealmMap(realms());
        provider.setConditionsProvider(c);
        provider.setSubjectProvider(s);
        return provider;
    }

    @Bean
    public TokenValidator transportSamlTokenValidator() {
        final SAMLTokenValidator v = new SAMLTokenValidator();
        v.setSamlRealmCodec(new SamlRealmCodec());
        return v;
    }

    @Bean
    public Validator transportUsernameTokenValidator() {
        return new CipheredCredentialsValidator(securityTokenServiceCredentialCipherExecutor());
    }

    @Bean
    public List transportServices() {
        return Arrays.asList(myEncryptionService(), transportService());
    }

    @Bean
    public StaticService transportService() {
        final StaticService s = new StaticService();
        s.setEndpoints(Arrays.asList(".*"));
        return s;
    }

    @Bean
    public StaticService myEncryptionService() {
        final StaticService s = new StaticService();
        s.setEndpoints(Arrays.asList("myServiceB.*"));

        final EncryptionProperties encryptionProperties = new EncryptionProperties();
        encryptionProperties.setEncryptionName("serviceB");
        encryptionProperties.setEncryptionAlgorithm("http://www.w3.org/2001/04/xmlenc#aes128-cbc");
        s.setEncryptionProperties(encryptionProperties);

        return s;
    }

    @ConditionalOnMissingBean(name = "transportSTSProperties")
    @Bean
    public STSPropertiesMBean transportSTSProperties() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdP().getSts();

        final StaticSTSProperties s = new StaticSTSProperties();
        s.setIssuer(getClass().getSimpleName());
        s.setRealmParser(new UriRealmParser(realms()));
        s.setSignatureCryptoProperties(getSecurityProperties(wsfed.getSigningKeystoreFile(), wsfed.getSigningKeystorePassword()));
        s.setEncryptionCryptoProperties(getSecurityProperties(wsfed.getEncryptionKeystoreFile(), wsfed.getEncryptionKeystorePassword()));
        s.setRelationships(relationships());
        return s;
    }

    private Properties getSecurityProperties(final String file, final String psw) {
        return getSecurityProperties(file, psw, null);
    }

    private Properties getSecurityProperties(final String file, final String psw, final String alias) {
        Properties p = new Properties();
        p.put("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        p.put("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        p.put("org.apache.ws.security.crypto.merlin.keystore.password", psw);
        p.put("org.apache.ws.security.crypto.merlin.keystore.file", file);

        if (StringUtils.isNotBlank(alias)) {
            p.put("org.apache.ws.security.crypto.merlin.keystore.alias", alias);
        }
        return p;
    }

    @ConditionalOnMissingBean(name = "securityTokenServiceAuthenticationPostProcessor")
    @Bean
    public AuthenticationPostProcessor securityTokenServiceAuthenticationPostProcessor() {
        return new SecurityTokenServiceAuthenticationPostProcessor(servicesManager, idpConfigService,
                wsFederationAuthenticationServiceSelectionStrategy, securityTokenServiceCredentialCipherExecutor());
    }

    @Bean
    public CipherExecutor securityTokenServiceCredentialCipherExecutor() {
        final WsFederationProperties.SecurityTokenService wsfed = casProperties.getAuthn().getWsfedIdP().getSts();
        return new SecurityTokenServiceCredentialCipherExecutor(wsfed.getEncryptionKey(), wsfed.getSigningKey());
    }

    @Override
    public void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
        plan.registerAuthenticationPostProcessor(securityTokenServiceAuthenticationPostProcessor());
    }
}
