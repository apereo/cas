package org.apereo.cas.digest;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

/**
 * This is {@link DefaultDigestHashedCredentialRetriever}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultDigestHashedCredentialRetriever implements DigestHashedCredentialRetriever {

    private final Map<String, String> store;

    public DefaultDigestHashedCredentialRetriever(final Map<String, String> users) {
        this.store = users;
    }

    @Override
    public String findCredential(final String uid, final String realm) throws AccountNotFoundException {
        if (store.containsKey(uid)) {
            return store.get(uid);
        }
        throw new AccountNotFoundException("Could not locate user account for " + uid);
    }
}
