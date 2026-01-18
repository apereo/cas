package org.apereo.cas.persondir;

import module java.base;
import org.apereo.cas.config.CasCoreScriptingAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyPrincipalAttributesProperties;
import org.apereo.cas.persondir.groovy.GroovyPersonAttributeDao;
import org.apereo.cas.persondir.groovy.InternalGroovyScriptDao;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GroovyPersonAttributeDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    CasCoreScriptingAutoConfiguration.class
})
@ExtendWith(CasTestExtension.class)
@SpringBootTestAutoConfigurations
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyPersonAttributeDaoTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() {
        val groovy = new GroovyPrincipalAttributesProperties();
        groovy.setLocation(new ClassPathResource("/GroovyAttributeDao.groovy"));
        val dao = new GroovyPersonAttributeDao(new InternalGroovyScriptDao(applicationContext, casProperties, groovy));
        val queryAttributes = new HashMap<String, Object>();
        queryAttributes.put("username", "casuser");

        val results = dao.getPeople(queryAttributes);
        assertFalse(results.isEmpty());
        val personAttributes = results.stream().findFirst().orElseThrow();
        assertEquals("casuser", personAttributes.getName());
        val attributes = personAttributes.getAttributes();
        assertTrue(attributes.containsKey("givenName"));
        assertTrue(attributes.containsKey("lastName"));
        assertTrue(attributes.containsKey("name"));
        assertTrue(attributes.containsKey("nickname"));
        assertTrue(attributes.containsKey("username"));

    }
}
