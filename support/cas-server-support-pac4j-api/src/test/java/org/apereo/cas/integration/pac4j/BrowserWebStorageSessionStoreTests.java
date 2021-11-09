package org.apereo.cas.integration.pac4j;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.web.BrowserSessionStorage;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.Serializable;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BrowserWebStorageSessionStoreTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseSessionStoreTests.SharedTestConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Web")
public class BrowserWebStorageSessionStoreTests {
    @Autowired
    @Qualifier("webflowCipherExecutor")
    private CipherExecutor webflowCipherExecutor;

    @Test
    public void verifyOperation() {
        val store = new BrowserWebStorageSessionStore(webflowCipherExecutor);
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        store.set(ctx, "key1", "value1");
        store.set(ctx, "key2", List.of("HelloWorld"));
        store.set(ctx, "key3", 1234567);
        store.set(ctx, "dummy", new Dummy());
        var session = store.getTrackableSession(ctx);
        assertTrue(session.isPresent());

        store.renewSession(ctx);
        val trackableSession = (BrowserSessionStorage) session.get();
        store.buildFromTrackableSession(ctx, trackableSession.getPayload());
        assertTrue(store.get(ctx, "key1").isPresent());
        assertTrue(store.get(ctx, "key2").isPresent());
        assertTrue(store.get(ctx, "key3").isPresent());
        assertTrue(store.get(ctx, "dummy").isPresent());
    }

    private static class Dummy implements Serializable {
        private static final long serialVersionUID = 7015295901443767970L;
    }
}
