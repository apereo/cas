package org.apereo.cas.gauth.token;

import module java.base;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link GoogleAuthenticatorDynamoDbTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorDynamoDbTokenRepository extends BaseOneTimeTokenRepository<GoogleAuthenticatorToken> {
    private final GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator facilitator;

    private final long expireTokensInSeconds;

    @Override
    public GoogleAuthenticatorToken store(final GoogleAuthenticatorToken token) {
        val tokenToSave = token.assignIdIfNecessary();
        facilitator.store(tokenToSave);
        return (GoogleAuthenticatorToken) tokenToSave;
    }

    @Override
    public GoogleAuthenticatorToken get(final String uid, final Integer otp) {
        return facilitator.find(uid, otp);
    }

    @Override
    public void remove(final String uid, final Integer otp) {
        facilitator.remove(uid, otp);
    }

    @Override
    public void remove(final String uid) {
        facilitator.remove(uid);
    }

    @Override
    public void remove(final Integer otp) {
        facilitator.remove(otp);
    }

    @Override
    public void removeAll() {
        facilitator.removeAll();
    }

    @Override
    public long count(final String uid) {
        return facilitator.count(uid);
    }

    @Override
    public long count() {
        return facilitator.count();
    }

    @Override
    protected void cleanInternal() {
        val time = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(this.expireTokensInSeconds);
        facilitator.removeFrom(time);
    }
}
