package org.apereo.cas.syncope.passwordless;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.model.support.passwordless.account.PasswordlessAuthenticationSyncopeAccountsProperties;
import org.apereo.cas.syncope.SyncopeUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SyncopePasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class SyncopePasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    private final ConfigurableApplicationContext applicationContext;
    private final List<PasswordlessUserAccountCustomizer> customizerList;
    private final PasswordlessAuthenticationSyncopeAccountsProperties properties;

    @Override
    public Optional<? extends PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        val account = locatePasswordlessAccount(request);
        customizerList
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .forEach(customizer -> customizer.customize(account));
        return account;
    }

    protected Optional<? extends PasswordlessUserAccount> locatePasswordlessAccount(
        final PasswordlessAuthenticationRequest request) {
        val results = SyncopeUtils.syncopeUserSearch(properties, request.getUsername());
        if (results.size() == 1) {
            val syncopeUser = results.getFirst();
            return Optional.of(PasswordlessUserAccount
                .builder()
                .username(getPasswordlessUserAttribute(syncopeUser, "username"))
                .name(getPasswordlessUserAttribute(syncopeUser, "name"))
                .email(getPasswordlessUserAttribute(syncopeUser, "email"))
                .phone(getPasswordlessUserAttribute(syncopeUser, "phoneNumber"))
                .build()
            );
        }
        return Optional.empty();
    }

    protected String getPasswordlessUserAttribute(final Map<String, List<Object>> syncopeUser,
                                                  final String attributeName) {
        return syncopeUser.containsKey(attributeName)
            ? syncopeUser.get(attributeName).getFirst().toString()
            : null;
    }

}

