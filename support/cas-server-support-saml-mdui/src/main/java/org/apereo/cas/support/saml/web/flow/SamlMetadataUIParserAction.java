package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.web.flow.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.web.flow.mdui.SimpleMetadataUIInfo;
import org.apereo.cas.web.support.WebUtils;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2mdui.UIInfo;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * This is {@link SamlMetadataUIParserAction} that attempts to parse
 * the mdui extension block for a SAML SP from the provided metadata locations.
 * The result is put into the flow request context under the parameter
 * {@link #MDUI_FLOW_PARAMETER_NAME}. The entity id parameter is
 * specified by default at {@link SamlProtocolConstants#PARAMETER_ENTITY_ID}.
 *
 * <p>This action is best suited to be invoked when the CAS login page
 * is about to render so that the page, once the MDUI info is obtained,
 * has a chance to populate the UI with relevant info about the SP.</p>
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIParserAction extends AbstractAction {
    /**
     * The default entityId parameter name.
     */
    public static final String MDUI_FLOW_PARAMETER_NAME = "mduiContext";

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private String entityIdParameterName;
    
    private MetadataResolverAdapter metadataAdapter;
    
    private ServicesManager servicesManager;
    
    private ServiceFactory<WebApplicationService> serviceFactory;

    /**
     * Instantiates a new SAML mdui parser action.
     * Defaults the parameter name to {@link SamlProtocolConstants#PARAMETER_ENTITY_ID}.
     *
     * @param metadataAdapter the metadata resources
     */
    public SamlMetadataUIParserAction(final MetadataResolverAdapter metadataAdapter) {
        this(SamlProtocolConstants.PARAMETER_ENTITY_ID, metadataAdapter);
    }

    /**
     * Instantiates a new SAML mdui parser action.
     *
     * @param entityIdParameterName the entity id parameter name
     * @param metadataAdapter     the metadata adapter
     */
    public SamlMetadataUIParserAction(final String entityIdParameterName,
                                      final MetadataResolverAdapter metadataAdapter) {
        this.entityIdParameterName = entityIdParameterName;
        this.metadataAdapter = metadataAdapter;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(requestContext);
        final String entityId = request.getParameter(this.entityIdParameterName);
        if (StringUtils.isBlank(entityId)) {
            logger.debug("No entity id found for parameter [{}]", this.entityIdParameterName);
            return success();
        }

        final WebApplicationService service = this.serviceFactory.createService(entityId);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            logger.debug("Entity id [{}] is not recognized/allowed by the CAS service registry", entityId);

            if (registeredService != null) {
                WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext,
                        registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            }

            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Entity " + entityId + " not recognized");
        }

        final EntityDescriptor entityDescriptor = this.metadataAdapter.getEntityDescriptorForEntityId(entityId);
        if (entityDescriptor == null) {
            logger.debug("Entity descriptor not found for [{}]", entityId);
            return success();
        }

        final SPSSODescriptor spssoDescriptor = getSPSsoDescriptor(entityDescriptor);
        if (spssoDescriptor == null) {
            logger.debug("SP SSO descriptor not found for [{}]", entityId);
            return success();
        }

        final Extensions extensions = spssoDescriptor.getExtensions();
        if (extensions == null) {
            logger.debug("No extensions are found for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return success();
        }

        final List<XMLObject> spExtensions = extensions.getUnknownXMLObjects(UIInfo.DEFAULT_ELEMENT_NAME);
        if (spExtensions.isEmpty()) {
            logger.debug("No extensions are located for [{}]", UIInfo.DEFAULT_ELEMENT_NAME.getNamespaceURI());
            return success();
        }

        final SimpleMetadataUIInfo mdui = new SimpleMetadataUIInfo(registeredService);

        spExtensions.stream().filter(obj -> obj instanceof UIInfo).forEach(obj -> {
            final UIInfo uiInfo = (UIInfo) obj;
            logger.debug("Found UI info for [{}] and added to flow context", entityId);
            mdui.setUIInfo(uiInfo);
        });

        requestContext.getFlowScope().put(MDUI_FLOW_PARAMETER_NAME, mdui);
        return success();
    }

    /**
     * Gets SP SSO descriptor.
     *
     * @param entityDescriptor the entity descriptor
     * @return the SP SSO descriptor
     */
    private SPSSODescriptor getSPSsoDescriptor(final EntityDescriptor entityDescriptor) {
        logger.debug("Locating SP SSO descriptor for SAML2 protocol...");
        SPSSODescriptor spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        if (spssoDescriptor == null) {
            logger.debug("Locating SP SSO descriptor for SAML11 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML11P_NS);
        }
        if (spssoDescriptor == null) {
            logger.debug("Locating SP SSO descriptor for SAML1 protocol...");
            spssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML10P_NS);
        }
        logger.debug("SP SSO descriptor resolved to be [{}]", spssoDescriptor);
        return spssoDescriptor;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setServiceFactory(final ServiceFactory<WebApplicationService> serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
}
