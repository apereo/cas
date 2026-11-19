package org.apereo.cas.config;

import module java.base;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.GetOption;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link EtcdPropertySourceTests}.
 * <p>
 * Spring Cloud closes the bootstrap context that located the property source once a refresh is done, and with it any
 * client the context created. The property source must therefore keep the values it read and open a client of its own
 * for every operation that talks to etcd.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("CasConfiguration")
class EtcdPropertySourceTests {
    private static final String SERVER_NAME = "/cas/config/default/cas.server.name";

    @Test
    void verifyPropertiesOutliveTheirClient() {
        val clients = new ArrayList<Client>();
        val source = new EtcdPropertySource("etcd", () -> newClient(clients, Map.of(SERVER_NAME, "https://apereo.org")), List.of());
        assertArrayEquals(new String[]{"cas.server.name"}, source.getPropertyNames());
        assertEquals("https://apereo.org", source.getProperty("cas.server.name"));
        assertNull(source.getProperty("cas.server.prefix"));
        assertEquals(1, clients.size());
        val client = clients.getFirst();
        verify(client).close();
        verify(client.getKVClient(), never()).get(any(ByteSequence.class));
    }

    @Test
    void verifyEachOperationUsesAClientOfItsOwn() {
        val clients = new ArrayList<Client>();
        val source = new EtcdPropertySource("etcd", () -> newClient(clients, Map.of()), List.of());
        source.setProperty("cas.server.name", "https://apereo.org");
        assertEquals("https://apereo.org", source.getProperty("cas.server.name"));
        source.removeProperty("cas.server.name");
        assertNull(source.getProperty("cas.server.name"));
        source.refresh();
        assertEquals(4, clients.size());
        val key = ByteSequence.from(SERVER_NAME, StandardCharsets.UTF_8);
        verify(clients.get(1).getKVClient()).put(key, ByteSequence.from("https://apereo.org", StandardCharsets.UTF_8));
        verify(clients.get(2).getKVClient()).delete(key);
        clients.forEach(client -> verify(client).close());
    }

    private static Client newClient(final List<Client> clients, final Map<String, String> values) {
        val keyValues = values.entrySet().stream().map(entry -> {
            val keyValue = mock(KeyValue.class);
            when(keyValue.getKey()).thenReturn(ByteSequence.from(entry.getKey(), StandardCharsets.UTF_8));
            when(keyValue.getValue()).thenReturn(ByteSequence.from(entry.getValue(), StandardCharsets.UTF_8));
            return keyValue;
        }).toList();
        val response = mock(GetResponse.class);
        when(response.getKvs()).thenReturn(keyValues);
        val kv = mock(KV.class);
        when(kv.get(any(ByteSequence.class), any(GetOption.class))).thenReturn(CompletableFuture.completedFuture(response));
        when(kv.get(any(ByteSequence.class))).thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Client is closed")));
        when(kv.put(any(ByteSequence.class), any(ByteSequence.class))).thenReturn(CompletableFuture.completedFuture(mock(PutResponse.class)));
        when(kv.delete(any(ByteSequence.class))).thenReturn(CompletableFuture.completedFuture(mock(DeleteResponse.class)));
        val client = mock(Client.class);
        when(client.getKVClient()).thenReturn(kv);
        clients.add(client);
        return client;
    }
}
