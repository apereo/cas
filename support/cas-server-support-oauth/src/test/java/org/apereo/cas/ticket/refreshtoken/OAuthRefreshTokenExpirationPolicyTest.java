package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

/**
 * Adding test to check expired refresh token.
 *
 * @author Phillip Rower
 * @since 5.0.0
 */
public class OAuthRefreshTokenExpirationPolicyTest {
    @Mock
    private Service mockService;

    @Mock
    private Authentication mockAuthentication;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isExpiredTrueTest() {
        final OAuthRefreshTokenExpirationPolicy policy = new OAuthRefreshTokenExpirationPolicy(-1L);
        DefaultRefreshTokenFactory defaultRefreshTokenFactory = new DefaultRefreshTokenFactory();
        defaultRefreshTokenFactory.setExpirationPolicy(policy);
        final RefreshTokenImpl refreshToken = (RefreshTokenImpl) defaultRefreshTokenFactory.create(mockService, mockAuthentication );

        assertNotNull(refreshToken.getCreationTime());
        assertTrue(policy.isExpired(refreshToken));
    }

    @Test
    public void isExpiredFalseTest() {
        final OAuthRefreshTokenExpirationPolicy policy = new OAuthRefreshTokenExpirationPolicy(10L);
        DefaultRefreshTokenFactory defaultRefreshTokenFactory = new DefaultRefreshTokenFactory();
        defaultRefreshTokenFactory.setExpirationPolicy(policy);
        final RefreshTokenImpl refreshToken = (RefreshTokenImpl) defaultRefreshTokenFactory.create(mockService, mockAuthentication );

        assertNotNull(refreshToken.getCreationTime());
        assertFalse(policy.isExpired(refreshToken));
    }

}
