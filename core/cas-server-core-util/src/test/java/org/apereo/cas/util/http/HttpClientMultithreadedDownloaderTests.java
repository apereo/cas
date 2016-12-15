package org.apereo.cas.util.http;

import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.Assert.*;

/**
 * This is {@link HttpClientMultithreadedDownloaderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {
                AopAutoConfiguration.class,
                CasCoreUtilConfiguration.class})
@EnableScheduling
public class HttpClientMultithreadedDownloaderTests {
    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void verify() throws Exception {
        final Resource resource = resourceLoader.getResource("https://raw.githubusercontent.com/apereo/cas/master/NOTICE");
        final File target = File.createTempFile("notice", ".md");
        final HttpClientMultithreadedDownloader downloader = new HttpClientMultithreadedDownloader(resource, target);
        downloader.download();
        assertTrue(target.exists());
    }
}
