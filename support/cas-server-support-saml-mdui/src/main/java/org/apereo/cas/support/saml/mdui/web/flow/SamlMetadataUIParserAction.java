package org.apereo.cas.support.saml.mdui.web.flow;

import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.support.saml.mdui.MetadataResolverAdapter;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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
@Slf4j
@RequiredArgsConstructor
public class SamlMetadataUIParserAction extends BaseCasWebflowAction {

    private final String entityIdParameterName;

    private final MetadataResolverAdapter metadataAdapter;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final ServicesManager servicesManager;

    private final ArgumentExtractor argumentExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val entityId = getEntityIdFromRequest(requestContext);
        if (StringUtils.isBlank(entityId)) {
            LOGGER.debug("No entity id found for parameter [{}]", this.entityIdParameterName);
            return success();
        }
        LOGGER.debug("Located entity id [{}] from request", entityId);
        if (MetadataUIUtils.isMetadataFoundForEntityId(metadataAdapter, entityId)) {
            LOGGER.debug("Metadata is found for entity [{}]", entityId);
            val registeredService = getRegisteredServiceFromRequest(requestContext);
            LOGGER.debug("Registered service definition linked to [{}] is found as [{}]", entityId, registeredService);
            verifyRegisteredService(requestContext, registeredService);
            loadSamlMetadataIntoRequestContext(requestContext, entityId, registeredService);
        } else {
            LOGGER.debug("Metadata is not found for entity [{}] and CAS service registry is consulted for the entity definition", entityId);
            val registeredService = getRegisteredServiceFromRequest(requestContext, entityId);
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
    protected void loadSamlMetadataIntoRequestContext(final RequestContext requestContext, final String entityId,
                                                      final WebBasedRegisteredService registeredService) {
        LOGGER.debug("Locating SAML MDUI for entity [{}]", entityId);
        val mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(
            this.metadataAdapter, entityId, registeredService, WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext));
        LOGGER.debug("Located SAML MDUI for entity [{}] as [{}]", entityId, mdui);
        WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
    }


    protected void verifyRegisteredService(final RequestContext requestContext, final RegisteredService registeredService) {
        val service = WebUtils.getService(requestContext);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            LOGGER.debug("Service [{}] is not recognized/allowed by the CAS service registry", registeredService);
            if (registeredService != null) {
                WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(requestContext, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            }
            throw UnauthorizedServiceException.denied("Rejected");
        }
    }

    protected WebBasedRegisteredService getRegisteredServiceFromRequest(final RequestContext requestContext, final String entityId) {
        val service = this.serviceFactory.createService(entityId);
        var registeredService = (WebBasedRegisteredService) servicesManager.findServiceBy(service);
        if (registeredService == null) {
            val currentService = WebUtils.getService(requestContext);
            LOGGER.debug("Entity id [{}] not found in the registry. Fallback onto [{}]", entityId, currentService);
            registeredService = (WebBasedRegisteredService) servicesManager.findServiceBy(currentService);
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
    protected WebBasedRegisteredService getRegisteredServiceFromRequest(final RequestContext requestContext) {
        val currentService = WebUtils.getService(requestContext);
        return (WebBasedRegisteredService) this.servicesManager.findServiceBy(currentService);
    }

    /**
     * Gets entity id from request.
     *
     * @param requestContext the request context
     * @return the entity id from request
     */
    protected String getEntityIdFromRequest(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        var entityId = request.getParameter(this.entityIdParameterName);
        if (StringUtils.isBlank(entityId)) {
            val service = argumentExtractor.extractService(request);
            if (service != null && service.getAttributes().containsKey(this.entityIdParameterName)) {
                entityId = service.getFirstAttribute(this.entityIdParameterName, String.class);
            }
        }
        return entityId;
    }
}
