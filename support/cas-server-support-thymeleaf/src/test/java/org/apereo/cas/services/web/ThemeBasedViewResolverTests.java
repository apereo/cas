package org.apereo.cas.services.web;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.ThemeResolver;

import java.util.Locale;

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
