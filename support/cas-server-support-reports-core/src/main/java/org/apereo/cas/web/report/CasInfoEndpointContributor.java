package org.apereo.cas.web.report;

import org.apereo.cas.util.SystemUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link CasInfoEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class CasInfoEndpointContributor implements InfoContributor {
    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void contribute(final Info.Builder builder) {
        builder.withDetail("systemInfo", SystemUtils.getSystemInfo());
        builder.withDetail("casModules", SystemUtils.getRuntimeModules(applicationContext));
    }
}
