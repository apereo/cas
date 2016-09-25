package org.apereo.cas.support.wsfederation;

import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.support.saml.AbstractOpenSamlTests;
import org.apereo.cas.support.wsfederation.config.WsFederationAuthenticationConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Abstract class, provides resources to run wsfed tests.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@RunWith(SpringRunner.class)
@SpringApplicationConfiguration(locations = {"classpath:/applicationContext.xml"},
        classes = {WsFederationAuthenticationConfiguration.class, CasCoreAuthenticationConfiguration.class,
                CasCoreServicesConfiguration.class, CasCoreUtilConfiguration.class})
public class AbstractWsFederationTests extends AbstractOpenSamlTests {

    @Autowired
    protected WsFederationHelper wsFederationHelper;
}

