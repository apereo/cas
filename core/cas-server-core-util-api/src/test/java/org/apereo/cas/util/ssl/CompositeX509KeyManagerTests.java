package org.apereo.cas.util.ssl;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CompositeX509KeyManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("X509")
class CompositeX509KeyManagerTests {
    @Test
    void verifyOperation() throws Throwable {
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        val ks = KeyStore.getInstance("JKS");
        ks.load(null, "changeit".toCharArray());
        kmf.init(ks, "changeit".toCharArray());
        val km = kmf.getKeyManagers();

        val managers = Arrays.stream(km)
            .filter(X509KeyManager.class::isInstance)
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
