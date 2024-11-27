package org.apereo.cas.web.report;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.InetAddressUtils;
import org.apereo.cas.util.SystemUtils;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import java.util.Map;

/**
 * This is {@link CasInfoEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class CasInfoEndpointContributor implements InfoContributor {
    private final CasConfigurationProperties casProperties;
    private final CasRuntimeModuleLoader loader;

    @Override
    public void contribute(final Info.Builder builder) {
        builder.withDetail("systemInfo", SystemUtils.getSystemInfo());
        builder.withDetail("casModules", Unchecked.supplier(loader::load).get());
        builder.withDetail("server", Map.of(
            "hostname", StringUtils.defaultIfBlank(casProperties.getHost().getName(), "N/A"),
            "host", InetAddressUtils.getCasServerHostName(),
            "name", StringUtils.defaultIfBlank(casProperties.getServer().getName(), "N/A"),
            "scope", StringUtils.defaultIfBlank(casProperties.getServer().getScope(), "N/A")
        ));
    }
}
