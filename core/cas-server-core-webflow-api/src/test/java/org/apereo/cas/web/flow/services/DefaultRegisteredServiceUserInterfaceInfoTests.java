package org.apereo.cas.web.flow.services;

import org.apereo.cas.services.RegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultRegisteredServiceUserInterfaceInfoTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class DefaultRegisteredServiceUserInterfaceInfoTests {
    @Test
    public void verifyOperation() {
        val info = new DefaultRegisteredServiceUserInterfaceInfo(mock(RegisteredService.class)) {
            private static final long serialVersionUID = 2331519665722637762L;

            @Override
            public Collection<String> getDescriptions() {
                return List.of("Description");
            }

            @Override
            public Collection<String> getDisplayNames() {
                return List.of("DisplayNames");
            }

            @Override
            public Collection<String> getInformationURLs() {
                return List.of("https://apereo.org/cas");
            }

            @Override
            public Collection<Logo> getLogoUrls() {
                return List.of(new Logo("https://logo.url", 32, 32));
            }

            @Override
            public Collection<String> getPrivacyStatementURLs() {
                return List.of("https://apereo.org/cas");
            }
        };
        assertNotNull(info.getDescription());
        assertNotNull(info.getDisplayName());
        assertTrue(info.getLogoWidth() > 0);
        assertTrue(info.getLogoHeight() > 0);

    }

}
