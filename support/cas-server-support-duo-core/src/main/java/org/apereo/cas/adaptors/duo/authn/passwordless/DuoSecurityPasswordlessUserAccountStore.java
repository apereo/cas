package org.apereo.cas.adaptors.duo.authn.passwordless;

import org.apereo.cas.adaptors.duo.DuoSecurityUserAccountStatus;
import org.apereo.cas.adaptors.duo.authn.DuoSecurityMultifactorAuthenticationProvider;
import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link DuoSecurityPasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class DuoSecurityPasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public Optional<? extends PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) throws Throwable {
        val providers = applicationContext.getBeansOfType(DuoSecurityMultifactorAuthenticationProvider.class).values();
        return providers
            .stream()
            .filter(Objects::nonNull)
            .filter(BeanSupplier::isNotProxy)
            .map(DuoSecurityMultifactorAuthenticationProvider.class::cast)
            .filter(provider -> provider.getDuoAuthenticationService().getProperties().isPasswordlessAuthenticationEnabled())
            .map(provider -> {
                val duoService = provider.getDuoAuthenticationService();
                return duoService
                    .getAdminApiService()
                    .flatMap(admin -> FunctionUtils.doUnchecked(() -> admin.getDuoSecurityUserAccount(request.getUsername(), false)));
            })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(account -> account.getStatus() != DuoSecurityUserAccountStatus.ENROLL)
            .map(duoAccount -> PasswordlessUserAccount
                .builder()
                .username(duoAccount.getUsername())
                .email(duoAccount.getEmail())
                .name(duoAccount.getFirstName())
                .phone(duoAccount.getPhone())
                .source(duoAccount.getProviderId())
                .build())
            .findFirst();
    }
}
