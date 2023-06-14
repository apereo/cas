package org.apereo.cas.web.flow.services;

import org.apereo.cas.services.WebBasedRegisteredService;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.Serial;
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
@Tag("RegisteredService")
class DefaultRegisteredServiceUserInterfaceInfoTests {
    @Test
    void verifyOperation() {
        val info = new DefaultRegisteredServiceUserInterfaceInfo(mock(WebBasedRegisteredService.class)) {
            @Serial
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
            public Collection<String> getPrivacyStatementURLs() {
                return List.of("https://apereo.org/cas");
            }

            @Override
            public Collection<Logo> getLogoUrls() {
                return List.of(new Logo("https://logo.url", 32, 32));
            }
        };
        assertNotNull(info.getDescription());
        assertNotNull(info.getDisplayName());
        assertTrue(info.getLogoWidth() > 0);
        assertTrue(info.getLogoHeight() > 0);
    }

    @Test
    void verifySpecialCases() {
        val service = mock(WebBasedRegisteredService.class);
        when(service.getInformationUrl()).thenReturn("https://apereo.org/cas");
        when(service.getPrivacyUrl()).thenReturn("https://apereo.org/cas");
        when(service.getLogo()).thenReturn("https://apereo.org/cas");

        val info = new DefaultRegisteredServiceUserInterfaceInfo(service) {
            @Serial
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
                return List.of();
            }

            @Override
            public Collection<String> getPrivacyStatementURLs() {
                return List.of();
            }

            @Override
            public Collection<Logo> getLogoUrls() {
                throw new RuntimeException("Bad Logo");
            }
        };
        assertNotNull(info.getInformationURL());
        assertNotNull(info.getLogoUrl());
        assertNotNull(info.getPrivacyStatementURL());
        assertTrue(info.getLogoWidth() > 0);
        assertTrue(info.getLogoHeight() > 0);
    }

    @Test
    void verifyDefault() {
        val service = mock(WebBasedRegisteredService.class);
        val info = new DefaultRegisteredServiceUserInterfaceInfo(service) {
            @Serial
            private static final long serialVersionUID = 2331519665722637762L;

            @Override
            public Collection<String> getInformationURLs() {
                return List.of("informationUrl");
            }

            @Override
            public Collection<String> getPrivacyStatementURLs() {
                return List.of("PrivacyStatement");
            }
        };
        assertNotNull(info.getInformationURL());
        assertNotNull(info.getPrivacyStatementURL());
    }
}
