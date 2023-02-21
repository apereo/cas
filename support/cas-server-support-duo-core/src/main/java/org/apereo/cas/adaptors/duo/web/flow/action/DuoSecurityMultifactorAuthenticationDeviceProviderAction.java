package org.apereo.cas.adaptors.duo.web.flow.action;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    private final ApplicationContext applicationContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val principal = authentication.getPrincipal();

        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        val accounts = providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .map(provider -> provider.getDuoAuthenticationService().getAdminApiService())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Unchecked.function(service -> service.getDuoSecurityUserAccount(principal.getId())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::mapDuoSecurityDevice)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        WebUtils.putMultifactorAuthenticationRegisteredDevices(requestContext, accounts);
        return null;
    }

    protected List<MultifactorAuthenticationRegisteredDevice> mapDuoSecurityDevice(final DuoSecurityUserAccount acct) {
        return acct
            .getDevices()
            .stream()
            .map(device -> {
                val model = String.format("%s %s", StringUtils.defaultString(device.getModel()),
                    StringUtils.defaultString(device.getPlatform())).trim();
                return MultifactorAuthenticationRegisteredDevice.builder()
                    .id(device.getId())
                    .type(device.getType())
                    .model(model)
                    .number(device.getNumber())
                    .name(StringUtils.defaultIfBlank(device.getName(), model))
                    .payload(device.toJson())
                    .lastUsedDateTime(device.getLastSeen())
                    .source("Duo Security")
                    .build();
            })
            .collect(Collectors.toList());

    }
}
