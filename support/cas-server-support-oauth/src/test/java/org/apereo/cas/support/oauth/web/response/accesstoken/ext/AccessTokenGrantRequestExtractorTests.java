package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.AbstractOAuth20Tests;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AccessTokenGrantRequestExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("OAuth")
public class AccessTokenGrantRequestExtractorTests extends AbstractOAuth20Tests {
    @Test
    public void verifyOperation() {
        val ext = mock(AccessTokenGrantRequestExtractor.class);
        when(ext.requestMustBeAuthenticated()).thenCallRealMethod();
        assertFalse(ext.requestMustBeAuthenticated());
    }
}
