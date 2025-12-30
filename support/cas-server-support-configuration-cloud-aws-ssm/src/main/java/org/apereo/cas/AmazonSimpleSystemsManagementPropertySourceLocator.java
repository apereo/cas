package org.apereo.cas;

import module java.base;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;

/**
 * This is {@link AmazonSimpleSystemsManagementPropertySourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AmazonSimpleSystemsManagementPropertySourceLocator implements PropertySourceLocator, DisposableBean {
    private final SsmClient ssmClient;

    @Override
    public PropertySource<?> locate(final Environment environment) {
        val sourceName = AmazonSimpleSystemsManagementPropertySource.class.getSimpleName();
        val profiles = new ArrayList<String>();
        profiles.add(StringUtils.EMPTY);
        profiles.addAll(List.of(environment.getActiveProfiles()));
        return new AmazonSimpleSystemsManagementPropertySource(sourceName, ssmClient, profiles);
    }

    @Override
    public void destroy() {
        ssmClient.close();
    }
}
