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
public class CouchDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private final GoogleAuthenticatorAccountCouchDbRepository couchDbRepository;

    public CouchDbGoogleAuthenticatorTokenCredentialRepository(final IGoogleAuthenticator googleAuthenticator,
                                                               final GoogleAuthenticatorAccountCouchDbRepository couchDbRepository,
                                                               final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.couchDbRepository = couchDbRepository;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return this.couchDbRepository.findByIdAndUsername(id, username);
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        return this.couchDbRepository.findById(id);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val accounts = couchDbRepository.findByUsername(username);
        if (accounts == null || accounts.isEmpty()) {
            LOGGER.debug("No record could be found for google authenticator id [{}]", username);
            return new ArrayList<>(0);
        }
        return decode(accounts);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return couchDbRepository.getAll();
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val records = couchDbRepository.findByUsername(account.getUsername());
        if (records == null || records.isEmpty()) {
            val newAccount = CouchDbGoogleAuthenticatorAccount.from(encode(account));
            couchDbRepository.add(newAccount);
            return newAccount;
        }
        records.stream()
            .filter(rec -> rec.getId() == account.getId())
            .map(CouchDbGoogleAuthenticatorAccount.class::cast)
            .findFirst()
            .ifPresent(act -> couchDbRepository.update(CouchDbGoogleAuthenticatorAccount.from(account)));
        return account;
    }

    @Override
    public void deleteAll() {
        couchDbRepository.getAll().forEach(couchDbRepository::deleteTokenAccount);
    }

    @Override
    public void delete(final String username) {
        couchDbRepository.findByUsername(username).forEach(couchDbRepository::deleteTokenAccount);
    }

    @Override
    public void delete(final long id) {
        val entity = (CouchDbGoogleAuthenticatorAccount) couchDbRepository.findById(id);
        couchDbRepository.deleteTokenAccount(entity);
    }

    @Override
    public long count() {
        return couchDbRepository.count();
    }

    @Override
    public long count(final String username) {
        return couchDbRepository.count(username);
    }
}
