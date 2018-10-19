package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SamlIdPMetadataUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class SamlIdPMetadataUIAction extends AbstractAction {
    private final ServicesManager servicesManager;

    private final SamlRegisteredServiceCachingMetadataResolver resolver;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val service = this.serviceSelectionStrategy.resolveService(WebUtils.getService(requestContext));
        if (service != null) {
            val registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof SamlRegisteredService) {
                val samlService = SamlRegisteredService.class.cast(registeredService);
                val adaptor =
                    SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, service.getId());

                if (adaptor.isEmpty()) {
                    throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                        "Cannot find metadata linked to " + service.getId());
                }

                val mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(adaptor.get().getEntityDescriptor(),
                    service.getId(), registeredService, WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext));
                WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
            }
        }
        return success();
    }
}
