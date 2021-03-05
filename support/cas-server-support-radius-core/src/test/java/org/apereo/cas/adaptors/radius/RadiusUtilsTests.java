package org.apereo.cas.adaptors.radius;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import net.jradius.dictionary.Attr_ClientId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.security.auth.login.FailedLoginException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RadiusUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Radius")
public class RadiusUtilsTests {
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
    public void verifyActionFailsWithFailOver() throws Exception {
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenReturn(null);
        val result = RadiusUtils.authenticate("casuser", "Mellon", CollectionUtils.wrapList(server), true, false, Optional.empty());
        assertFalse(result.getKey());
    }

    @Test
    public void verifyActionFails() throws Exception {
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenReturn(null);
        assertThrows(FailedLoginException.class,
            () -> RadiusUtils.authenticate("casuser", "Mellon", CollectionUtils.wrapList(server), false, false, Optional.empty()));
    }

    @Test
    public void verifyActionFailsWithException() throws Exception {
        val server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString())).thenThrow(FailedLoginException.class);
        assertThrows(FailedLoginException.class,
            () -> RadiusUtils.authenticate("casuser", "Mellon", CollectionUtils.wrapList(server), false, false, Optional.empty()));
    }
}
