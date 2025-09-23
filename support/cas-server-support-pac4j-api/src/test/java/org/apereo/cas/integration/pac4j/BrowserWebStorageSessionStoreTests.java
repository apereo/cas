package org.apereo.cas.integration.pac4j;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.BrowserWebStorageSessionStore;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
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
@ExtendWith(CasTestExtension.class)
class BrowserWebStorageSessionStoreTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(false).build().toObjectMapper();


    @Autowired
    @Qualifier(CipherExecutor.BEAN_NAME_WEBFLOW_CIPHER_EXECUTOR)
    private CipherExecutor webflowCipherExecutor;

    @Test
    void verifyOperation() throws Throwable {
        val store = new BrowserWebStorageSessionStore(webflowCipherExecutor, "ContextKey");
        val request = new MockHttpServletRequest();
        val ctx = new JEEContext(request, new MockHttpServletResponse());
        store.set(ctx, "key1", "value1");
        store.set(ctx, "key2", List.of("HelloWorld"));
        store.set(ctx, "key3", 1234567);
        store.set(ctx, "dummy", new Dummy());
        val session = store.getTrackableSession(ctx);
        assertTrue(session.isPresent());
        store.renewSession(ctx);
        val storage = (BrowserStorage) session.get();
        store.buildFromTrackableSession(ctx, MAPPER.writeValueAsString(Map.of(storage.getContext(), storage.getPayload())));
        assertTrue(store.get(ctx, "key1").isPresent());
        assertTrue(store.get(ctx, "key2").isPresent());
        assertTrue(store.get(ctx, "key3").isPresent());
        assertTrue(store.get(ctx, "dummy").isPresent());
    }

    private static final class Dummy implements Serializable {
        @Serial
        private static final long serialVersionUID = 7015295901443767970L;
    }
}
