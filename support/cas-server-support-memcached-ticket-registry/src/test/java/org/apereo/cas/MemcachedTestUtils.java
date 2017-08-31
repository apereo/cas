package org.apereo.cas;

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

import java.net.Socket;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * This is {@link MemcachedTestUtils}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public final class MemcachedTestUtils {

    private static final int PORT = 14938;

    private static MemcachedExecutable MEMCACHED_EXECUTABLE;
    private static MemcachedProcess MEMCACHED;

    private MemcachedTestUtils() {
    }

    public static void bootstrap() {
        try {
            final MemcachedStarter runtime = MemcachedStarter.getInstance(
                    new CasRuntimeConfigBuilder().defaults(Command.MemcacheD).build());
            MEMCACHED_EXECUTABLE = runtime.prepare(new MemcachedConfig(Version.V1_4_22, PORT));
            MEMCACHED = MEMCACHED_EXECUTABLE.start();
        } catch (final Exception e) {
            getLogger(MemcachedTestUtils.class).warn("Aborting since no memcached server could be started.", e);
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

    public static boolean isMemcachedListening() {
        try (Socket socket = new Socket("memcached-14938.c10.us-east-1-3.ec2.cloud.redislabs.com", PORT)) {
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
