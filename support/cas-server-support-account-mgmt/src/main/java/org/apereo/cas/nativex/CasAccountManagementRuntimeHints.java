package org.apereo.cas.nativex;

import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.val;
import org.springframework.aot.hint.RuntimeHints;
import java.util.List;

/**
 * This is {@link CasAccountManagementRuntimeHints}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class CasAccountManagementRuntimeHints implements CasRuntimeHintsRegistrar {
    @Override
    public void registerHints(final RuntimeHints hints, final ClassLoader classLoader) {
        val entries = List.<Class>of(AccountRegistrationProvisionerConfigurer.class);
        registerProxyHints(hints, entries);
        registerReflectionHints(hints, entries);
    }
}
