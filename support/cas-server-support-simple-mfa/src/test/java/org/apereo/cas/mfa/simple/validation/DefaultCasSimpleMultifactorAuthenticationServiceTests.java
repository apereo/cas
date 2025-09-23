package org.apereo.cas.mfa.simple.validation;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mfa.simple.BaseCasSimpleMultifactorAuthenticationTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@SpringBootTest(classes = {
    DefaultCasSimpleMultifactorAuthenticationServiceTests.DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration.class,
    BaseCasSimpleMultifactorAuthenticationTests.SharedTestConfiguration.class
}, properties = "cas.authn.mfa.simple.token.core.token-length=4")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
@Slf4j
class DefaultCasSimpleMultifactorAuthenticationServiceTests {
    @Autowired
    @Qualifier(CasSimpleMultifactorAuthenticationService.BEAN_NAME)
    private CasSimpleMultifactorAuthenticationService multifactorAuthenticationService;

    @Test
    void verifyOperation() {
        val attributes = CollectionUtils.<String, Object>wrap("email", "casuser@example.org");
        assertDoesNotThrow(() -> multifactorAuthenticationService.update(RegisteredServiceTestUtils.getPrincipal(), attributes));
        assertTrue(attributes.containsKey("updated"));
    }

    @Test
    void verifyGenerateTicketsUnderLoad() throws Throwable {
        val testHasFailed = new AtomicBoolean();
        val threads = new ArrayList<Thread>();
        IntStream.range(0, 500).forEach(i -> {
            val thread = Thread.ofVirtual().unstarted(Unchecked.runnable(() ->
                multifactorAuthenticationService.generate(RegisteredServiceTestUtils.getPrincipal(), RegisteredServiceTestUtils.getService())));
            thread.setUncaughtExceptionHandler((t, e) -> {
                LOGGER.error(e.getMessage(), e);
                testHasFailed.set(true);
            });
            thread.setName("Thread-" + i);
            threads.add(thread);
            thread.start();
        });
        for (val thread : threads) {
            try {
                thread.join();
            } catch (final InterruptedException e) {
                fail(e);
            }
        }
        if (testHasFailed.get()) {
            fail("Test failed");
        }
    }

    @TestConfiguration(value = "DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class DefaultCasSimpleMultifactorAuthenticationServiceTestConfiguration {
        @Bean
        public CasSimpleMultifactorAuthenticationAccountService casSimpleMultifactorAuthenticationAccountService() {
            return (principal, attributes) -> {
                assertTrue(attributes.containsKey("email"));
                attributes.put("updated", true);
            };
        }
    }

}
