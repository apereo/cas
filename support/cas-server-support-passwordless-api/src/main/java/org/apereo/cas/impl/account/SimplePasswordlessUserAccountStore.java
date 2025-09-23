package org.apereo.cas.impl.account;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link SimplePasswordlessUserAccountStore}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Getter
public class SimplePasswordlessUserAccountStore implements PasswordlessUserAccountStore {
    /**
     * Map of all passwordless accounts read from resources.
     */
    protected final Map<String, PasswordlessUserAccount> accounts;

    private final ConfigurableApplicationContext applicationContext;

    private final List<PasswordlessUserAccountCustomizer> customizerList;
    
    @Override
    public Optional<PasswordlessUserAccount> findUser(final PasswordlessAuthenticationRequest request) {
        val result = accounts
            .entrySet()
            .stream()
            .filter(entry -> RegexUtils.find(entry.getKey(), request.getUsername()))
            .map(Map.Entry::getValue)
            .findFirst();
        customizerList
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .forEach(customizer -> customizer.customize(result));
        return result;
    }
}
