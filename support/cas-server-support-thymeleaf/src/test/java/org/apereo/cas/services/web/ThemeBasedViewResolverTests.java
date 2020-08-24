package org.apereo.cas.services.web;

import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
import org.thymeleaf.context.WebEngineContext;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ThemeBasedViewResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Web")
public class ThemeBasedViewResolverTests {
    @Test
    public void verifyFailsOperation() {
        val themeResolver = mock(ThemeResolver.class);
        when(themeResolver.resolveThemeName(any())).thenThrow(new RuntimeException());
        val viewResolver = new ThemeBasedViewResolver(themeResolver, mock(ThemeViewResolverFactory.class));
        assertNull(viewResolver.resolveViewName("viewName", Locale.getDefault()));
    }

}
