package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link OAuth20RegisteredServiceUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class OAuth20RegisteredServiceUIAction extends BaseCasWebflowAction implements Serializable {
    @Serial
    private static final long serialVersionUID = 5588216693657081923L;
    private final transient ServicesManager servicesManager;

    private final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        return FunctionUtils.doUnchecked(() -> {
            val serviceCtx = WebUtils.getService(requestContext);
            if (serviceCtx != null) {
                val service = serviceSelectionStrategy.resolveServiceFrom(serviceCtx);
                val registeredService = this.servicesManager.findServiceBy(service);
                RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

                if (registeredService instanceof final OAuthRegisteredService oauthService) {
                    WebUtils.putServiceUserInterfaceMetadata(requestContext, new DefaultRegisteredServiceUserInterfaceInfo(oauthService));
                }
            }
            return success();
        });
    }
}
