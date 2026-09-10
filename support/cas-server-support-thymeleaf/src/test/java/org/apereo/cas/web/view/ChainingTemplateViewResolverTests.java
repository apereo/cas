package org.apereo.cas.web.view;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ChainingTemplateViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Web")
class ChainingTemplateViewResolverTests {

    private static ChainingTemplateViewResolver chainOver(final boolean delegateCacheable) {
        val chain = new ChainingTemplateViewResolver();
        val delegate = new StringTemplateResolver();
        delegate.setCheckExistence(true);
        delegate.setCacheable(delegateCacheable);
        chain.addResolver(delegate);
        chain.initialize();
        return chain;
    }

    @Test
    void verifyAction() {
        val res = chainOver(true).resolveTemplate(mock(IEngineConfiguration.class), "cas",
            "template", new LinkedHashMap<>());
        assertNotNull(res);
    }

    @Test
    void verifyCacheableDelegateResolutionStaysCacheable() {
        val res = chainOver(true).resolveTemplate(mock(IEngineConfiguration.class), "cas",
            "template", new LinkedHashMap<>());
        assertNotNull(res);
        assertTrue(res.getValidity().isCacheable());
    }

    @Test
    void verifyNonCacheableDelegateResolutionStaysNonCacheable() {
        val res = chainOver(false).resolveTemplate(mock(IEngineConfiguration.class), "cas",
            "template", new LinkedHashMap<>());
        assertNotNull(res);
        assertFalse(res.getValidity().isCacheable());
    }

    @Test
    void verifyNoDelegateResolvesToNothing() {
        val chain = new ChainingTemplateViewResolver();
        chain.initialize();
        assertNull(chain.resolveTemplate(mock(IEngineConfiguration.class), "cas",
            "template", new LinkedHashMap<>()));
    }
}
