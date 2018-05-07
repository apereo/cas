package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link InternalGroovyScriptDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = "cas.authn.attributeRepository.groovy[0].location=classpath:GroovyAttributeDao.groovy")
public class InternalGroovyScriptDaoTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyAction() {
        final var d = new InternalGroovyScriptDao(new StaticApplicationContext(), casProperties);
        final Map results = d.getAttributesForUser("casuser");
        assertFalse(results.isEmpty());
        assertFalse(d.getPersonAttributesFromMultivaluedAttributes(results).isEmpty());
    }
}
