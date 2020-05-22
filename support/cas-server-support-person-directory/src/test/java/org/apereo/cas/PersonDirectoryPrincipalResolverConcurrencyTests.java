package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasPersonDirectoryConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test concurrency of PersonDirectoryPrincipalResolver.
 * Use CachingPersonAttributeDao -> MergingPersonAttributeDao -> with multiple PersonAttributeDao implementations.
 *
 * @since 6.2
 */
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    CasPersonDirectoryConfiguration.class,
    CasCoreUtilConfiguration.class
}, properties = {
    "cas.authn.attributeRepository.stub.attributes.uid=cas",
    "cas.authn.attributeRepository.stub.attributes.givenName=apereo-cas",
    "cas.authn.attributeRepository.stub.attributes.phone=123456789",

    "cas.authn.attributeRepository.json[0].location=classpath:/json-attribute-repository.json",
    "cas.authn.attributeRepository.json[0].order=1",

    "cas.authn.attributeRepository.groovy[0].location=classpath:/GroovyAttributeDao.groovy",
    "cas.authn.attributeRepository.groovy[0].order=2",

    "cas.authn.attributeRepository.aggregation=merge",
    "cas.authn.attributeRepository.merger=multivalued"
})
@Tag("Simple")
public class PersonDirectoryPrincipalResolverConcurrencyTests {

    private static final int NUM_USERS = 100;

    private static final int EXECUTIONS_PER_USER = 1000;

    @Autowired
    @Qualifier("attributeRepository")
    private IPersonAttributeDao attributeRepository;

    @Autowired
    private CasConfigurationProperties casProperties;

    private PrincipalResolver personDirectoryResolver;

    /**
     * Assert a list of runnables can run in parallel without any concurrency related exceptions.
     * Use CountDownLatch to start all threads at same time and wait for them to finish.
     *
     * @param message           error message
     * @param runnables         list of runnables
     * @param maxTimeoutSeconds timeout for test completion
     * @throws InterruptedException interruption
     */
    private static void assertConcurrent(final String message, final List<? extends Runnable> runnables,
                                         final int maxTimeoutSeconds) throws InterruptedException {
        val numThreads = runnables.size();
        val exceptions = Collections.synchronizedList(new ArrayList<>());
        val threadPool = Executors.newFixedThreadPool(numThreads);
        try {
            val allExecutorThreadsReady = new CountDownLatch(numThreads);
            val afterInitBlocker = new CountDownLatch(1);
            val allDone = new CountDownLatch(numThreads);
            for (val submittedTestRunnable : runnables) {
                threadPool.execute(() -> {
                    allExecutorThreadsReady.countDown();
                    try {
                        afterInitBlocker.await();
                        submittedTestRunnable.run();
                    } catch (final Throwable e) {
                        exceptions.add(e);
                    } finally {
                        allDone.countDown();
                    }
                });
            }
            assertTrue(allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS),
                "Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent");
            afterInitBlocker.countDown();
            assertTrue(allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS), message + " timeout! More than " + maxTimeoutSeconds + " seconds");
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(exceptions.isEmpty(), message + " failed with exception(s)" + exceptions);
    }

    @BeforeEach
    protected void setUp() {
        this.personDirectoryResolver = CoreAuthenticationUtils.newPersonDirectoryPrincipalResolver(
            PrincipalFactoryUtils.newPrincipalFactory(),
            attributeRepository, casProperties.getPersonDirectory()
        );
    }

    /**
     * Create a PersonAttrGetter for each user and run them in parallel
     *
     * @throws Exception concurrency assertion failed
     */
    @Test
    public void validatePersonDirConcurrency() throws Exception {
        val userList = new ArrayList<String>();
        for (int i = 0; i < NUM_USERS; i++) {
            userList.add("user_" + i);
        }

        val runnables = new ArrayList<Runnable>();
        for (val user : userList) {
            val personAttrGetter = new PersonAttrGetter(personDirectoryResolver, user);
            runnables.add(personAttrGetter);
        }
        assertConcurrent("Getting persons", runnables, 600);
    }

    @Getter
    @Slf4j
    @RequiredArgsConstructor
    private static class PersonAttrGetter implements Runnable {

        private final PrincipalResolver personDirectoryResolver;

        private final String username;

        @Override
        public void run() {
            val upc = new UsernamePasswordCredential(username, "password");
            for (int i = 0; i < EXECUTIONS_PER_USER; i++) {
                try {
                    val person = this.personDirectoryResolver.resolve(upc);
                    val attributes = person.getAttributes();
                    assertEquals(username, person.getId());
                    LOGGER.debug("Fetched person: [{}] [{}], last-name [{}]", attributes.get("uid"),
                        attributes.get("lastName"), attributes.get("nickname"));
                } catch (final Exception e) {
                    LOGGER.warn("Error getting person: {}", e.getMessage(), e);
                    throw e;
                }
            }
        }
    }
}
