package org.apereo.cas.util.http;

import com.github.axet.wget.SpeedInfo;
import com.github.axet.wget.WGet;
import com.github.axet.wget.info.DownloadInfo;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstraction for a message that is sent to an http endpoint.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class HttpClientMultiThreadedDownloader {
    private final Resource resourceToDownload;
    private final File targetDestination;

    /**
     * Download.
     */
    @SneakyThrows
    public void download() {
        final var stop = new AtomicBoolean(false);
        final var info = new DownloadInfo(resourceToDownload.getURL());
        final var status = new DownloadStatusListener(info);

        info.extract(stop, status);

        info.enableMultipart();

        final var w = new WGet(info, this.targetDestination);

        status.speedInfo.start(0);

        LOGGER.info("Starting to download resource [{}] into [{}]", this.resourceToDownload, targetDestination);
        w.download(stop, status);

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

        @Override
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
                    final var now = System.currentTimeMillis();
                    if (now - 1_000 > last) {
                        last = now;

                        final var partBuilder = new StringBuilder();
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

                        final var p = info.getCount() / (float) info.getLength();
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
