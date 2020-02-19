package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.util.CollectionUtils;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.support.CachingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.ComplexStubPersonAttributeDao;
import org.apereo.services.persondir.support.MergingPersonAttributeDaoImpl;
import org.apereo.services.persondir.support.ScriptEnginePersonAttributeDao;
import org.apereo.services.persondir.support.SimpleUsernameAttributeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test concurrency of PersonDirectoryPrincipalResolver.
 * Use CachingPersonAttributeDao -> MergingPersonAttributeDao -> with multiple PersonAttributeDao implementations.
 * @since 6.2
 */
@Slf4j
public class PersonDirectoryPrincipalResolverConcurrencyTests {

    private static final String DEFAULT_ATTR = "uid";
    private static final int NUM_USERS = 100;
    private static final int EXECUTIONS_PER_USER = 1000;

    private PersonDirectoryPrincipalResolver personDirectoryResolver;
    private List<String> userList = new ArrayList<>();

    @BeforeEach
    protected void setUp() throws Exception {
        val attributeProvider = new SimpleUsernameAttributeProvider(DEFAULT_ATTR);
        val stubDao = new ComplexStubPersonAttributeDao();
        val scriptFile = "ConcurrencyPersonAttributeDao.groovy";
        val scriptDao = new ScriptEnginePersonAttributeDao(scriptFile, ScriptEnginePersonAttributeDao.getScriptEngineName(scriptFile), attributeProvider);
        val mergingDao = new MergingPersonAttributeDaoImpl();
        val stubDaoBackingMap = new HashMap<String, Map<String, List<Object>>>();

        for (int i=0; i < NUM_USERS; i++) {
            userList.add("user_" + i);
        }

        userList.forEach(u -> {
            val user = new HashMap<String, List<Object>>();
            user.put("uid", CollectionUtils.toCollection(u, ArrayList.class));
            user.put("phone", CollectionUtils.toCollection("777-7777", ArrayList.class));
            user.put("displayName", CollectionUtils.toCollection("Display " + u, ArrayList.class));
            stubDaoBackingMap.put(u, user);
            LOGGER.debug("Creating user: {}", user.get("uid"));
        });

        stubDao.setBackingMap(stubDaoBackingMap);
        stubDao.setUsernameAttributeProvider(attributeProvider);

        val attributeSources = new ArrayList<IPersonAttributeDao>();
        attributeSources.add(stubDao);
        attributeSources.add(scriptDao);
        mergingDao.setPersonAttributeDaos(attributeSources);

        val cachePersonAttrDao = new CachingPersonAttributeDaoImpl();
        cachePersonAttrDao.setCacheNullResults(false);
        val graphs = Caffeine.newBuilder()
            .maximumSize(25)
            .expireAfterWrite(Duration.ofSeconds(5L))
            .build();
        cachePersonAttrDao.setUserInfoCache((Map) graphs.asMap());

        cachePersonAttrDao.setCachedPersonAttributesDao(mergingDao);
        cachePersonAttrDao.setUsernameAttributeProvider(new SimpleUsernameAttributeProvider(DEFAULT_ATTR));
        cachePersonAttrDao.afterPropertiesSet();
        this.personDirectoryResolver = new PersonDirectoryPrincipalResolver(cachePersonAttrDao, "uid");
    }

    @Getter
    @Slf4j
    public static class PersonAttrGetter implements Runnable {

        private final PersonDirectoryPrincipalResolver personDirectoryResolver;
        private final String username;
        private final int executions;

        public PersonAttrGetter(final PersonDirectoryPrincipalResolver personDirectoryResolver, final String username, final int executions) {
            this.personDirectoryResolver = personDirectoryResolver;
            this.username = username;
            this.executions = executions;
        }

        @Override
        public void run() {
            for (int i = 0; i < executions; i++) {
                try {
                    val person = this.personDirectoryResolver.retrievePersonAttributes(username, new UsernamePasswordCredential(username, "password"));
                    assertEquals(username, person.get("uid").get(0));
                    LOGGER.debug("Got person: {} {}", person.get("uid"), person.get("displayName"));
                    LOGGER.debug("Person: {} likes {}", person.get("uid"), person.get("likes"));
                } catch (final Exception e) {
                    LOGGER.error("Error  getting person: {}", e.getMessage(), e);
                    throw e;
                }
            }
        }
    }

    /**
     * Create a PersonAttrGetter for each user and run them in parallel
     * @throws Exception concurrency assertion failed
     */
    @Test
    public void validatePersonDirConcurrency() throws Exception {
        val runnables = new ArrayList<Runnable>();
        for (val user: userList) {
            val personAttrGetter = new PersonAttrGetter(personDirectoryResolver, user, EXECUTIONS_PER_USER);
            runnables.add(personAttrGetter);
        }
        assertConcurrent("Getting persons", runnables, 600);
    }

    /**
     * Assert a list of runnables can run in parallel without any concurrency related exceptions.
     * Use CountDownLatch to start all threads at same time and wait for them to finish.
     * @param message error message
     * @param runnables list of runnables
     * @param maxTimeoutSeconds timeout for test completion
     * @throws InterruptedException interruption
     */
    public static void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds) throws InterruptedException {
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
            assertTrue(allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS), message +" timeout! More than" + maxTimeoutSeconds + "seconds");
        } finally {
            threadPool.shutdownNow();
        }
        assertTrue(exceptions.isEmpty(), message + "failed with exception(s)" + exceptions);
    }
}
