package org.apereo.cas.authentication.handler.support.jaas;

import com.google.common.base.Splitter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link AccountsPreDefinedLoginModule}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class AccountsPreDefinedLoginModule implements LoginModule {
    private static final int MAP_SIZE = 8;

    private Subject subject;
    private CallbackHandler callbackHandler;


    private Map<String, String> accounts;

    private boolean succeeded;

    @Override
    public void initialize(final Subject subject, final CallbackHandler callbackHandler,
                           final Map<String, ?> sharedState, final Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.accounts = new LinkedHashMap<>(MAP_SIZE);

        val providedAccounts = options.containsKey("accounts") ? options.get("accounts").toString() : null;
        if (StringUtils.isNotBlank(providedAccounts)) {
            val eachAccount = org.springframework.util.StringUtils.commaDelimitedListToSet(providedAccounts);
            eachAccount.stream()
                .map(account -> Splitter.on("::").splitToList(account))
                .filter(results -> results.size() == 2)
                .forEach(results -> accounts.put(results.get(0), results.get(1)));
        }
    }

    @Override
    public boolean login() throws LoginException {
        val nameCallback = new NameCallback("username");
        val passwordCallback = new PasswordCallback("password", false);

        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            throw new FailedLoginException(e.getMessage());
        }

        val username = nameCallback.getName();
        if (accounts.containsKey(username)) {
            val password = new String(passwordCallback.getPassword());
            this.succeeded = accounts.get(username).equals(password);
            subject.getPrincipals().add(new StaticPrincipal(username));
            return true;
        }
        this.succeeded = false;
        return false;
    }

    @Override
    public boolean commit() {
        return this.succeeded;
    }

    @Override
    public boolean abort() {
        return false;
    }

    @Override
    public boolean logout() {
        return true;
    }

    /**
     * Static principal added to the subject when authn is successful.
     */
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class StaticPrincipal implements Principal {
        private String name;
    }
}
