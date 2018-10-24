package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.config.support.EnvironmentConversionServiceInitializer;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * This is {@link InternalGroovyScriptDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ContextConfiguration(initializers = EnvironmentConversionServiceInitializer.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@TestPropertySource(properties = "cas.authn.attributeRepository.groovy[0].location=classpath:GroovyAttributeDao.groovy")
public class InternalGroovyScriptDaoTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private CasConfigurationProperties casProperties;

    @Test
    public void verifyAction() {
        val d = new InternalGroovyScriptDao(new StaticApplicationContext(), casProperties);
        val results = d.getAttributesForUser("casuser");
        assertFalse(results.isEmpty());
        assertFalse(d.getPersonAttributesFromMultivaluedAttributes(new HashMap(results)).isEmpty());
    }
}
