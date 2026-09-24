package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.core.client.BaseClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BaseDelegatedClientFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
@ExtendWith(CasTestExtension.class)
public abstract class BaseDelegatedClientFactoryTests {
    @Autowired
    @Qualifier(CasSSLContext.BEAN_NAME)
    protected CasSSLContext casSslContext;

    @Autowired
    @Qualifier("pac4jDelegatedClientFactory")
    protected DelegatedIdentityProviderFactory delegatedIdentityProviderFactory;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    /**
     * Concurrent callers must all see the same identity providers. A caller handed a different set
     * from its peers is the shape of the defect this guards: an empty list reads downstream as
     * "no identity providers are configured" rather than as a failure.
     *
     * @throws Exception in case the concurrent builds cannot be started or joined
     */
    @Test
    void verifyConcurrentBuildsAgree() throws Exception {
        val sizes = new HashSet<Integer>();
        for (val clients : buildConcurrently(delegatedIdentityProviderFactory)) {
            assertNotNull(clients);
            sizes.add(clients.size());
        }
        assertEquals(1, sizes.size(), () -> "Concurrent builds disagreed on the identity providers: " + sizes);
    }

    protected static List<List<BaseClient>> buildConcurrently(final DelegatedIdentityProviderFactory factory) throws Exception {
        val startGate = new CountDownLatch(1);
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val tasks = IntStream.rangeClosed(1, 4)
                .mapToObj(__ -> executor.submit(() -> {
                    startGate.await();
                    return factory.build();
                }))
                .toList();
            startGate.countDown();
            val results = new ArrayList<List<BaseClient>>();
            for (val task : tasks) {
                results.add(task.get(60, TimeUnit.SECONDS));
            }
            return results;
        }
    }
}

