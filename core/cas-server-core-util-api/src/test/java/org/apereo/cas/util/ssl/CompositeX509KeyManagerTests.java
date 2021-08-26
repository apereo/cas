package org.apereo.cas.util.ssl;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.KeyStore;
import java.security.Principal;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CompositeX509KeyManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("X509")
public class CompositeX509KeyManagerTests {
    @Test
    public void verifyOperation() throws Exception {
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        val ks = KeyStore.getInstance("JKS");
        ks.load(null, "changeit".toCharArray());
        kmf.init(ks, "changeit".toCharArray());
        val km = kmf.getKeyManagers();

        val managers = Arrays.stream(km)
            .filter(tm -> tm instanceof X509KeyManager)
            .map(X509KeyManager.class::cast)
            .collect(Collectors.toList());

        val input = new CompositeX509KeyManager(managers);
        assertNull(input.chooseClientAlias(new String[]{"any"}, new Principal[]{}, mock(Socket.class)));
        assertNull(input.chooseServerAlias("any", new Principal[]{}, mock(Socket.class)));
        assertNull(input.getCertificateChain("cas"));
        assertEquals(0, input.getClientAliases("cas", new Principal[]{}).length);
        assertEquals(0, input.getServerAliases("cas", new Principal[]{}).length);

    }
}
