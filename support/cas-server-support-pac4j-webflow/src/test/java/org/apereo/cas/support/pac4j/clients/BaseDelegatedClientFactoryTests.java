package org.apereo.cas.support.pac4j.clients;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
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

    /**
     * Callers of the factory treat a built provider list as non-null, so concurrent callers that
     * arrive while a build is already in flight must wait for it rather than be handed nothing.
     *
     * @throws Exception in case the concurrent builds cannot be started or joined
     */
    @Test
    void verifyConcurrentBuildsNeverYieldNull() throws Exception {
        val startGate = new CountDownLatch(1);
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val tasks = IntStream.rangeClosed(1, 4)
                .mapToObj(__ -> executor.submit(() -> {
                    startGate.await();
                    return delegatedIdentityProviderFactory.build();
                }))
                .toList();
            startGate.countDown();
            for (val task : tasks) {
                assertNotNull(task.get(60, TimeUnit.SECONDS));
            }
        }
    }
}

