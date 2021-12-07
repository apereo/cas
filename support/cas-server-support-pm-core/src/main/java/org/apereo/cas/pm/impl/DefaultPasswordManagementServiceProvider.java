package org.apereo.cas.pm.impl;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.PasswordManagementServiceProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultPasswordManagementServiceProvider implements PasswordManagementServiceProvider {

    private final List<PasswordManagementService> passwordManagementServices;

    private final CasConfigurationProperties casProperties;

    private final CipherExecutor passwordManagementCipherExecutor;

    @Override
    public PasswordManagementService getPasswordChangeService(RegisteredService registeredService) {
        final Optional<PasswordManagementService> first = passwordManagementServices.stream().findFirst();

        return first.orElseGet(() -> new NoOpPasswordManagementService(
                passwordManagementCipherExecutor,
                casProperties.getServer().getPrefix(),
                casProperties.getAuthn().getPm()));
    }
}
