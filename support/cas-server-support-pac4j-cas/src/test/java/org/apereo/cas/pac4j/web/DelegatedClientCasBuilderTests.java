package org.apereo.cas.pac4j.web;

import module java.base;
import org.apereo.cas.config.CasDelegatedAuthenticationCasAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.authentication.clients.BaseDelegatedIdentityProviderFactory;
import org.apereo.cas.support.pac4j.clients.BaseDelegatedClientFactoryTests;
import org.apereo.cas.test.CasTestExtension;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.BaseClient;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedClientCasBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@ImportAutoConfiguration(CasDelegatedAuthenticationCasAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.authn.pac4j.cas[0].login-url=https://login.example.org/login",
    "cas.authn.pac4j.cas[0].protocol=SAML",
    "cas.authn.pac4j.cas[0].principal-id-attribute=uid",
    "cas.authn.pac4j.cas[0].css-class=cssClass",
    "cas.authn.pac4j.cas[0].display-name=My CAS",
    "cas.authn.pac4j.core.lazy-init=true",
    "cas.custom.properties.delegation-test.enabled=false"
})
class DelegatedClientCasBuilderTests extends BaseDelegatedClientFactoryTests {
    @Test
    void verifyFactoryForCasClientsHavingLoginInDomain() {
        val clients = delegatedIdentityProviderFactory.build();
        assertEquals(1, clients.size());
        val client = (CasClient) clients.getFirst();
        assertEquals("https://login.example.org/", client.getConfiguration().getPrefixUrl());
    }

    /**
     * A caller arriving while a build is in flight must be handed that build's result rather than
     * the cache as it stood before, which on a cold start is empty and reads downstream as
     * "no identity providers are configured". The build is held open by a latch until the second
     * caller has been observed waiting on it.
     *
     * @throws Exception in case the concurrent builds cannot be started or joined
     */
    @Test
    void verifyBuildsWaitForTheBuildInFlight() throws Exception {
        val loading = new CountDownLatch(1);
        val release = new CountDownLatch(1);
        val factory = new BaseDelegatedIdentityProviderFactory(new CasConfigurationProperties(),
            List.of(), casSslContext, Caffeine.newBuilder().build(), applicationContext) {
            @Override
            protected List<BaseClient> load() throws Exception {
                loading.countDown();
                release.await();
                return List.of(mock(BaseClient.class));
            }
        };
        try (val executor = Executors.newVirtualThreadPerTaskExecutor()) {
            val building = executor.submit(factory::build);
            assertTrue(loading.await(30, TimeUnit.SECONDS));
            val queued = executor.submit(factory::build);
            assertThrows(TimeoutException.class, () -> queued.get(250, TimeUnit.MILLISECONDS));
            release.countDown();
            assertEquals(1, building.get(30, TimeUnit.SECONDS).size());
            assertEquals(1, queued.get(30, TimeUnit.SECONDS).size());
        }
    }
}
