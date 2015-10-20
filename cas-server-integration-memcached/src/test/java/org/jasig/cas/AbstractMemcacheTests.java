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

package org.jasig.cas;

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
import org.slf4j.Logger;

import java.net.Socket;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This is {@link AbstractMemcacheTests}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public abstract class AbstractMemcacheTests {

    private static final int PORT = 11211;

    private static MemcachedExecutable MEMCACHED_EXECUTABLE;
    private static MemcachedProcess MEMCACHED;

    protected final Logger logger = getLogger(this.getClass());

    public static void bootstrap() {
        try {
            final MemcachedStarter runtime = MemcachedStarter.getInstance(
                    new CasRuntimeConfigBuilder().defaults(Command.MemcacheD).build());
            MEMCACHED_EXECUTABLE = runtime.prepare(new MemcachedConfig(Version.V1_4_22, PORT));
            MEMCACHED = MEMCACHED_EXECUTABLE.start();
        } catch (final Exception e) {
            getLogger(AbstractMemcacheTests.class).warn("Aborting since no memcached server could be started.", e);
        }
    }

    public static void shutdown() {
        if (MEMCACHED != null && MEMCACHED.isProcessRunning()) {
            MEMCACHED.stop();
        }
        if (MEMCACHED_EXECUTABLE != null) {
            MEMCACHED_EXECUTABLE.stop();
        }
    }

    public boolean isMemcachedListening() {
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
