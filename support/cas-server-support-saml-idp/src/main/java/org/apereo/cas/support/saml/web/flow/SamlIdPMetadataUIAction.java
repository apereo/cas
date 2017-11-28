package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.mdui.SamlMetadataUIInfo;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link SamlIdPMetadataUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SamlIdPMetadataUIAction extends AbstractAction {
    private final ServicesManager servicesManager;

    private final SamlRegisteredServiceCachingMetadataResolver resolver;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    public SamlIdPMetadataUIAction(final ServicesManager servicesManager,
                                   final SamlRegisteredServiceCachingMetadataResolver resolver,
                                   final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        this.servicesManager = servicesManager;
        this.resolver = resolver;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Service service = this.serviceSelectionStrategy.resolveService(WebUtils.getService(requestContext));
        if (service != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof SamlRegisteredService) {
                final SamlRegisteredService samlService = SamlRegisteredService.class.cast(registeredService);
                final Optional<SamlRegisteredServiceServiceProviderMetadataFacade> adaptor =
                        SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, service.getId());

                if (!adaptor.isPresent()) {
                    throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                            "Cannot find metadata linked to " + service.getId());
                }

                final SamlMetadataUIInfo mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(adaptor.get().getEntityDescriptor(),
                        service.getId(), registeredService, WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext));
                WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
            }
        }
        return success();
    }
}
