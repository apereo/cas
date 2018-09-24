package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.adaptors.gauth.repository.credentials.BaseGoogleAuthenticatorCredentialRepository;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.couchdb.gauth.CouchDbOneTimeTokenAccount;
import org.apereo.cas.couchdb.gauth.OneTimeTokenAccountCouchDbRepository;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Collection;

/**
 * This is {@link CouchDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Slf4j
public class CouchDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorCredentialRepository {

    /**
     * CouchDb instance for tokens storage.
     */
    private OneTimeTokenAccountCouchDbRepository couchDb;

    public CouchDbGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                               final OneTimeTokenAccountCouchDbRepository googleAuthenticatorAccountRepository,
                                                               final CipherExecutor<String, String> tokenCredentialCipher) {
        super(googleAuthenticator, tokenCredentialCipher);
        this.couchDb = googleAuthenticatorAccountRepository;
    }

    @Override
    public OneTimeTokenAccount get(final String username) {
        val tokenAccount = couchDb.findOneByUsername(username);
        if (tokenAccount == null) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
        } else {
            return decode(tokenAccount);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return couchDb.getAll();
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val couchDbOneTimeTokenAccount = couchDb.findOneByUsername(account.getUsername());
        if (couchDbOneTimeTokenAccount == null) {
            val newAccount = new CouchDbOneTimeTokenAccount(encode(account));
            couchDb.add(newAccount);
            return newAccount;
        } else {
            couchDb.update(couchDbOneTimeTokenAccount.update(encode(account)));
            return couchDbOneTimeTokenAccount;
        }
    }

    @Override
    public void deleteAll() {
        couchDb.getAll().forEach(t -> couchDb.deleteTokenAccount(t));
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
