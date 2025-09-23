package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class DynamoDbGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private final DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator facilitator;

    public DynamoDbGoogleAuthenticatorTokenCredentialRepository(final CasGoogleAuthenticator googleAuthenticator,
                                                                final CipherExecutor<String, String> tokenCredentialCipher,
                                                                final CipherExecutor<Number, Number> scratchCodesCipher,
                                                                final DynamoDbGoogleAuthenticatorTokenCredentialRepositoryFacilitator facilitator) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.facilitator = facilitator;
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        val r = facilitator.find(id);
        return Optional.ofNullable(r).map(this::decode).orElse(null);
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        val r = facilitator.find(username, id);
        return Optional.ofNullable(r).map(this::decode).orElse(null);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val r = facilitator.find(username);
        return decode(r);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        val r = facilitator.findAll();
        return r.stream()
            .map(this::decode)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return update(account.assignIdIfNecessary());
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encodedAccount = encode(account);
        facilitator.store(encodedAccount);
        return encodedAccount;
    }

    @Override
    public void deleteAll() {
        facilitator.removeAll();
    }

    @Override
    public void delete(final String username) {
        facilitator.remove(username);
    }

    @Override
    public void delete(final long id) {
        facilitator.remove(id);
    }

    @Override
    public long count() {
        return facilitator.count();
    }

    @Override
    public long count(final String username) {
        return facilitator.count(username);
    }
}
