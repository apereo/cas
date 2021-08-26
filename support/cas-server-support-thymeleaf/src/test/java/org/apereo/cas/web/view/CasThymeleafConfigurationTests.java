package org.apereo.cas.web.view;

import org.apereo.cas.BaseThymeleafTests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasThymeleafConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = BaseThymeleafTests.SharedTestConfiguration.class,
    properties = {
        "cas.view.rest.url=http://localhost:8182",
        "cas.view.template-prefixes=classpath:templates,file:/templates"
    })
@Tag("Web")
public class CasThymeleafConfigurationTests {
    @Autowired
    @Qualifier("chainingTemplateViewResolver")
    private AbstractTemplateResolver chainingTemplateViewResolver;

    @Test
    public void verifyOperation() {
        assertNotNull(chainingTemplateViewResolver);
    }
}
