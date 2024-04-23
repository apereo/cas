package org.apereo.cas.web.view;

import org.apereo.cas.BaseThymeleafTests;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractTemplateResolver;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
class CasThymeleafConfigurationTests {
    @Autowired
    @Qualifier("chainingTemplateViewResolver")
    private AbstractTemplateResolver chainingTemplateViewResolver;

    /**
     * Make sure there are 7 template view resolvers.
     * Two for each template prefix, one for rest url,and one for a theme folder under templates in the
     * classpath and one for templates in thymeleaf/templates.
     */
    @Test
    void verifyOperation() throws Throwable {
        assertNotNull(chainingTemplateViewResolver);
        assertEquals(7, ((ChainingTemplateViewResolver) chainingTemplateViewResolver).getResolvers().size());
        assertNotNull(chainingTemplateViewResolver.resolveTemplate(mock(IEngineConfiguration.class), null, "testTemplate", new HashMap<>()));
    }
}
