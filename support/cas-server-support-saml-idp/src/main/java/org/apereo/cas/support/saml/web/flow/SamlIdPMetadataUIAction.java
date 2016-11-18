package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.mdui.SimpleMetadataUIInfo;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SamlIdPMetadataUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlIdPMetadataUIAction extends AbstractAction {
    private final ServicesManager servicesManager;

    private final SamlRegisteredServiceCachingMetadataResolver resolver;

    private final ValidationServiceSelectionStrategy serviceSelectionStrategy;

    public SamlIdPMetadataUIAction(final ServicesManager servicesManager,
                                   final SamlRegisteredServiceCachingMetadataResolver resolver,
                                   final ValidationServiceSelectionStrategy serviceSelectionStrategy) {
        this.servicesManager = servicesManager;
        this.resolver = resolver;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        Service service = WebUtils.getService(requestContext);
        if (service != null) {
            service = serviceSelectionStrategy.resolveServiceFrom(service);
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);


            if (registeredService instanceof SamlRegisteredService) {

                final SamlRegisteredService samlService = SamlRegisteredService.class.cast(registeredService);
                final SamlRegisteredServiceServiceProviderMetadataFacade facade =
                        SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, service.getId());

                final SimpleMetadataUIInfo mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(facade.getEntityDescriptor(),
                        service.getId(), registeredService);
                WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
            }
        }
        return success();
    }
}
