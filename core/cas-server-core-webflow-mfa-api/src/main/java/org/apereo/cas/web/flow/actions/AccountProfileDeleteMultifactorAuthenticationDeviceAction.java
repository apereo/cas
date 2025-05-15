package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccountProfileDeleteMultifactorAuthenticationDeviceAction}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class AccountProfileDeleteMultifactorAuthenticationDeviceAction extends BaseCasWebflowAction {
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val deviceId = requestContext.getRequestParameters().getRequired("id");
        val source = requestContext.getRequestParameters().getRequired("source");
        val principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val providers = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(MultifactorAuthenticationProvider::getDeviceManager)
            .filter(BeanSupplier::isNotProxy)
            .filter(manager -> manager.getSource().contains(source))
            .toList();
        providers.forEach(provider -> provider.removeRegisteredDevice(principal, deviceId));
        return success();
    }
}
