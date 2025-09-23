package org.apereo.cas.support.wsfederation;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasWsFederationAuthenticationAutoConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredential;
import org.apereo.cas.support.wsfederation.web.WsFederationCookieManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.beans.BeanContainer;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class, provides resources to run wsfed tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = AbstractWsFederationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.wsfed[0].identity-provider-url=https://adfs.example.com/adfs/ls/",
        "cas.authn.wsfed[0].identity-provider-identifier=http://(iam-dev-windows.unicon.net|adfs.example.com)/adfs/services/trust",
        "cas.authn.wsfed[0].relying-party-identifier=urn:federation:cas",
        "cas.authn.wsfed[0].attributes-type=WSFED",
        "cas.authn.wsfed[0].signing-certificate-resources=classpath:adfs-signing.crt",
        "cas.authn.wsfed[0].identity-attribute=upn",
        "cas.authn.wsfed[0].attribute-resolver-enabled=true",
        "cas.authn.wsfed[0].auto-redirect=false",
        "cas.authn.wsfed[0].name=Test ADFS1",
        "cas.authn.wsfed[0].encryption-private-key=classpath:adfs-enc-private.key",
        "cas.authn.wsfed[0].encryption-certificate=classpath:adfs-enc-certificate.crt"
    })
public abstract class AbstractWsFederationTests extends AbstractOpenSamlTests {
    protected static final String ISSUER = "http://adfs.example.com/adfs/services/trust";

    protected static final String AUDIENCE = "urn:federation:cas";

    @Autowired
    @Qualifier("wsFederationConfigurations")
    protected BeanContainer<WsFederationConfiguration> wsFederationConfigurations;

    @Autowired
    @Qualifier("wsFederationCookieManager")
    protected WsFederationCookieManager wsFederationCookieManager;

    @Autowired
    @Qualifier("wsFederationHelper")
    protected WsFederationHelper wsFederationHelper;

    public static WsFederationCredential getCredential() {
        val attributes = new HashMap<>(CoreAuthenticationTestUtils.getAttributeRepository().getBackingMap());
        attributes.put("upn", List.of("cas@example.org"));
        return getCredential(attributes);
    }

    public static WsFederationCredential getCredential(final Map<String, List<Object>> attributes) {
        val standardCred = new WsFederationCredential();
        standardCred.setNotBefore(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setNotOnOrAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(1));
        standardCred.setIssuedOn(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setIssuer(ISSUER);
        standardCred.setAudience(AUDIENCE);
        standardCred.setRetrievedOn(ZonedDateTime.now(ZoneOffset.UTC));
        standardCred.setId("_6257b2bf-7361-4081-ae1f-ec58d4310f61");
        standardCred.setRetrievedOn(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(1));
        standardCred.setAttributes(attributes);
        return standardCred;
    }

    @SpringBootTestAutoConfigurations
    @ImportAutoConfiguration(CasWsFederationAuthenticationAutoConfiguration.class)
    @SpringBootConfiguration(proxyBeanMethods = false)
    @Import(AbstractOpenSamlTests.SharedTestConfiguration.class)
    public static class SharedTestConfiguration {
    }

}

