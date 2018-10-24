package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.*;

/**
 * This is {@link GroovyPrincipalFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = RefreshAutoConfiguration.class)
public class GroovyPrincipalFactoryTests {
    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Test
    public void verifyAction() {
        val factory = PrincipalFactoryUtils.newGroovyPrincipalFactory(new ClassPathResource("PrincipalFactory.groovy"));
        val p = factory.createPrincipal("casuser", CollectionUtils.wrap("name", "CAS"));
        assertEquals("casuser", p.getId());
        assertEquals(1, p.getAttributes().size());
    }
}
