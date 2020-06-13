package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredServiceTestUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.ext.saml2mdui.DisplayName;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MetadataUIUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Utility")
public class MetadataUIUtilsTests {
    @Test
    public void verifyNoDescriptor() {
        val entity = mock(EntityDescriptor.class);
        assertNull(MetadataUIUtils.getSPSsoDescriptor(entity));
    }

    @Test
    public void verifyDescriptor() {
        val entity = mock(EntityDescriptor.class);
        when(entity.getSPSSODescriptor(anyString())).thenReturn(mock(SPSSODescriptor.class));
        assertNotNull(MetadataUIUtils.getSPSsoDescriptor(entity));
    }

    @Test
    public void verifyLocate() {
        val entity = mock(EntityDescriptor.class);

        val request = new MockHttpServletRequest();
        val id = UUID.randomUUID().toString();
        val service = RegisteredServiceTestUtils.getRegisteredService();
        var mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(entity, id, service, request);
        assertNotNull(mdui);

        val sp = mock(SPSSODescriptor.class);
        when(entity.getSPSSODescriptor(anyString())).thenReturn(sp);
        mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(entity, id, service, request);
        assertNotNull(mdui);

        val extensions = mock(Extensions.class);
        when(sp.getExtensions()).thenReturn(extensions);
        mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(entity, id, service, request);
        assertNotNull(mdui);

        val info = mock(UIInfo.class);
        val displayName = mock(DisplayName.class);
        when(displayName.getValue()).thenReturn("CAS");
        when(info.getDisplayNames()).thenReturn(List.of(displayName));
        when(extensions.getUnknownXMLObjects(any())).thenReturn(List.of(info));
        mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(entity, id, service, request);
        assertNotNull(mdui);
        assertEquals("CAS", mdui.getDisplayName());
    }
}
