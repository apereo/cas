package org.apereo.cas.adaptors.radius;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.jradius.dictionary.Attr_ClientId;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.FailedLoginException;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class RadiusUtilsTests {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void verifyActionPasses() throws Exception {
        val server = mock(RadiusServer.class);
        val attribute = new Attr_ClientId("client_id");
        val response = new CasRadiusResponse(100, 100, CollectionUtils.wrapList(attribute));
        when(server.authenticate(anyString(), anyString(), any())).thenReturn(response);
        val result = RadiusUtils.authenticate("casuser", "Mellon",
            CollectionUtils.wrapList(server), true, false, Optional.empty());
        assertTrue(result.getKey());
        assertTrue(result.getRight().isPresent());
    }

    @Test
    public void verifyActionFails() throws Exception {
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenReturn(null);
        thrown.expect(FailedLoginException.class);
        RadiusUtils.authenticate("casuser", "Mellon",
            CollectionUtils.wrapList(server), false, false, Optional.empty());
    }

    @Test
    public void verifyActionFailsWithException() throws Exception {
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenThrow(FailedLoginException.class);
        thrown.expect(FailedLoginException.class);
        RadiusUtils.authenticate("casuser", "Mellon",
            CollectionUtils.wrapList(server), false, false, Optional.empty());
    }
}
