package org.apereo.cas.support.pac4j;

import org.apereo.cas.support.pac4j.authentication.DelegatedClientFactory;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.cas.client.CasClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link RefreshableDelegatedClientsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("Delegation")
public class RefreshableDelegatedClientsTests {
    @Test
    public void verifyOperation() {
        val delegatedFactory = mock(DelegatedClientFactory.class);
        when(delegatedFactory.build()).thenReturn(List.of());
        val refreshableClients = new RefreshableDelegatedClients("http://localhost:8080/cas", delegatedFactory);
        assertTrue(refreshableClients.findAllClients().isEmpty());
        val client = new CasClient();
        when(delegatedFactory.build()).thenReturn(List.of(client));
        assertFalse(refreshableClients.findAllClients().isEmpty());
        assertTrue(refreshableClients.findClient(client.getName()).isPresent());

    }
}
