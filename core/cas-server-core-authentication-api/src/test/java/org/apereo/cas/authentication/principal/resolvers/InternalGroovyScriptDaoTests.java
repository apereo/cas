package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyPrincipalAttributesProperties;

import lombok.val;
import org.apereo.services.persondir.support.GroovyPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InternalGroovyScriptDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class InternalGroovyScriptDaoTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyAction() throws Throwable {
        val groovy = new GroovyPrincipalAttributesProperties();
        groovy.setLocation(new ClassPathResource("/GroovyAttributeDao.groovy"));
        val d = new GroovyPersonAttributeDao(new InternalGroovyScriptDao(applicationContext, casProperties, groovy));
        val queryAttributes = new HashMap<String, Object>();
        queryAttributes.put("username", "casuser");

        val results = d.getPeople(queryAttributes);
        assertFalse(results.isEmpty());
    }
}
