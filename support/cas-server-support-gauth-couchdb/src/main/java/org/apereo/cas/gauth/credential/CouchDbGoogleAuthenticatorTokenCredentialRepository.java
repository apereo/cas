package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.couchdb.gauth.credential.CouchDbGoogleAuthenticatorAccount;
import org.apereo.cas.couchdb.gauth.credential.GoogleAuthenticatorAccountCouchDbRepository;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CouchDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class CouchDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorCredentialRepository {
    private final GoogleAuthenticatorAccountCouchDbRepository couchDb;

    public CouchDbGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                               final GoogleAuthenticatorAccountCouchDbRepository googleAuthenticatorAccountRepository,
                                                               final CipherExecutor<String, String> tokenCredentialCipher) {
        super(googleAuthenticator, tokenCredentialCipher);
        this.couchDb = googleAuthenticatorAccountRepository;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val accounts = couchDb.findByUsername(username);
        if (accounts == null || accounts.isEmpty()) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
            return new ArrayList<>(0);
        }
        return decode(accounts);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return couchDb.getAll();
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val records = couchDb.findByUsername(account.getUsername());
        if (records == null || records.isEmpty()) {
            val newAccount = CouchDbGoogleAuthenticatorAccount.from(encode(account));
            couchDb.add(newAccount);
            return newAccount;
        }
        records.stream()
            .filter(rec -> rec.getId() == account.getId())
            .findFirst()
            .ifPresent(act -> couchDb.update(CouchDbGoogleAuthenticatorAccount.from(account)));
        return account;
    }

    @Override
    public void deleteAll() {
        couchDb.getAll().forEach(couchDb::deleteTokenAccount);
    }

    @Override
    public void delete(final String username) {
        couchDb.findByUsername(username).forEach(couchDb::deleteTokenAccount);
    }

    @Override
    public long count() {
        return couchDb.count();
    }
}
