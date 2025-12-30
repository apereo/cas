package org.apereo.cas.config;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * This is {@link AmazonS3PropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AmazonS3PropertySourceLocator implements PropertySourceLocator, DisposableBean {
    private final S3Client s3Client;
    private final String bucketName;

    @Override
    public void destroy() throws Exception {
        s3Client.close();
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val context = AmazonS3BucketPropertySource.class.getName();
        return new AmazonS3BucketPropertySource(context, s3Client, bucketName);
    }
}
