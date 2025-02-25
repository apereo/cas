package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationDeviceProviderAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
public class DuoSecurityMultifactorAuthenticationDeviceProviderAction extends BaseCasWebflowAction
    implements MultifactorAuthenticationDeviceProviderAction {

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();
        val providers = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, DuoSecurityMultifactorAuthenticationProvider.class).values();
        val accounts = providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .filter(provider -> Objects.nonNull(provider.getDeviceManager()))
            .map(provider -> provider.getDeviceManager().findRegisteredDevices(principal))
            .flatMap(List::stream)
            .collect(Collectors.toSet());
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationRegisteredDevices(requestContext, accounts);
        return null;
    }
}
