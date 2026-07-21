package org.apereo.cas.impl.account;

import module java.base;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.api.PasswordlessUserAccountCustomizer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ScriptResourceCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link GroovyPasswordlessUserAccountCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyPasswordlessUserAccountCustomizer implements PasswordlessUserAccountCustomizer {
    protected final CasConfigurationProperties casProperties;
    protected final ConfigurableApplicationContext applicationContext;
    protected final ScriptResourceCacheManager scriptResourceCacheManager;

    @Override
    public Optional<? extends PasswordlessUserAccount> customize(final Optional<? extends PasswordlessUserAccount> account) {
        return FunctionUtils.doAndHandle(() -> {
            if (account.isPresent()) {
                val args = new Object[]{account.get(), applicationContext, LOGGER};
                val resource = casProperties.getAuthn().getPasswordless().getCore()
                    .getPasswordlessAccountCustomizerScript().getLocation();
                val watchableScript = scriptResourceCacheManager.resolveScriptableResource(resource.getURI().toString());
                return Optional.ofNullable(watchableScript.execute(args, PasswordlessUserAccount.class));
            }
            return account;
        }, e -> Optional.<PasswordlessUserAccount>empty()).get();
    }
}
