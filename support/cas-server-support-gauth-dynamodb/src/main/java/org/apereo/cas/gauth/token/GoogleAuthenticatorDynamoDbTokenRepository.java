package org.apereo.cas.gauth.token;

import org.apereo.cas.authentication.OneTimeToken;
import org.apereo.cas.otp.repository.token.BaseOneTimeTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * This is {@link GoogleAuthenticatorDynamoDbTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorDynamoDbTokenRepository extends BaseOneTimeTokenRepository {
    private final GoogleAuthenticatorDynamoDbTokenRepositoryFacilitator facilitator;

    private final long expireTokensInSeconds;

    @Override
    public void store(final OneTimeToken token) {
        facilitator.store(token);
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
