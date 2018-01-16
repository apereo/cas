package org.apereo.cas.web.report;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.SystemUtils;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;

/**
 * This is {@link CasInfoEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class CasInfoEndpointContributor implements InfoContributor {
    @Override
    public void contribute(final Info.Builder builder) {
        builder.withDetails(SystemUtils.getSystemInfo());
    }
}
