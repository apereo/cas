package org.apereo.cas.support.saml.mdui;

import org.apereo.cas.services.RegisteredService;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link MetadataUIUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@UtilityClass
public class MetadataUIUtils {

    /**
     * Gets SP SSO descriptor.
     *
     * @param entityDescriptor the entity descriptor
     * @return the SP SSO descriptor
     */
    public static SPSSODescriptor getSPSsoDescriptor(final EntityDescriptor entityDescriptor) {
        LOGGER.trace("Locating SP SSO descriptor for SAML2 protocol...");
        var spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (spssoDescriptor == null) {
            LOGGER.trace("Locating SP SSO descriptor for SAML11 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML11P_NS);
        }
        if (spssoDescriptor == null) {
            LOGGER.trace("Locating SP SSO descriptor for SAML1 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML10P_NS);
        }
        LOGGER.trace("SP SSO descriptor resolved to be [{}]", spssoDescriptor);
        return spssoDescriptor;
    }

    /**
     * Is metadata found for entity id ?
     *
     * @param metadataAdapter the metadata adapter
     * @param entityId        the entity id
     * @return true/false
     */
    public static boolean isMetadataFoundForEntityId(final MetadataResolverAdapter metadataAdapter, final String entityId) {
        return metadataAdapter.getEntityDescriptorForEntityId(entityId) != null;
    }

    /**
     * Locate MDUI for entity id simple metadata ui info.
     *
     * @param metadataAdapter   the metadata adapter
     * @param entityId          the entity id
     * @param registeredService the registered service
     * @param requestContext    the request context
     * @return the simple metadata ui info
     */
    public static SamlMetadataUIInfo locateMetadataUserInterfaceForEntityId(final MetadataResolverAdapter metadataAdapter,
                                                                            final String entityId, final RegisteredService registeredService, final HttpServletRequest requestContext) {
        val entityDescriptor = metadataAdapter.getEntityDescriptorForEntityId(entityId);
        return locateMetadataUserInterfaceForEntityId(entityDescriptor, entityId, registeredService, requestContext);
    }

    /**
     * Locate mdui for entity id simple metadata ui info.
     *
     * @param entityDescriptor  the entity descriptor
     * @param entityId          the entity id
     * @param registeredService the registered service
     * @param requestContext    the request context
     * @return the simple metadata ui info
     */
    public static SamlMetadataUIInfo locateMetadataUserInterfaceForEntityId(final EntityDescriptor entityDescriptor, final String entityId,
                                                                            final RegisteredService registeredService, final HttpServletRequest requestContext) {
        val mdui = new SamlMetadataUIInfo(registeredService, requestContext.getLocale().getLanguage());
        if (entityDescriptor == null) {
            LOGGER.trace("Entity descriptor not found for [{}]", entityId);
            return mdui;
        }
        val spssoDescriptor = getSPSsoDescriptor(entityDescriptor);
        if (spssoDescriptor == null) {
            LOGGER.trace("SP SSO descriptor not found for [{}]", entityId);
            return mdui;
        }
        val extensions = spssoDescriptor.getExtensions();
        if (extensions == null) {
            LOGGER.trace("No extensions in the SP SSO descriptor are found for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return mdui;
        }
        val spExtensions = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
        if (spExtensions.isEmpty()) {
            LOGGER.trace("No extensions in the SP SSO descriptor are located for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return mdui;
        }
        spExtensions.stream().filter(UIInfo.class::isInstance).forEach(obj -> {
            val uiInfo = (UIInfo) obj;
            LOGGER.trace("Found MDUI info for [{}]", entityId);
            mdui.setUiInfo(uiInfo);
        });
        return mdui;
    }
}
