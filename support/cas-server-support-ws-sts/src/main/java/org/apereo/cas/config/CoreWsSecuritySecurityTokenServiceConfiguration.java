package org.apereo.cas.config;

import org.apache.cxf.sts.IdentityMapper;
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
import org.apache.cxf.sts.token.validator.UsernameTokenValidator;
import org.apache.cxf.sts.token.validator.X509TokenValidator;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.ClaimTypeConstants;
import org.apereo.cas.support.FileClaimsHandler;
import org.apereo.cas.support.IdentityMapperImpl;
import org.apereo.cas.support.PasswordCallbackHandler;
import org.apereo.cas.support.SamlRealmCodec;
import org.apereo.cas.support.UriRealmParser;
import org.apereo.cas.support.X509TokenDelegationHandler;
import org.opensaml.saml.saml2.core.NameID;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.xml.ws.Provider;
import java.util.ArrayList;
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
public class CoreWsSecuritySecurityTokenServiceConfiguration {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Bean
    public ServletRegistrationBean cxfServlet() {
        final ServletRegistrationBean bean = new ServletRegistrationBean();
        bean.setEnabled(true);
        bean.setName("cxfServlet");
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
    public TokenIssueOperation transportIssueDelegate() {
        final TokenIssueOperation op = new TokenIssueOperation();
        op.setTokenProviders(transportTokenProviders());
        op.setServices(transportServices());
        op.setStsProperties(transportSTSProperties());
        op.setClaimsManager(claimsManager());
        op.setTokenValidators(transportTokenValidators());
        op.setEventListener(loggerListener());
        op.setDelegationHandlers(delegationHandlers());
        op.setEncryptIssuedToken(true);
        return op;
    }

    @Bean
    public TokenValidateOperation transportValidateDelegate() {
        final TokenValidateOperation op = new TokenValidateOperation();
        op.setTokenValidators(transportTokenValidators());
        op.setStsProperties(transportSTSProperties());
        op.setEventListener(loggerListener());
        return op;
    }

    @Bean
    public List<Relationship> relationships() {
        final List<Relationship> list = new ArrayList<>();

        final Relationship rel1 = new Relationship();
        rel1.setSourceRealm("REALMA");
        rel1.setSourceRealm("REALMB");
        rel1.setIdentityMapper(identityMapper());
        rel1.setType("FederatedIdentity");
        list.add(rel1);

        final Relationship rel2 = new Relationship();
        rel2.setSourceRealm("REALMB");
        rel2.setSourceRealm("REALMA");
        rel2.setIdentityMapper(identityMapper());
        rel2.setType("FederatedIdentity");
        list.add(rel2);

        return list;
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
        final RealmProperties realm = new RealmProperties();
        realm.setIssuer("STS Realm A");

        final Properties p = new Properties();
        p.put("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        p.put("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        p.put("org.apache.ws.security.crypto.merlin.keystore.password", "storepass");
        p.put("org.apache.ws.security.crypto.merlin.keystore.alias", "realma");
        p.put("org.apache.ws.security.crypto.merlin.keystore.file", "stsrealm_a.jks");
        realm.setSignatureCryptoProperties(p);
        realm.setCallbackHandler(new PasswordCallbackHandler());
        return realm;
    }

    @Bean
    public RealmProperties realmB() {
        final RealmProperties realm = new RealmProperties();
        realm.setIssuer("STS Realm B");
        realm.setSignaturePropertiesFile("stsKeystoreB.properties");
        realm.setCallbackHandler(new PasswordCallbackHandler());
        return realm;
    }

    @Bean
    public Map realms() {
        final Map realms = new HashMap<>();
        realms.put("REALMA", realmA());
        realms.put("REALMB", realmB());
        return realms;
    }

    @Bean
    public SAMLTokenProvider transportSamlTokenProvider() {
        final SAMLTokenProvider provider = new SAMLTokenProvider();
        provider.setAttributeStatementProviders(attributeStatementProvidersList());
        provider.setRealmMap(realms());
        provider.setConditionsProvider(conditionsProvider());
        provider.setSubjectProvider(subjectProvider());
        return provider;
    }

    @Bean
    public DefaultConditionsProvider conditionsProvider() {
        final DefaultConditionsProvider c = new DefaultConditionsProvider();
        c.setLifetime(1000);
        c.setAcceptClientLifetime(true);
        return c;
    }

    @Bean
    public DefaultSubjectProvider subjectProvider() {
        final DefaultSubjectProvider s = new DefaultSubjectProvider();
        s.setSubjectNameIDFormat(NameID.UNSPECIFIED);
        return s;
    }

    @Bean
    public List attributeStatementProvidersList() {
        return Arrays.asList(claimAttributeProvider());
    }

    @Bean
    public ClaimsAttributeStatementProvider claimAttributeProvider() {
        return new ClaimsAttributeStatementProvider();
    }

    @Bean
    public ClaimsManager claimsManager() {
        final ClaimsManager m = new ClaimsManager();
        m.setClaimHandlers(claimHandlerList());
        return m;
    }

    @Bean
    public IdentityMapper identityMapper() {
        return new IdentityMapperImpl();
    }

    @Bean
    public SamlRealmCodec samlRealmCodec() {
        return new SamlRealmCodec();
    }

    @Bean
    public UriRealmParser customRealmParser() {
        final UriRealmParser p = new UriRealmParser();
        p.setRealmMap(realms());
        return p;
    }

    @Bean
    public SAMLTokenValidator transportSamlTokenValidator() {
        final SAMLTokenValidator v = new SAMLTokenValidator();
        v.setSamlRealmCodec(samlRealmCodec());
        return v;
    }

    @Bean
    public UsernameTokenValidator transportUsernameTokenValidator() {
        return new UsernameTokenValidator();
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

    @Bean
    public StaticSTSProperties transportSTSProperties() {
        final StaticSTSProperties s = new StaticSTSProperties();
        s.setCallbackHandler(new PasswordCallbackHandler());
        s.setIssuer("CAS STS");
        s.setRealmParser(customRealmParser());

        Properties p = new Properties();
        p.put("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        p.put("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        p.put("org.apache.ws.security.crypto.merlin.keystore.password", "storepass");
        p.put("org.apache.ws.security.crypto.merlin.keystore.file", "ststrust.jks");
        s.setSignatureCryptoProperties(p);

        p = new Properties();
        p.put("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
        p.put("org.apache.ws.security.crypto.merlin.keystore.type", "jks");
        p.put("org.apache.ws.security.crypto.merlin.keystore.password", "storepass");
        p.put("org.apache.ws.security.crypto.merlin.keystore.file", "stsencrypt.jks");
        s.setEncryptionCryptoProperties(p);
        
        s.setRelationships(relationships());
        return s;
    }

    @Bean
    public List claimHandlerList() {
        return Arrays.asList(claimsHandlerA(), claimsHandlerB());
    }

    @Bean
    public FileClaimsHandler claimsHandlerA() {
        final Map claimsMap = new HashMap();

        Map values = new HashMap();
        values.put(ClaimTypeConstants.CLAIMS_GIVEN_NAME, "alice");
        values.put(ClaimTypeConstants.CLAIMS_SURNAME, "smith");
        values.put(ClaimTypeConstants.CLAIMS_EMAIL_ADDRESS_2005, "aliac@somewhere.org");
        values.put(ClaimTypeConstants.CLAIMS_ROLE, "User");
        claimsMap.put("alice", values);

        values = new HashMap();
        values.put(ClaimTypeConstants.CLAIMS_GIVEN_NAME, "bob");
        values.put(ClaimTypeConstants.CLAIMS_SURNAME, "something");
        values.put(ClaimTypeConstants.CLAIMS_EMAIL_ADDRESS_2005, "bobs@somewhere.org");
        values.put(ClaimTypeConstants.CLAIMS_ROLE, "User,Manager,Admin");

        final FileClaimsHandler f = new FileClaimsHandler();
        f.setUserClaims(claimsMap);
        f.setSupportedClaims(supportedClaims());
        f.setRealm("REALMA");
        return f;
    }

    @Bean
    public FileClaimsHandler claimsHandlerB() {
        final Map claimsMap = new HashMap();

        Map values = new HashMap();
        values.put(ClaimTypeConstants.CLAIMS_GIVEN_NAME, "ALICE");
        values.put(ClaimTypeConstants.CLAIMS_SURNAME, "SMITH");
        values.put(ClaimTypeConstants.CLAIMS_EMAIL_ADDRESS_2005, "aliaS@somewhere.org");
        values.put(ClaimTypeConstants.CLAIMS_ROLE, "User");
        claimsMap.put("alice", values);

        values = new HashMap();
        values.put(ClaimTypeConstants.CLAIMS_GIVEN_NAME, "BOB");
        values.put(ClaimTypeConstants.CLAIMS_SURNAME, "SOMEONE");
        values.put(ClaimTypeConstants.CLAIMS_EMAIL_ADDRESS_2005, "bobs@somewhere.org");
        values.put(ClaimTypeConstants.CLAIMS_ROLE, "User,MANAGER,ADMIN");

        final FileClaimsHandler f = new FileClaimsHandler();
        f.setUserClaims(claimsMap);
        f.setSupportedClaims(supportedClaims());
        f.setRealm("REALMB");
        return f;
    }


    @Bean
    public List supportedClaims() {
        return ClaimTypeConstants.ALL_CLAIMS;
    }

}
