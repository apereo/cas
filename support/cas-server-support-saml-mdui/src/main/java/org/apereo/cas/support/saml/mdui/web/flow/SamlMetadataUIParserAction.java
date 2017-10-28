package org.apereo.cas.support.saml.mdui.web.flow;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link SamlMetadataUIParserAction} that attempts to parse
 * the MDUI extension block for a SAML SP from the provided metadata locations.
 * The result is put into the flow request context. The entity id parameter is
 * specified by default at {@link org.apereo.cas.support.saml.SamlProtocolConstants#PARAMETER_ENTITY_ID}.
 * <p>This action is best suited to be invoked when the CAS login page
 * is about to render so that the page, once the MDUI info is obtained,
 * has a chance to populate the UI with relevant info about the SP.</p>
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class SamlMetadataUIParserAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlMetadataUIParserAction.class);

    private final String entityIdParameterName;
    private final MetadataResolverAdapter metadataAdapter;

    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> serviceFactory;

    /**
     * Instantiates a new SAML MDUI parser action.
     *
     * @param entityIdParameterName the entity id parameter name
     * @param metadataAdapter       the metadata adapter
     * @param serviceFactory        the service factory
     * @param servicesManager       the service manager
     */
    public SamlMetadataUIParserAction(final String entityIdParameterName, final MetadataResolverAdapter metadataAdapter,
                                      final ServiceFactory<WebApplicationService> serviceFactory, final ServicesManager servicesManager) {
        this.entityIdParameterName = entityIdParameterName;
        this.metadataAdapter = metadataAdapter;
        this.serviceFactory = serviceFactory;
        this.servicesManager = servicesManager;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        final String entityId = getEntityIdFromRequest(requestContext);
        if (StringUtils.isBlank(entityId)) {
            LOGGER.debug("No entity id found for parameter [{}]", this.entityIdParameterName);
            return success();
        }

        LOGGER.debug("Located entity id [{}] from request", entityId);
        
        if (!MetadataUIUtils.isMetadataFoundForEntityId(metadataAdapter, entityId)) {
            LOGGER.debug("Metadata is not found for entity [{}] and CAS service registry is consulted for the entity definition", entityId);
            final RegisteredService registeredService = getRegisteredServiceFromRequest(requestContext, entityId);
            LOGGER.debug("Registered service definition linked to [{}] is found as [{}]", entityId, registeredService);
            verifyRegisteredService(requestContext, registeredService);
            loadSamlMetadataIntoRequestContext(requestContext, entityId, registeredService);
        } else {
            LOGGER.debug("Metadata is found for entity [{}]", entityId);
            final RegisteredService registeredService = getRegisteredServiceFromRequest(requestContext);
            LOGGER.debug("Registered service definition linked to [{}] is found as [{}]", entityId, registeredService);
            verifyRegisteredService(requestContext, registeredService);
            loadSamlMetadataIntoRequestContext(requestContext, entityId, registeredService);
        }

        return success();
    }

    /**
     * Load saml metadata into request context.
     *
     * @param requestContext    the request context
     * @param entityId          the entity id
     * @param registeredService the registered service
     */
    protected void loadSamlMetadataIntoRequestContext(final RequestContext requestContext, final String entityId, final RegisteredService registeredService) {
        LOGGER.debug("Locating SAML MDUI for entity [{}]", entityId);
        final SamlMetadataUIInfo mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(
            this.metadataAdapter, entityId, registeredService, WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext));
        LOGGER.debug("Located SAML MDUI for entity [{}] as [{}]", entityId, mdui);
        WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
    }

    /**
     * Verify registered service.
     *
     * @param requestContext    the request context
     * @param registeredService the registered service
     */
    protected void verifyRegisteredService(final RequestContext requestContext, final RegisteredService registeredService) {
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.debug("Service [{}] is not recognized/allowed by the CAS service registry", registeredService);
            if (registeredService != null) {
                WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
    }

    /**
     * Gets registered service from request.
     *
     * @param requestContext the request context
     * @param entityId       the entity id
     * @return the registered service from request
     */
    protected RegisteredService getRegisteredServiceFromRequest(final RequestContext requestContext, final String entityId) {
        final Service currentService = WebUtils.getService(requestContext);
        final WebApplicationService service = this.serviceFactory.createService(entityId);
        RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null) {
            LOGGER.debug("Entity id [{}] not found in the registry. Fallback onto [{}]", entityId, currentService);
            registeredService = this.servicesManager.findServiceBy(currentService);
        }
        LOGGER.debug("Located service definition [{}]", registeredService);
        return registeredService;
    }
    
    /**
     * Gets registered service from request.
     *
     * @param requestContext the request context
     * @return the registered service from request
     */
    protected RegisteredService getRegisteredServiceFromRequest(final RequestContext requestContext) {
        final Service currentService = WebUtils.getService(requestContext);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(currentService);
        return registeredService;
    }

    /**
     * Gets entity id from request.
     *
     * @param requestContext the request context
     * @return the entity id from request
     */
    protected String getEntityIdFromRequest(final RequestContext requestContext) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        return request.getParameter(this.entityIdParameterName);
    }
}
