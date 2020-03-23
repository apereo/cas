package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.val;
import org.apereo.services.persondir.support.GroovyPersonAttributeDao;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InternalGroovyScriptDaoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Groovy")
@SpringBootTest(classes = RefreshAutoConfiguration.class,
    properties = "cas.authn.attributeRepository.groovy[0].location=classpath:GroovyAttributeDao.groovy")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class InternalGroovyScriptDaoTests {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    public void verifyAction() {
        val d = new GroovyPersonAttributeDao(new InternalGroovyScriptDao(applicationContext, casProperties));
        val queryAttributes = new HashMap<String, Object>();
        queryAttributes.put("username", "casuser");

        val results = d.getPeople(queryAttributes);
        assertFalse(results.isEmpty());
    }
}
