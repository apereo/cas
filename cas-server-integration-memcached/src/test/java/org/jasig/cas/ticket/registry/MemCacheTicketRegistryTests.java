/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry;

import de.flapdoodle.embed.memcached.Command;
import de.flapdoodle.embed.memcached.MemcachedExecutable;
import de.flapdoodle.embed.memcached.MemcachedProcess;
import de.flapdoodle.embed.memcached.MemcachedStarter;
import de.flapdoodle.embed.memcached.config.ArtifactStoreBuilder;
import de.flapdoodle.embed.memcached.config.DownloadConfigBuilder;
import de.flapdoodle.embed.memcached.config.MemcachedConfig;
import de.flapdoodle.embed.memcached.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.memcached.distribution.Version;
import de.flapdoodle.embed.process.config.store.IDownloadConfig;
import de.flapdoodle.embed.process.io.progress.StandardConsoleProgressListener;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.authentication.principal.Service;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.*;

/**
 * Unit test for MemCacheTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
public class MemCacheTicketRegistryTests {
    private static final Logger LOGGER = getLogger(MemCacheTicketRegistryTests.class);
    private static final int PORT = 11211;

    private static MemcachedExecutable MEMCACHED_EXECUTABLE;
    private static MemcachedProcess MEMCACHED;

    private MemCacheTicketRegistry registry;

    private final String registryBean;

    private final boolean binaryProtocol;

    public MemCacheTicketRegistryTests(final String beanName, final boolean binary) {
        registryBean = beanName;
        binaryProtocol = binary;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getTestParameters() throws Exception {
        return Arrays.asList(new Object[] {"testCase1", false}, new Object[] {"testCase2", true});
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        try {
            final MemcachedStarter runtime = MemcachedStarter.getInstance(
                    new CasRuntimeConfigBuilder().defaults(Command.MemcacheD).build());
            MEMCACHED_EXECUTABLE = runtime.prepare(new MemcachedConfig(Version.V1_4_22, PORT));
            MEMCACHED = MEMCACHED_EXECUTABLE.start();
        } catch (final Exception e) {
            LOGGER.warn("Aborting since no memcached server could be started.", e);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
       if (MEMCACHED != null && MEMCACHED.isProcessRunning()) {
           MEMCACHED.stop();
       }
        if (MEMCACHED_EXECUTABLE != null) {
            MEMCACHED_EXECUTABLE.stop();
        }
    }

    @Before
    public void setUp() throws IOException {

        // Abort tests if there is no memcached server available on localhost:11211.
        final boolean environmentOk = isMemcachedListening();
        if (!environmentOk) {
            LOGGER.warn("Aborting test since no memcached server is available on localhost.");
        }
        Assume.assumeTrue(environmentOk);
        final ApplicationContext context = new ClassPathXmlApplicationContext("/ticketRegistry-test.xml");
        registry = context.getBean(registryBean, MemCacheTicketRegistry.class);
    }

    @Test
    public void verifyWriteGetDelete() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-crud";
        final ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        final ServiceTicket ticketFromRegistry = (ServiceTicket) registry.getTicket(id);
        Assert.assertNotNull(ticketFromRegistry);
        Assert.assertEquals(id, ticketFromRegistry.getId());
        registry.deleteTicket(id);
        Assert.assertNull(registry.getTicket(id));
    }

    @Test
    public void verifyExpiration() throws Exception {
        final String id = "ST-1234567890ABCDEFGHIJKL-exp";
        final ServiceTicket ticket = mock(ServiceTicket.class, withSettings().serializable());
        when(ticket.getId()).thenReturn(id);
        registry.addTicket(ticket);
        Assert.assertNotNull(registry.getTicket(id, ServiceTicket.class));
        // Sleep a little longer than service ticket expiry defined in Spring context
        Thread.sleep(2100);
        Assert.assertNull(registry.getTicket(id, ServiceTicket.class));
    }

    @Test
    public void verifyDeleteTicketWithChildren() throws Exception {
        this.registry.addTicket(new TicketGrantingTicketImpl(
                "TGT", TestUtils.getAuthentication(), new NeverExpiresExpirationPolicy()));
        final TicketGrantingTicket tgt = this.registry.getTicket(
                "TGT", TicketGrantingTicket.class);

        final Service service = TestUtils.getService("TGT_DELETE_TEST");

        final ServiceTicket st1 = tgt.grantServiceTicket(
                "ST1", service, new NeverExpiresExpirationPolicy(), true);
        final ServiceTicket st2 = tgt.grantServiceTicket(
                "ST2", service, new NeverExpiresExpirationPolicy(), true);
        final ServiceTicket st3 = tgt.grantServiceTicket(
                "ST3", service, new NeverExpiresExpirationPolicy(), true);

        this.registry.addTicket(st1);
        this.registry.addTicket(st2);
        this.registry.addTicket(st3);

        assertNotNull(this.registry.getTicket("TGT", TicketGrantingTicket.class));
        assertNotNull(this.registry.getTicket("ST1", ServiceTicket.class));
        assertNotNull(this.registry.getTicket("ST2", ServiceTicket.class));
        assertNotNull(this.registry.getTicket("ST3", ServiceTicket.class));

        this.registry.deleteTicket(tgt.getId());

        assertNull(this.registry.getTicket("TGT", TicketGrantingTicket.class));
        assertNull(this.registry.getTicket("ST1", ServiceTicket.class));
        assertNull(this.registry.getTicket("ST2", ServiceTicket.class));
        assertNull(this.registry.getTicket("ST3", ServiceTicket.class));
    }

    private boolean isMemcachedListening() {
        try (final Socket socket = new Socket("127.0.0.1", PORT)) {
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private static class CasRuntimeConfigBuilder extends RuntimeConfigBuilder {
        @Override
        public RuntimeConfigBuilder defaults(final Command command) {
            final RuntimeConfigBuilder builder = super.defaults(command);

            final IDownloadConfig downloadConfig = new CasDownloadConfigBuilder()
                    .defaultsForCommand(command)
                    .progressListener(new StandardConsoleProgressListener())
                    .build();
            this.artifactStore().overwriteDefault(new ArtifactStoreBuilder()
                    .defaults(command).download(downloadConfig).build());
            return builder;
        }
    }

    /**
     * Download an embedded memcached instance based on environment.
     */
    private static class CasDownloadConfigBuilder extends DownloadConfigBuilder {
        @Override
        public DownloadConfigBuilder defaults() {
            final DownloadConfigBuilder bldr = super.defaults();
            bldr.downloadPath("http://heli0s.darktech.org/memcached/");
            return bldr;
        }
    }
}
