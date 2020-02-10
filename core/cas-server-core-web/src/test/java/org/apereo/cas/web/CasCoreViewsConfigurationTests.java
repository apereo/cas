package org.apereo.cas.web;

import org.apereo.cas.config.CasCoreViewsConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasCoreViewsConfigurationTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    ThymeleafAutoConfiguration.class,
    CasCoreViewsConfiguration.class
},
    properties = {
        "cas.view.rest.url=http://localhost:8182",
        "cas.view.template-prefixes=file:/templates"
    })
public class CasCoreViewsConfigurationTests {
    @Autowired
    @Qualifier("chainingTemplateViewResolver")
    private AbstractTemplateResolver chainingTemplateViewResolver;

    @Test
    public void verifyOperation() {
        assertNotNull(chainingTemplateViewResolver);
    }
}
