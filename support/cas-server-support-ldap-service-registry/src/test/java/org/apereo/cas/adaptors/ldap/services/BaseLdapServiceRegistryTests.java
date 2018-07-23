package org.apereo.cas.adaptors.ldap.services;

import org.apereo.cas.adaptors.ldap.services.config.LdapServiceRegistryConfiguration;
import org.apereo.cas.category.LdapCategory;
import org.apereo.cas.services.AbstractServiceRegistryTests;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceRegistry;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is {@link BaseLdapServiceRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@EnableScheduling
@DirtiesContext
@RunWith(Parameterized.class)
@Category(LdapCategory.class)
@SpringBootTest(classes = {LdapServiceRegistryConfiguration.class, RefreshAutoConfiguration.class})
public abstract class BaseLdapServiceRegistryTests extends AbstractServiceRegistryTests {

    @Autowired
    @Qualifier("ldapServiceRegistry")
    private ServiceRegistry dao;

    public BaseLdapServiceRegistryTests(final Class<? extends RegisteredService> registeredServiceClass) {
        super(registeredServiceClass);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(RegexRegisteredService.class, OAuthRegisteredService.class, SamlRegisteredService.class);
    }

    @Override
    public ServiceRegistry getNewServiceRegistry() {
        return this.dao;
    }
}
