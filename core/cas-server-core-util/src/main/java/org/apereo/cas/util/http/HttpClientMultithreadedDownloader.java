package org.apereo.cas.util.http;

import com.github.axet.wget.SpeedInfo;
import com.github.axet.wget.WGet;
import com.github.axet.wget.info.DownloadInfo;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstraction for a message that is sent to an http endpoint.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class HttpClientMultithreadedDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientMultithreadedDownloader.class);

    private final Resource resourceToDownload;
    private final File targetDestination;

    public HttpClientMultithreadedDownloader(final Resource resourceToDownload, final File targetDestination) {
        this.resourceToDownload = resourceToDownload;
        this.targetDestination = targetDestination;
    }

    /**
     * Download.
     */
    public void download() {
        final AtomicBoolean stop = new AtomicBoolean(false);
        try {
            final DownloadInfo info = new DownloadInfo(resourceToDownload.getURL());
            final DownloadStatusListener status = new DownloadStatusListener(info);

            // extract information from the web
            info.extract(stop, status);

            // enable multipart download
            info.enableMultipart();

            // create downloader
            final WGet w = new WGet(info, this.targetDestination);

            // init speed info
            status.speedInfo.start(0);

            // will blocks until download finishes
            LOGGER.info("Starting to download resource [{}] into [{}]", this.resourceToDownload, targetDestination);
            w.download(stop, status);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static class DownloadStatusListener implements Runnable {
        private final DownloadInfo info;
        private final SpeedInfo speedInfo = new SpeedInfo();
        private long last;

        /**
         * Instantiates a new Download status listener.
         *
         * @param info the info
         */
        DownloadStatusListener(final DownloadInfo info) {
            this.info = info;
        }

        public void run() {

            switch (info.getState()) {
                case DONE:
                    speedInfo.end(info.getCount());
                    LOGGER.info("Download completed. [{}] average speed ([{}])", info.getState(),
                            FileUtils.byteCountToDisplaySize(speedInfo.getAverageSpeed()));
                    break;

                case RETRYING:
                    LOGGER.debug("[{}] retry: [{}], delay: [{}]", info.getState(), info.getRetry(), info.getDelay());
                    break;

                case DOWNLOADING:
                    speedInfo.step(info.getCount());
                    final long now = System.currentTimeMillis();
                    if (now - 1_000 > last) {
                        last = now;

                        final StringBuilder partBuilder = new StringBuilder();
                        if (info.getParts() != null) {
                            info.getParts().forEach(p -> {
                                switch (p.getState()) {
                                    case DOWNLOADING:
                                        partBuilder.append(String.format("Part#%d(%.2f) ", p.getNumber(),
                                                p.getCount() / (float) p.getLength()));
                                        break;
                                    case ERROR:
                                    case RETRYING:
                                        partBuilder.append(String.format("Part#%d(%s) ", p.getNumber(),
                                                p.getException().getMessage() + " r:" + p.getRetry() + " d:" + p.getDelay()));
                                        break;
                                    default:
                                        break;
                                }
                            });
                        }

                        final float p = info.getCount() / (float) info.getLength();
                        LOGGER.debug(String.format("%.2f %s (%s / %s)", p, partBuilder.toString(),
                                FileUtils.byteCountToDisplaySize(speedInfo.getCurrentSpeed()),
                                FileUtils.byteCountToDisplaySize(speedInfo.getAverageSpeed())));
                    }
                    break;

                case EXTRACTING:
                case EXTRACTING_DONE:
                default:
                    LOGGER.debug(info.getState().toString());
                    break;
            }
        }
    }
}
