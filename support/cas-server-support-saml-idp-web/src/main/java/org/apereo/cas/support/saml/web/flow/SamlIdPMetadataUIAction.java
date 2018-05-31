package org.apereo.cas.support.saml.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
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
@Slf4j
@AllArgsConstructor
public class SamlIdPMetadataUIAction extends AbstractAction {
    private final ServicesManager servicesManager;

    private final SamlRegisteredServiceCachingMetadataResolver resolver;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var service = this.serviceSelectionStrategy.resolveService(WebUtils.getService(requestContext));
        if (service != null) {
            final var registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof SamlRegisteredService) {
                final var samlService = SamlRegisteredService.class.cast(registeredService);
                final var adaptor =
                        SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, service.getId());

                if (!adaptor.isPresent()) {
                    throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                            "Cannot find metadata linked to " + service.getId());
                }

                final var mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(adaptor.get().getEntityDescriptor(),
                        service.getId(), registeredService, WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext));
                WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
            }
        }
        return success();
    }
}
