package org.apereo.cas.support.wsfederation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.support.wsfederation.config.WsFederationAuthenticationComponentSerializationConfiguration;
import org.apereo.cas.support.wsfederation.config.WsFederationAuthenticationConfiguration;
import org.apereo.cas.support.wsfederation.config.support.authentication.WsFedAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class, provides resources to run wsfed tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@SpringBootTest(classes = AbstractWsFederationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed[0].identityProviderUrl=https://adfs.example.com/adfs/ls/",
        "cas.authn.wsfed[0].identityProviderIdentifier=http://adfs.example.com/adfs/services/trust",
        "cas.authn.wsfed[0].relyingPartyIdentifier=urn:federation:cas",
        "cas.authn.wsfed[0].attributesType=WSFED",
        "cas.authn.wsfed[0].signingCertificateResources=classpath:adfs-signing.crt",
        "cas.authn.wsfed[0].identityAttribute=upn",
        "cas.authn.wsfed[0].attributeResolverEnabled=true",
        "cas.authn.wsfed[0].autoRedirect=false",
        "cas.authn.wsfed[0].name=Test ADFS1"
    })
@ContextConfiguration(locations = "classpath:/applicationContext.xml")
public abstract class AbstractWsFederationTests extends AbstractOpenSamlTests {
    protected static final String ISSUER = "http://adfs.example.com/adfs/services/trust";

    protected static final String AUDIENCE = "urn:federation:cas";

    @Autowired
    @Qualifier("wsFederationConfigurations")
    protected Collection<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier("wsFederationCookieManager")
    protected WsFederationCookieManager wsFederationCookieManager;

    @Autowired
    @Qualifier("wsFederationHelper")
    protected WsFederationHelper wsFederationHelper;

    public static WsFederationCredential getCredential() {
        val standardCred = new WsFederationCredential();
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setIssuer(ISSUER);
        standardCred.setAudience(AUDIENCE);
        standardCred.setRetrievedOn(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        standardCred.setRetrievedOn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(1));

        val attributes = new HashMap<>(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());
        attributes.put("upn", List.of("cas@example.org"));
        
        standardCred.setAttributes(attributes);
        return standardCred;
    }

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        WsFederationAuthenticationConfiguration.class,
        WsFederationAuthenticationComponentSerializationConfiguration.class,
        WsFedAuthenticationEventExecutionPlanConfiguration.class,
        AbstractOpenSamlTests.SharedTestConfiguration.class
    })
    public static class SharedTestConfiguration {
    }

}

