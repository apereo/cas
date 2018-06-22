package org.apereo.cas.adaptors.ldap.services;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.ldap.services.config.LdapServiceRegistryConfiguration;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.junit.ConditionalIgnoreRule;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link BaseLdapServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableScheduling
@DirtiesContext
@Slf4j
@Category(LdapCategory.class)
@SpringBootTest(classes = {LdapServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
public class BaseLdapServiceRegistryTests extends AbstractServiceRegistryTests {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    @Autowired
    @Qualifier("ldapServiceRegistry")
    private ServiceRegistry dao;

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }

    @Test
    public void verifySamlService() {
        final SamlRegisteredService r = new SamlRegisteredService();
        r.setName("verifySamlService");
        r.setServiceId("Testing");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        final Map fmt = new HashMap();
        fmt.put("key", "value");
        r.setAttributeNameFormats(fmt);
        r.setMetadataCriteriaDirection("INCLUDE");
        r.setMetadataCriteriaRemoveEmptyEntitiesDescriptors(true);
        r.setMetadataSignatureLocation("location");
        r.setRequiredAuthenticationContextClass("Testing");
        final SamlRegisteredService r2 = (SamlRegisteredService) this.dao.save(r);
        assertEquals(r, r2);
    }

    @Test
    public void verifyOAuthServices() {
        final OAuthRegisteredService r = new OAuthRegisteredService();
        r.setName("test1456");
        r.setServiceId("testId");
        r.setTheme("theme");
        r.setDescription("description");
        r.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        r.setClientId("testoauthservice");
        r.setClientSecret("anothertest");
        r.setBypassApprovalPrompt(true);
        final RegisteredService r2 = this.dao.save(r);
        assertEquals(r, r2);
    }
}
