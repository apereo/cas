package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.ext.saml2mdui.Description;
import org.opensaml.saml.ext.saml2mdui.DisplayName;
import org.opensaml.saml.ext.saml2mdui.Logo;
import org.opensaml.saml.ext.saml2mdui.UIInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlMetadataUIInfoTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("SAML")
public class SamlMetadataUIInfoTests {
    @Test
    public void verifyInfoNotAvailable() {
        val service = RegisteredServiceTestUtils.getRegisteredService();
        service.setPrivacyUrl("http://cas.example.org");
        service.setInformationUrl("http://cas.example.org");
        val info = new SamlMetadataUIInfo(service, "en");
        assertEquals(service.getName(), info.getDisplayName());
        assertEquals(service.getDescription(), info.getDescription());
        assertEquals(service.getInformationUrl(), info.getInformationURL());
        assertEquals("en", info.getLocale());
        assertTrue(info.getLogoUrls().isEmpty());
        assertEquals(service.getPrivacyUrl(), info.getPrivacyStatementURL());
    }

    @Test
    public void verifyInfo() {
        val mdui = mock(UIInfo.class);
        val description = mock(Description.class);
        when(description.getValue()).thenReturn("Description");
        when(description.getXMLLang()).thenReturn("en");

        val names = mock(DisplayName.class);
        when(names.getValue()).thenReturn("Name");
        when(names.getXMLLang()).thenReturn("en");

        when(mdui.getDescriptions()).thenReturn(CollectionUtils.wrapList(description));
        when(mdui.getDisplayNames()).thenReturn(CollectionUtils.wrapList(names));

        val logo = mock(Logo.class);
        when(logo.getURI()).thenReturn("https://example.logo.com");
        when(logo.getWidth()).thenReturn(16);
        when(logo.getHeight()).thenReturn(16);
        when(mdui.getLogos()).thenReturn(List.of(logo));

        val service = RegisteredServiceTestUtils.getRegisteredService();
        val info = new SamlMetadataUIInfo(mdui, service);
        assertEquals(names.getValue(), info.getDisplayName());
        assertEquals(description.getValue(), info.getDescription());
        assertFalse(info.getLogoUrls().isEmpty());
    }
}
