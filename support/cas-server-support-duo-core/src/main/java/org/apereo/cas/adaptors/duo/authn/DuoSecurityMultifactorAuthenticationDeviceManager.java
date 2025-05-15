package org.apereo.cas.adaptors.duo.authn;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccount;
import org.apereo.cas.authentication.device.MultifactorAuthenticationDeviceManager;
import org.apereo.cas.authentication.device.MultifactorAuthenticationRegisteredDevice;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link DuoSecurityMultifactorAuthenticationDeviceManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class DuoSecurityMultifactorAuthenticationDeviceManager implements MultifactorAuthenticationDeviceManager {
    private final DuoSecurityMultifactorAuthenticationProvider duoSecurityMultifactorAuthenticationProvider;

    @Override
    public List<MultifactorAuthenticationRegisteredDevice> findRegisteredDevices(final Principal principal) {
        return Stream.of(duoSecurityMultifactorAuthenticationProvider)
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .map(provider -> provider.getDuoAuthenticationService().getAdminApiService())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Unchecked.function(service -> service.getDuoSecurityUserAccount(principal.getId())))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(this::mapDuoSecurityDevice)
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    public void removeRegisteredDevice(final Principal principal, final String deviceId) {
        Stream.of(duoSecurityMultifactorAuthenticationProvider)
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .map(provider -> provider.getDuoAuthenticationService().getAdminApiService())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(Unchecked.consumer(service -> service.deleteDuoSecurityUserDevice(principal.getId(), deviceId)));
    }

    @Override
    public List<String> getSource() {
        return List.of("Duo Security");
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
                    .source(getSource().getFirst())
                    .details(Map.of("providerId", Objects.requireNonNull(acct.getProviderId(), "Provider ID cannot be null")))
                    .build();
            })
            .collect(Collectors.toList());
    }
}
