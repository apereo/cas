package org.apereo.cas.digest;

import lombok.RequiredArgsConstructor;

import javax.security.auth.login.AccountNotFoundException;
import java.util.Map;

/**
 * This is {@link DefaultDigestHashedCredentialRetriever}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 * @deprecated Since 6.6
 */
@RequiredArgsConstructor
@Deprecated(since = "6.6")
public class DefaultDigestHashedCredentialRetriever implements DigestHashedCredentialRetriever {

    private final Map<String, String> store;

    @Override
    public String findCredential(final String uid, final String realm) throws AccountNotFoundException {
        if (store.containsKey(uid)) {
            return store.get(uid);
        }
        throw new AccountNotFoundException("Could not locate user account for " + uid);
    }
}
