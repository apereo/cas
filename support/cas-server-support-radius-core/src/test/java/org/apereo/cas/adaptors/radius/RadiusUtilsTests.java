package org.apereo.cas.adaptors.radius;

import org.apereo.cas.util.CollectionUtils;

import net.jradius.dictionary.Attr_ClientId;
import net.jradius.packet.attribute.RadiusAttribute;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.security.auth.login.FailedLoginException;
import java.util.Map;
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
        final RadiusServer server = mock(RadiusServer.class);
        final RadiusAttribute attribute = new Attr_ClientId("client_id");
        final RadiusResponse response = new RadiusResponse(100, 100, CollectionUtils.wrapList(attribute));
        when(server.authenticate(anyString(), anyString(), anyObject())).thenReturn(response);
        final Pair<Boolean, Optional<Map<String, Object>>> result = RadiusUtils.authenticate("casuser", "Mellon",
            Optional.empty(), CollectionUtils.wrapList(server), true, false);
        assertTrue(result.getKey());
        assertTrue(result.getRight().isPresent());
    }

    @Test
    public void verifyActionFails() throws Exception {
        final RadiusServer server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString(), anyObject())).thenReturn(null);
        thrown.expect(FailedLoginException.class);
        RadiusUtils.authenticate("casuser", "Mellon",
            Optional.empty(), CollectionUtils.wrapList(server), false, false);
    }

    @Test
    public void verifyActionFailsWithException() throws Exception {
        final RadiusServer server = mock(RadiusServer.class);
        when(server.authenticate(anyString(), anyString(), anyObject())).thenThrow(RuntimeException.class);
        thrown.expect(RuntimeException.class);
        RadiusUtils.authenticate("casuser", "Mellon", Optional.empty(),
            CollectionUtils.wrapList(server), false, false);
    }
}
