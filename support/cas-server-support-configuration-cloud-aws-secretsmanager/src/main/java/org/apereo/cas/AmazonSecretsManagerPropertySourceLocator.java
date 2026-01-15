package org.apereo.cas;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

/**
 * This is {@link AmazonSecretsManagerPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AmazonSecretsManagerPropertySourceLocator implements PropertySourceLocator, DisposableBean {
    private final SecretsManagerClient secretsManagerClient;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val sourceName = AmazonSecretsManagerPropertySource.class.getSimpleName();
        return new AmazonSecretsManagerPropertySource(sourceName, secretsManagerClient);
    }

    @Override
    public void destroy() throws Exception {
        secretsManagerClient.close();
    }
}

