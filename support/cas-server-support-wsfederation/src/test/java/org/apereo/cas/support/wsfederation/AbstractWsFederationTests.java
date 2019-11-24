package org.apereo.cas.support.wsfederation;

import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.wsfederation.config.WsFederationAuthenticationConfiguration;
import org.apereo.cas.support.wsfederation.config.support.authentication.WsFedAuthenticationEventExecutionPlanConfiguration;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;

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

import java.util.Collection;

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
public class AbstractWsFederationTests extends AbstractOpenSamlTests {
    @Autowired
    @Qualifier("wsFederationConfigurations")
    protected Collection<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier("wsFederationCookieManager")
    protected WsFederationCookieManager wsFederationCookieManager;

    @Autowired
    @Qualifier("wsFederationHelper")
    protected WsFederationHelper wsFederationHelper;

    @ImportAutoConfiguration({
        RefreshAutoConfiguration.class,
        MailSenderAutoConfiguration.class,
        AopAutoConfiguration.class
    })
    @SpringBootConfiguration
    @Import({
        WsFederationAuthenticationConfiguration.class,
        WsFedAuthenticationEventExecutionPlanConfiguration.class,
        AbstractOpenSamlTests.SharedTestConfiguration.class
    })
    public interface SharedTestConfiguration {
    }
}

