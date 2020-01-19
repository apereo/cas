package org.apereo.cas.support.saml.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.mdui.MetadataUIUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SamlIdPMetadataUIAction extends AbstractAction {
    private final ServicesManager servicesManager;

    private final SamlRegisteredServiceCachingMetadataResolver resolver;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val service = this.serviceSelectionStrategy.resolveService(WebUtils.getService(requestContext));
        if (service != null) {
            val samlService = this.servicesManager.findServiceBy(service, SamlRegisteredService.class);
            if (samlService != null) {
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, samlService);
                val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, service.getId());

                if (adaptor.isEmpty()) {
                    LOGGER.debug("Cannot find SAML2 metadata linked to [{}]. Skipping MDUI...", service.getId());
                    return success();
                }

                val mdui = MetadataUIUtils.locateMetadataUserInterfaceForEntityId(adaptor.get().getEntityDescriptor(),
                    service.getId(), samlService, WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext));
                WebUtils.putServiceUserInterfaceMetadata(requestContext, mdui);
            }
        }
        return success();
    }
}
