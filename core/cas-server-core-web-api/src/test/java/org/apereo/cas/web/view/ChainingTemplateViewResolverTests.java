package org.apereo.cas.web.view;

import org.junit.Test;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingTemplateViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ChainingTemplateViewResolverTests {
    @Test
    public void verifyAction() {
        final var r = new ChainingTemplateViewResolver();
        final var resolver = new StringTemplateResolver();
        resolver.setCheckExistence(true);
        r.addResolver(resolver);
        r.initialize();
        final var res = r.resolveTemplate(mock(IEngineConfiguration.class), "cas",
            "template", new LinkedHashMap<>());
        assertNotNull(res);
    }
}
