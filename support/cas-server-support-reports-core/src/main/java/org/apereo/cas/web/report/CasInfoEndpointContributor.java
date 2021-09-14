package org.apereo.cas.web.report;

import org.apereo.cas.util.SystemUtils;
import org.apereo.cas.util.feature.CasRuntimeModuleLoader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

/**
 * This is {@link CasInfoEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class CasInfoEndpointContributor implements InfoContributor {
    private final CasRuntimeModuleLoader loader;

    @Override
    public void contribute(final Info.Builder builder) {
        builder.withDetail("systemInfo", SystemUtils.getSystemInfo());
        builder.withDetail("casModules", loader.load());
    }
}
