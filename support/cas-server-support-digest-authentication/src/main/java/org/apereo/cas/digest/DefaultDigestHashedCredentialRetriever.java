package org.apereo.cas.digest;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.security.auth.login.AccountNotFoundException;

/**
 * This is {@link DefaultDigestHashedCredentialRetriever}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultDigestHashedCredentialRetriever implements DigestHashedCredentialRetriever {
    private Table<String, String, String> store = HashBasedTable.create();

    @Override
    public String findCredential(final String uid, final String realm) throws AccountNotFoundException {
        if (store.contains(uid, realm)) {
            return store.get(uid, realm);
        }
        throw new AccountNotFoundException("Could not locate user account for " + uid);
    }

    public Table<String, String, String> getStore() {
        return store;
    }

    public void setStore(final Table<String, String, String> store) {
        this.store = store;
    }
}
